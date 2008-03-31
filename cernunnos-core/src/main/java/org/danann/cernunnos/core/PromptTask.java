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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import org.dom4j.Node;

import org.danann.cernunnos.AbstractContainerTask;
import org.danann.cernunnos.Attributes;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.LiteralPhrase;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.Task;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public final class PromptTask extends AbstractContainerTask {

	// Instance Members.
	private Phrase attribute_name;
	private Phrase message;
	private List<Phrase> options;
	private Phrase dflt;

	/*
	 * Public API.
	 */

	public static final Reagent ATTRIBUTE_NAME = new SimpleReagent("ATTRIBUTE_NAME", "@attribute-name", ReagentType.PHRASE, 
					String.class, "Optional name under which the user's response will be registered as a request " +
					"attribute.  If omitted, the name 'Attributes.OBJECT' will be used.", 
					new LiteralPhrase(Attributes.OBJECT));

	public static final Reagent MESSAGE = new SimpleReagent("MESSAGE", "@message", ReagentType.PHRASE, 
					String.class, "Textual description of the needed input.");

	public static final Reagent OPTIONS = new SimpleReagent("OPTIONS", "option/@regex", ReagentType.NODE_LIST, 
					List.class, "Optional set of valid inputs.  If not provided, any non-empty input " +
					"will be accepted.", new LinkedList<Phrase>());

	public static final Reagent DEFAULT = new SimpleReagent("DEFAULT", "@default", ReagentType.PHRASE, 
					String.class, "Optional default value that will be used if the user's response " +
					"is empty.  The value of DEFAULT does not need to appear in the set of OPTIONS.", 
					new LiteralPhrase(null));

	public static final Reagent SUBTASKS = new SimpleReagent("SUBTASKS", "subtasks/*", ReagentType.NODE_LIST, 
					List.class, "The set of tasks that are children of this task.", new LinkedList<Task>());

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {ATTRIBUTE_NAME, MESSAGE, OPTIONS, DEFAULT, SUBTASKS};
		final Formula rslt = new SimpleFormula(getClass(), reagents);
		return rslt;
	}

	public void init(EntityConfig config) {

		super.init(config);

		// Instance Members.
		this.attribute_name = (Phrase) config.getValue(ATTRIBUTE_NAME);
		this.message = (Phrase) config.getValue(MESSAGE);
		this.options = new LinkedList<Phrase>();
		List<?> list = (List<?>) config.getValue(OPTIONS);
		for (Object o : list) {
			Node n = (Node) o;
			this.options.add(config.getGrammar().newPhrase(n.getText()));
		}
		this.dflt = (Phrase) config.getValue(DEFAULT);

	}

	public void perform(TaskRequest req, TaskResponse res) {
		
		final String name = (String) attribute_name.evaluate(req, res);
		final String m = (String) message.evaluate(req, res);
		final String d = (String) dflt.evaluate(req, res);
		
		final List<String> list = new LinkedList<String>();
		for (Phrase p : options) {
			list.add((String) p.evaluate(req, res));
		}
		
		try {
			
			// Add some visual space...
			System.out.println();

			final InputStreamReader inpt = new InputStreamReader(System.in);
			final BufferedReader reader = new BufferedReader(inpt);
			String response = null;
			boolean done = false;
			while (!done) {
				
				final StringBuilder bldr = new StringBuilder();
				bldr.append(m);
				
				// List allowable responses...
				if (list.size() > 0) {
					bldr.append("\nAllowable responses (must match one of " +
										"these regular expressions):");
					for (String s : list) {
						bldr.append("\n\t- ").append(s);
					}
				}
				
				if (d != null) {
					bldr.append("\nDefault response:  ").append(d);
				}
				
				bldr.append("\nResponse:  ");

				System.out.print(bldr.toString());
				response = reader.readLine();
				
				if (response == null || response.trim().length() == 0) {
					// No response is only allowable when there's a default...
					if (d != null) {
						response = d;
						done = true;
					}
				} else {
					done = isValid(response, list);
				}
				
			} 
			
			res.setAttribute(name, response);
			super.performSubtasks(req, res);
			

		} catch (Throwable t) {
			final String msg = "Error prompting user for the following input:"
							+ "\n\t\tATTRIBUTE_NAME=" + name
							+ "\n\t\tMESSAGE=" + m
							+ "\n\t\tDEFAULT=" + d;
			throw new RuntimeException(msg, t);
		}
		
	}
	
	/*
	 * Implementation.
	 */

	private boolean isValid(String response, List<String> options) {

		boolean rslt = false;	// default...

		if (options.size() == 0) {
			// If no acceptable responses are specified, it's wide open...
			rslt = true;
		} else {
			for (String o : options) {
				if (response.matches(o)) {
					rslt = true;
					break;
				}
			}
		}
		
		return rslt;
		
	}

}