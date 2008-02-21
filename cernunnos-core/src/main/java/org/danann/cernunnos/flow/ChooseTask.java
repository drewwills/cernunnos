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

package org.danann.cernunnos.flow;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;

import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Grammar;
import org.danann.cernunnos.LiteralPhrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.Task;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public final class ChooseTask implements Task {

	// Instance Members.
	private List<IfTask> whenList;
	private IfTask otherwise;

	/*
	 * Public API.
	 */

	public static final Reagent WHEN = new SimpleReagent("WHEN", "when", ReagentType.NODE_LIST, List.class, 
						"Zero or more child <when> elements each containing a 'test' attribute.  The "
						+ "SUBTASKS of the first WHEN whose test is positive (true) will be performed.", 
						new LinkedList());

	public static final Reagent OTHERWISE = new SimpleReagent("OTHERWISE", "otherwise", ReagentType.NODE_LIST, List.class, 
						"Optional <otherwise> element whose children will be performed if none of the specified "
						+ "WHEN conditions is met.", new LinkedList());

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {WHEN, OTHERWISE};
		final Formula rslt = new SimpleFormula(ChooseTask.class, reagents);
		return rslt;
	}

	public void init(EntityConfig config) {

		// Instance Members.
		List wElements = (List) config.getValue(WHEN);
		this.whenList = new LinkedList<IfTask>();
		for (Iterator wIt = wElements.iterator(); wIt.hasNext();) {
			Element e = (Element) wIt.next();
			IfTask k = new IfTask();
			EntityConfig ec = new EntityConfigImpl(config.getGrammar(), k.getFormula(), e);
			k.init(ec);
			whenList.add(k);
		}
		
		List oElements = (List) config.getValue(OTHERWISE);
		switch (oElements.size()) {
			case 0:
				// None provided -- all is well...
				this.otherwise = null;
				break;
			case 1:
				// One <otherwise> was provided...
				Element e = (Element) oElements.get(0);
				this.otherwise = new IfTask();
				EntityConfigImpl ec = new EntityConfigImpl(config.getGrammar(), otherwise.getFormula(), e);
				ec.setValue(IfTask.TEST, new LiteralPhrase(Boolean.TRUE));
				otherwise.init(ec);
				break;
			default:
				break;			
		}
		
	}
	
	public void perform(TaskRequest req, TaskResponse res) {

		Task runme = null;
		for (IfTask k : whenList) {
			if (k.isApplicable(req, res)) {
				runme = k;
				break;
			}
		}
		runme = runme != null ? runme : otherwise;

		if (runme != null) {
			runme.perform(req, res);
		}
		
	}

	/*
	 * Nested Types.
	 */

	private static final class EntityConfigImpl implements EntityConfig {
		
		// Instance Members.
		private final Grammar grammar;
		private final Formula formula;
		private final Map<Reagent,Object> mappings;
		
		/*
		 * Public API.
		 */

		public EntityConfigImpl(Grammar g, Formula f, Element e) {
			
			// Assertions...
			if (g == null) {
				String msg = "Argument 'g [Grammar]' cannot be null.";
				throw new IllegalArgumentException(msg);
			}
			if (f == null) {
				String msg = "Argument 'f [Formula]' cannot be null.";
				throw new IllegalArgumentException(msg);
			}
			if (e == null) {
				String msg = "Argument 'e [Element]' cannot be null.";
				throw new IllegalArgumentException(msg);
			}
			
			// Instance Members.
			this.grammar = g;
			this.formula = f;
			this.mappings = new HashMap<Reagent,Object>();
			for (Reagent r : f.getReagents()) {
				Object value = r.getReagentType().evaluate(g, e, r.getXpath());
				if (value == null) {
					// First see if there's a default...
					if (r.hasDefault()) {
						value = r.getDefault();
					} else if (r.equals(IfTask.TEST)) {
						// This is the OTHERWISE, we're going to add this later... 
					} else {
						String msg = "The required expression '" + r.getXpath() 
							+ "' is missing from the following node:  " + e.asXML();
						throw new RuntimeException(msg);
					}
				}
				mappings.put(r, value);
			}

		}
		
		public Grammar getGrammar() {
			return grammar;
		}
		
		public Formula getFormula() {
			return formula;
		}
		
		public Object getValue(Reagent r) {

			// Assertions...
			if (r == null) {
				String msg = "Argument 'r [Reagent]' cannot be null.";
				throw new IllegalArgumentException(msg);
			}
			if (!formula.getReagents().contains(r)) {
				String msg = "This task does not define the specified reagent:  " 
															+ r.getXpath();
				throw new IllegalArgumentException(msg);
			}
			if (!mappings.keySet().contains(r)) {
				String msg = "No value is established for the specified reagent "
												+ "(error in construction).";
				throw new IllegalArgumentException(msg);
			}

			return mappings.get(r);
			
		}
		
		public void setValue(Reagent r, Object value) {

			// Assertions...
			if (r == null) {
				String msg = "Argument 'r [Reagent]' cannot be null.";
				throw new IllegalArgumentException(msg);
			}
			if (!formula.getReagents().contains(r)) {
				String msg = "This task does not define the specified reagent:  " 
															+ r.getXpath();
				throw new IllegalArgumentException(msg);
			}

			mappings.put(r, value);
			
		}
		
		public Map<Reagent,Object> getValues() {
			return (Map<Reagent,Object>) Collections.unmodifiableMap(mappings);
		}

	}
	
}