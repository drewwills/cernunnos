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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

/**
 * Base class for simple container tasks.  <code>AbstractContainerTask</code> 
 * provides the <code>SUBTASKS</code> reagent for subclasses to add to their 
 * formulas.
 */
public abstract class AbstractContainerTask implements Task {
    protected final Log logger = LogFactory.getLog(this.getClass());

	// NB:  We're using the subtasks reference as a que, so we want the concrete 
	// reference type, not the interface.
	private LinkedList<Task> subtasks;

	/*
	 * Public API.
	 */
	
	/**
	 * Subclasses <strong>must</strong> invoke <code>super.init</code> within 
	 * their own <code>init</code> method for subtasks to bootstrap properly.
	 * 
	 * @param config Task configuration information provided by the Cernunnos 
	 * runtime.
	 */
	public void init(EntityConfig config) {

		// The following fancy conversion is here to avoid type safety warnings..
		List list = (List) config.getValue(SUBTASKS);
		this.subtasks = new LinkedList<Task>();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Element e = (Element) it.next();
			this.subtasks.add(config.getGrammar().newTask(e, this));
		}
		
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
	 * Subclasses <strong>must</strong> invoke 
	 * <code>super.performChildren</code> within their own <code>perform</code> 
	 * method if child tasks are to be executed at all.  Naturally, subclasses 
	 * may process their own operations before subtasks, after subtasks, or 
	 * both.  Subclasses may also refrain from invoking child tasks if 
	 * circumstances warrent (such as where there is an error).
	 * 
	 * @param req Contains input information and operations. 
	 * @param res Contains output information and operations.
	 */
	protected void performSubtasks(TaskRequest req, TaskResponse res) {

		// Assertions...
		if (req == null) {
			String msg = "Argument 'req' cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		if (res == null) {
			String msg = "Argument 'res' cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		if (subtasks == null) {
			String msg = "Child tasks have not been initialized.  Subclasses "
							+ "of AbstractContainerTask must call super.init() "
							+ "within their own init() method.";
			throw new IllegalStateException(msg);
		}
		
		// Invoke each of our childern...
		for (Task k : subtasks) {
			k.perform(req, res);
		}
		
	}

}