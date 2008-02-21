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

package org.danann.cernunnos.core;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.dom4j.Node;

import org.danann.cernunnos.AbstractContainerTask;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.Task;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public final class WithTask extends AbstractContainerTask {

	// Instance Members.
	private Map<Phrase,Phrase> attributes;

	/*
	 * Public API.
	 */

	public static final Reagent KEYS = new SimpleReagent("KEYS", "attribute/@key", ReagentType.NODE_LIST, List.class,
						"Name of a request attribute that will be available to subtasks.", new LinkedList<Task>());

	public static final Reagent VALUES = new SimpleReagent("VALUES", "attribute/text()", ReagentType.NODE_LIST, 
						List.class, "Value of the request attribute.", new LinkedList<Task>());

	public static final Reagent SUBTASKS = new SimpleReagent("SUBTASKS", "subtasks/*", ReagentType.NODE_LIST, List.class,
						"The set of tasks that are children of this task.", new LinkedList<Task>());

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {KEYS, VALUES, SUBTASKS};
		final Formula rslt = new SimpleFormula(WithTask.class, reagents);
		return rslt;
	}

	public void init(EntityConfig config) {
		
		super.init(config);

		// Instance Members.
		this.attributes = new HashMap<Phrase,Phrase>();
		List keys = (List) config.getValue(KEYS);
		List values = (List) config.getValue(VALUES);
		for (int i=0; i < keys.size(); i++) {
			
			Node k = (Node) keys.get(i);
			Node v = (Node) values.get(i);
			
			attributes.put(config.getGrammar().newPhrase(k.getText()), 
						config.getGrammar().newPhrase(v.getText()));

		}
		
	}

	public void perform(TaskRequest req, TaskResponse res) {
		
		for (Entry<Phrase,Phrase> y : attributes.entrySet()) {
			res.setAttribute((String) y.getKey().evaluate(req, res), 
								y.getValue().evaluate(req, res));
		}
		
		super.performSubtasks(req, res);
		
	}
	
}