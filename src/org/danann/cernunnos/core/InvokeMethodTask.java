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

package org.danann.cernunnos.core;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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

public class InvokeMethodTask extends AbstractContainerTask {

	// Instance Members.
	private Phrase object;
	private Phrase clazz;
	private Phrase method;
	private List<Phrase> parameters;
	private Phrase attribute_name;

	/*
	 * Public API.
	 */

	public static final Reagent OBJECT = new SimpleReagent("OBJECT", "@object", ReagentType.PHRASE, Object.class, 
							"Optional object instance upon which the specified METHOD will be invoked.  "
							+ "Provide either OBJECT or CLASS, but not both.", null);

	public static final Reagent CLASS = new SimpleReagent("CLASS", "@class", ReagentType.PHRASE, String.class, 
							"Optional name (fully-qualified) of a class that contains static member METHOD.  "
							+ "Provide either OBJECT or CLASS, but not both.", null);

	public static final Reagent METHOD = new SimpleReagent("METHOD", "@method", ReagentType.PHRASE, String.class, 
							"Name of a method that exists on CLASS.");

	public static final Reagent PARAMETERS = new SimpleReagent("PARAMETERS", "parameter/@value", ReagentType.NODE_LIST, List.class, 
							"The parameters (if any) of METHOD.", Collections.emptyList());

	public static final Reagent SUBTASKS = new SimpleReagent("SUBTASKS", "subtasks/*", ReagentType.NODE_LIST, List.class, 
							"The set of tasks that are children of this task.", new LinkedList<Task>());

	public static final Reagent ATTRIBUTE_NAME = new SimpleReagent("ATTRIBUTE_NAME", "@attribute-name", ReagentType.PHRASE, String.class,
							"Optional name under which the result of invoking METHOD will be registered as a request attribute.", null);

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {OBJECT, CLASS, METHOD, PARAMETERS, ATTRIBUTE_NAME, SUBTASKS};
		final Formula rslt = new SimpleFormula(InvokeMethodTask.class, reagents);
		return rslt;
	}

	public void init(EntityConfig config) {

		super.init(config);		

		// Instance Members.
		this.object = (Phrase) config.getValue(OBJECT); 
		this.clazz = (Phrase) config.getValue(CLASS); 
		this.method = (Phrase) config.getValue(METHOD); 
		this.parameters = new LinkedList<Phrase>();
		List nodes = (List) config.getValue(PARAMETERS);		
		for (Iterator it = nodes.iterator(); it.hasNext();) {
			Node n = (Node) it.next();
			parameters.add(config.getGrammar().newPhrase(n.getText()));
		}
		this.attribute_name = (Phrase) config.getValue(ATTRIBUTE_NAME);
		
	}

	public void perform(TaskRequest req, TaskResponse res) {
		
		String m = (String) method.evaluate(req, res);

		try {
						
			// Evaluate the parameters...
			Class[] argTypes = new Class[parameters.size()];
			Object[] argValues = new Object[parameters.size()];
			for (int i=0; i < parameters.size(); i++) {
				argValues[i] = parameters.get(i).evaluate(req, res);
				// NB: NPE if one or more args is null...
				argTypes[i] = argValues[i].getClass();
			}
			
			// Find the method & target...
			Method[] methods = null;
			Object target = null;
			if (object != null) {
				target = object.evaluate(req, res);
				methods = target.getClass().getMethods();
			} else {
				String c = (String) clazz.evaluate(req, res);
				methods = Class.forName(c).getDeclaredMethods();
			}

			Method myMethod = null;
			for (Method d : methods) {
				if (d.getName().equals(m)) {
					Class[] params = d.getParameterTypes(); 
					if (params.length == argTypes.length) {
						boolean matches = true;
						for (int i=0; i < params.length; i++) {
							ArrayList<Class> types = new ArrayList<Class>();
							types.addAll(Arrays.asList(argTypes[i].getInterfaces()));
							for (Class sup = argTypes[i].getSuperclass(); sup != null; sup = sup.getSuperclass()) {
								types.add(sup);
								types.addAll(Arrays.asList(sup.getInterfaces()));
							}
							if (!types.contains(params[i])) {
								matches = false;
								break;
							}
						}
						if (matches) {
							myMethod = d;
							break;
						}
					}
				}
			}
			if (myMethod == null) {
				String msg = "Unable to locate the specified method:  " + m;
				for (Class z : argTypes) {
					msg = msg + "\n\targ.getClass()=" + z.getName();
				}
				throw new RuntimeException(msg);
			}
			
			Object rslt = myMethod.invoke(target, argValues);
			
			if (attribute_name != null) {
				String name = (String) attribute_name.evaluate(req, res);
				res.setAttribute(name, rslt);
			}

			super.performSubtasks(req, res);
			
		} catch (Throwable t) {
			String msg = "Error invoking the specified method:  " + m;
			throw new RuntimeException(msg, t);
		}
		
	}
	
}