/*
 * Copyright 2007 Andrew Wills
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

package org.danann.cernunnos;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;

/**
 * Base class for simple container tasks.  <code>AbstractContainerTask</code> 
 * provides the <code>SUBTASKS</code> reagent for subclasses to add to their 
 * formulas.
 */
public abstract class AbstractContainerTask implements Task {
    protected final Log log = LogFactory.getLog(this.getClass());

	private List<Task> subtasks;

	/*
	 * Public API.
	 */
	
	/**
	 * Use this <code>List</code> to create a <code>SUBTASKS</code> reagent that 
	 * will not cause <code>POSSIBLE PROGRAMMING ERROR...</code> warnings when 
	 * empty.  
	 */
	public static final List<Element> SUPPRESS_EMPTY_SUBTASKS_WARNINGS = 
	                        createSuppressEmptySubtasksWarningsList();
	
	/**
	 * Subclasses <strong>must</strong> invoke <code>super.init</code> within 
	 * their own <code>init</code> method for subtasks to bootstrap properly.
	 * 
	 * @param config Task configuration information provided by the Cernunnos 
	 * runtime.
	 */
	public void init(EntityConfig config) {
	    this.subtasks = this.loadSubtasks(config, SUBTASKS, true);
	}
	
	/*
	 * Protected API.
	 */
	
	/**
	 * Initializes (but does not bootstrap) the collection of child tasks.  
	 */
	protected AbstractContainerTask() {
		// Instance Members.
		this.subtasks = null;
	}

	/**
	 * Subclasses <strong>must</strong> add a reagent named "SUBTASKS" to their 
	 * formulas in order that child tasks get bootstrapped properly.  They can 
	 * use this one except in cases where a different XPath expression must be 
	 * specified.
	 */
	protected static final Reagent SUBTASKS = new SimpleReagent("SUBTASKS", "*", ReagentType.NODE_LIST, List.class, 
								"The set of tasks that are children of this task.", new LinkedList<Task>());
	
	/**
	 * Abstracts the loading of subtasks for a Reagent into a single menthod
	 * 
	 * @param warnIfMissing If true a WARN level log message will be issued if no tasks are loaded for the Reagent 
	 */
	@SuppressWarnings("unchecked")
    protected List<Task> loadSubtasks(EntityConfig config, Reagent subtasksPhrase, boolean warnIfMissing) {
	    final List<Task> subtasks = new LinkedList<Task>();
	    
	    final List<Element> taskElements = (List<Element>) config.getValue(subtasksPhrase);
	    final Grammar grammar = config.getGrammar();
        for (final Element taskElement : taskElements) {
            final Task task = grammar.newTask(taskElement, this);
            subtasks.add(task);
        }
        
        // There's likely an error in the Cernunnos XML 
        // if we don't have any subtasks, issue a warning...
        if (warnIfMissing && subtasks.size() == 0 && log.isWarnEnabled()) {
            log.warn("POSSIBLE PROGRAMMING ERROR:  Class '" 
                    + getClass().getName() 
                    + "' has an empty collection of " + subtasksPhrase.getName());
        }
        
        return subtasks;
	}

	/**
	 * Subclasses <strong>must</strong> invoke 
	 * <code>super.performSubtasks</code> within their own <code>perform</code> 
	 * method if child tasks are to be executed at all.  Naturally, subclasses 
	 * may process their own operations before subtasks, after subtasks, or 
	 * both.  Subclasses may also refrain from invoking child tasks if 
	 * circumstances warrant (such as where there is an error).
	 * 
	 * @param req Contains input information and operations. 
	 * @param res Contains output information and operations.
	 */
	protected void performSubtasks(TaskRequest req, TaskResponse res) {
	    this.performSubtasks(req, res, this.subtasks);
	}
	
	/**
	 * Executes a List of Tasks as children of this Task
	 */
	protected void performSubtasks(TaskRequest req, TaskResponse res, List<Task> tasks) {
        // Assertions...
        if (req == null) {
            String msg = "Argument 'req' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (res == null) {
            String msg = "Argument 'res' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (tasks == null) {
            String msg = "Child tasks have not been initialized.  Subclasses "
                            + "of AbstractContainerTask must call super.init() "
                            + "within their own init() method.";
            throw new IllegalStateException(msg);
        }
        
        // Invoke each of our children...
        for (Task k : tasks) {
            k.perform(req, res);
        }
	}
	
	/*
	 * Implementation.
	 */
	
    private static List<Element> createSuppressEmptySubtasksWarningsList() {
        DocumentFactory fac = new DocumentFactory();
        List<Element> list = new LinkedList<Element>();
        list.add(fac.createElement("org.danann.cernunnos.NoOpTask"));
        return Collections.unmodifiableList(list);
	}

}