/*
 * Copyright 2008 Andrew Wills
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.danann.cernunnos.runtime;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.dom4j.Element;

import org.danann.cernunnos.AbstractContainerTask;
import org.danann.cernunnos.Attributes;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Grammar;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.ResourceHelper;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.Task;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public final class EncloseGrammarTask extends AbstractContainerTask {
    
    // Instance Members.
    private String grammar_name;
    private final ResourceHelper resource = new ResourceHelper();

    /*
     * Public API.
     */

    public static final Reagent NAME = new SimpleReagent("NAME", "@name", ReagentType.STRING, String.class,
                        "A short name for this grammar, beginning with a capital letter.  One word names " +
                        "are preferable (e.g. 'Core', 'Mail').  Grammar names should be unique for clarity.", 
                        XmlGrammar.DEFAULT_GRAMMAR_NAME);

    public static final Reagent ENTRIES = new SimpleReagent("ENTRIES", "entries/*", ReagentType.NODE_LIST, 
                        List.class, "Set of grammar entries that will suppliment the existing grammar.", 
                        new LinkedList<Task>());

    public static final Reagent SUBTASKS = new SimpleReagent("SUBTASKS", "subtasks/*", ReagentType.NODE_LIST, List.class,
                        "The set of tasks that are children of this task;  only these tasks may use the grammar " +
                        "entries defined by this task.", new LinkedList<Task>());

    public Formula getFormula() {
        Reagent[] reagents = new Reagent[] {NAME, ResourceHelper.CONTEXT_SOURCE, 
                    ResourceHelper.LOCATION_TASK_NODEFAULT, ENTRIES, SUBTASKS};
        final Formula rslt = new SimpleFormula(this.getClass(), reagents);
        return rslt;
    }

    public void init(EntityConfig config) {

        // Instance Members.
        this.grammar_name = (String) config.getValue(NAME);

        // We'll use these in a couple places...
        final RuntimeRequestResponse req = new RuntimeRequestResponse();
        final String origin = "classpath:/" + getClass().getName()
                    .replaceAll("\\.", "/") 
                    + ".java";  // This class stands in for Attributes.ORIGIN
        req.setAttribute(Attributes.ORIGIN, origin);
        final TaskResponse res = new RuntimeRequestResponse();
        Grammar g = null;

        // We must determine if the grammar is defined in-situ 
        // or specified by a CONTEXT/LOCATION pairing...
        if (config.getValue(ResourceHelper.LOCATION_TASK_NODEFAULT) == null) {
            
            // In-situ:   prepare an overlapping grammar object by running the entry tasks...
            g = new XmlGrammar(grammar_name, origin, config.getGrammar());
            req.setAttribute(SECRET_GRAMMAR_KEY, g);
            final List<?> entries = (List<?>) config.getValue(ENTRIES);

            // There's likely an error in the Cernunnos XML 
            // if we don't have any subtasks, issue a warning...
            if (entries.size() == 0 && log.isWarnEnabled()) {
                String msg = "POSSIBLE PROGRAMMING ERROR:  Class '" 
                                    + getClass().getName() 
                                    + "' has an empty collection of ENTRIES.";
                log.warn(msg);
            }

            for (final Iterator<?> it = entries.iterator(); it.hasNext();) {
                Task k = g.newTask((Element) it.next(), this);
                // Run each task as it's parsed;  this way, later 
                // definitions may leverage earlier definitions...
                k.perform(req, res);
            }
            
        } else {
            
            // External grammar file...
            this.resource.init(config);
            final ScriptRunner runner = new ScriptRunner();
            g = (Grammar) runner.evaluate(resource.evaluate(req, res).toExternalForm());
            
        }
        
        super.init(new SimpleEntityConfig(g, config.getEntryName(), 
                        config.getSource(), config.getFormula(), 
                        config.getValues()));

    }

    public void perform(TaskRequest req, TaskResponse res) {
        super.performSubtasks(req, res);
    }
    
    /*
     * Package API.
     */
    
    static final String SECRET_GRAMMAR_KEY = 
                    "Beyond this place of wrath and tears\n" +
                    "\tLooms but the horror of the shade,";

}
