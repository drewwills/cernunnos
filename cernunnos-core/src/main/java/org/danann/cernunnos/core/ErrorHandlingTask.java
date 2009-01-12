package org.danann.cernunnos.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.danann.cernunnos.Attributes;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Grammar;
import org.danann.cernunnos.LiteralPhrase;
import org.danann.cernunnos.ManagedException;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.Task;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;
import org.dom4j.Element;
import org.dom4j.Node;

public final class ErrorHandlingTask implements Task {

    // Instance Members.
    private List<Task> try_block;
    private List<Phrase> catch_classes;
    private List<List<Task>> catch_blocks;
    private List<Task> finally_block;
    private final Log log = LogFactory.getLog(getClass());

    /*
     * Public API.
     */

    protected static final Reagent TRY = new SimpleReagent("TRY", "try/*", ReagentType.NODE_LIST, List.class, 
                    "A collection of tasks to perform.  If an error occurs within these tasks, it " +
                    "will be caught and excution will continue appropriately.");

    protected static final Reagent CATCH_CLASS = new SimpleReagent("CATCH_CLASS", "catch/@class", ReagentType.NODE_LIST, List.class, 
                    "Optional Java class (<? extends Throwable>) that, if caught, will trigger the CATCH_BLOCK to which it " +
                    "applies.  The default is Throwable.class.", new LiteralPhrase(Throwable.class));

    public static final Reagent CATCH_BLOCK = new SimpleReagent("CATCH_BLOCK", "catch", ReagentType.NODE_LIST, List.class,
                    "Each CATCH_BLOCK specifies a collection of tasks that will be invoked if the corresponding " +
                    "CATCH_CLASS is caught.", new LinkedList<Object>());

    protected static final Reagent FINALLY_BLOCK = new SimpleReagent("FINALLY_BLOCK", "finally/*", ReagentType.NODE_LIST, List.class, 
                    "A collection of tasks to perform after the try and catch blocks are complete. The finally " +
                    "block will be executed regardless of any exception being thrown.", new LinkedList<Task>());

    public Formula getFormula() {
        Reagent[] reagents = new Reagent[] {TRY, CATCH_CLASS, CATCH_BLOCK, FINALLY_BLOCK};
        final Formula rslt = new SimpleFormula(getClass(), reagents);
        return rslt;
    }

    public void init(EntityConfig config) {
        final Grammar grammar = config.getGrammar();
        
        // try_block.
        final List<?> tryList = (List<?>) config.getValue(TRY);
        this.try_block = new ArrayList<Task>(tryList.size());
        for (final Iterator<?> it = tryList.iterator(); it.hasNext();) {
            final Element e = (Element) it.next();
            this.try_block.add(grammar.newTask(e, this));
        }

        // catch_blocks.
        final List<?> catchList = (List<?>) config.getValue(CATCH_BLOCK);
        this.catch_classes = new ArrayList<Phrase>(catchList.size());
        this.catch_blocks = new ArrayList<List<Task>>(catchList.size());
        for (final Iterator<?> it = catchList.iterator(); it.hasNext();) {
            final Element e = (Element) it.next();
            Phrase clazz = null;
            // ToDo:  Use an XPath object once we introduce structure-based reagents...
            final Node classAttr = e.selectSingleNode("@class");         
            if (classAttr != null) {
                clazz = grammar.newPhrase(classAttr);
            } else {
                clazz = (Phrase) CATCH_CLASS.getDefault();
            }
            this.catch_classes.add(clazz);
            
            final List<?> elements = e.elements();
            final List<Task> block = new ArrayList<Task>(elements.size());
            for (final Iterator<?> subs = elements.iterator(); subs.hasNext();) {
                final Element sub = (Element) subs.next();
                block.add(grammar.newTask(sub, this));
            }
            this.catch_blocks.add(block);
        }

        // finally_block.
        final List<?> finallyList = (List<?>) config.getValue(FINALLY_BLOCK);
        this.finally_block = new ArrayList<Task>(finallyList.size());
        for (final Iterator<?> it = finallyList.iterator(); it.hasNext();) {
            final Element e = (Element) it.next();
            this.finally_block.add(grammar.newTask(e, this));
        }
    }

    public void perform(TaskRequest req, TaskResponse res) {
        
        try {
            
            for (Task k : try_block) {
                k.perform(req, res);
            }
            
        } catch (Throwable t) {
            log.warn("Caught the following error.", t);
            
            // Figure out which error to match...
            Throwable compareTo = t instanceof ManagedException ? t.getCause(): t;
            res.setAttribute(Attributes.EXCEPTION, compareTo);
            
            boolean reThrow = true; // Until we know otherwise...
            for (int i=0; i < catch_classes.size(); i++) {
                Class<?> clazz = (Class<?>) catch_classes.get(i).evaluate(req, res);
                if (clazz.isAssignableFrom(compareTo.getClass())) {
                    if (log.isDebugEnabled()) {
                        log.debug("The caught error (" + compareTo.getClass().getName() 
                                    + ") matched the following CATCH_CLASS:  " 
                                    + clazz.getName());
                    }
                    // We have a <catch> clause to handle this error...
                    reThrow = false;
                    List<Task> tasks = catch_blocks.get(i);
                    for (Task k : tasks) {
                        k.perform(req, res);
                    }
                    // We execute *at most* one CATCH_BLOCK...
                    break;  
                }
            }
            if (reThrow) {
                String msg = "ErrorHandlingTask caught an error for which it had " +
                		                    "no associated CATCH_BLOCK:  " + 
                		                    compareTo.getClass().getName();
                throw new RuntimeException(msg, t);
            }
        } finally {
            for (Task k : finally_block) {
                k.perform(req, res);
            }
        }
        
    }

}
