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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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
import org.dom4j.Node;

@Deprecated
public class InvokeMethodTask extends AbstractContainerTask {

	// Instance Members.
	private Phrase object;
	private Phrase clazz;
	private Phrase method;
	private List<Phrase> parameters;
	private List<Phrase> parameter_types;
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

	public static final Reagent PARAMETER_TYPES = new SimpleReagent("PARAMETER_TYPES", "parameter/@type", ReagentType.NODE_LIST, List.class,
							"Optional class name of a PARAMETERS item.  InvokeMethodTask doesn't normally handle null parameters because " +
							"null references can't tell you their type.  Specify PARAMETER_TYPES explicitly for any parameters that may " +
							"contain null values.", null);

	public static final Reagent SUBTASKS = new SimpleReagent("SUBTASKS", "subtasks/*", ReagentType.NODE_LIST, List.class,
							"The set of tasks that are children of this task.", new LinkedList<Task>());

	public static final Reagent ATTRIBUTE_NAME = new SimpleReagent("ATTRIBUTE_NAME", "@attribute-name", ReagentType.PHRASE, String.class,
							"Optional name under which the result of invoking METHOD will be registered as a request attribute.  If " +
							"omitted, the name 'Attributes.OBJECT' will be used.", new LiteralPhrase(Attributes.OBJECT));

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {OBJECT, CLASS, METHOD, PARAMETERS, ATTRIBUTE_NAME, SUBTASKS};
		final Formula rslt = new SimpleFormula(getClass(), reagents);
		return rslt;
	}

	public void init(EntityConfig config) {

		super.init(config);

		// Instance Members.
		this.object = (Phrase) config.getValue(OBJECT);
		this.clazz = (Phrase) config.getValue(CLASS);
		this.method = (Phrase) config.getValue(METHOD);
		this.parameters = new LinkedList<Phrase>();
		this.parameter_types = new LinkedList<Phrase>();
		List<?> nodes = (List<?>) config.getValue(PARAMETERS);
		for (Iterator<?> it = nodes.iterator(); it.hasNext();) {
			Node n = (Node) it.next();
			parameters.add(config.getGrammar().newPhrase(n));
			// See if a type was explicitly specified...
			Node y = n.selectSingleNode("../@type");
			if (y != null) {
				parameter_types.add(config.getGrammar().newPhrase(y));
			} else {
				// We need to order this list in parity w/ the other list...
				parameter_types.add(null);
			}
		}
		this.attribute_name = (Phrase) config.getValue(ATTRIBUTE_NAME);

	}

	public void perform(TaskRequest req, TaskResponse res) {

		String m = (String) method.evaluate(req, res);


		// Evaluate the parameters...
		Class<?>[] argTypes = new Class[parameters.size()];
		Object[] argValues = new Object[parameters.size()];
		try {
			for (int i=0; i < parameters.size(); i++) {
				argValues[i] = parameters.get(i).evaluate(req, res);
				if (parameter_types.get(i) != null) {
				    final String className = (String) parameter_types.get(i).evaluate(req, res);
                    try {
                        argTypes[i] = Class.forName(className);
                    }
                    catch (ClassNotFoundException cnfe) {
                        throw new RuntimeException("Could not find specified class: " + className, cnfe);
                    }
				} else {
					// Infer the type from the object at hand...
					argTypes[i] = argValues[i].getClass();	// NB: NPE if one or more args is null...
				}
			}
		} catch (NullPointerException npe) {
			String msg = "Arguments to InvokeMethodTask may not be null.";
			throw new IllegalArgumentException(msg, npe);
		}

		// Find the method & target...
		Method[] methods = null;
		Object target = null;
		if (object != null) {
			target = object.evaluate(req, res);
			methods = target.getClass().getMethods();
		} else {
			String c = (String) clazz.evaluate(req, res);
			final Class<?> clazzInstance;
            try {
                clazzInstance = Class.forName(c);
            }
            catch (ClassNotFoundException cnfe) {
                throw new RuntimeException("Could not find specified class: " + c, cnfe);
            }
            methods = clazzInstance.getDeclaredMethods();
		}

		Method myMethod = null;
		for (Method d : methods) {
			if (d.getName().equals(m)) {
				Class<?>[] params = d.getParameterTypes();
				if (params.length == argTypes.length) {
					boolean matches = true;
					for (int i=0; i < params.length; i++) {
						ArrayList<Class> types = new ArrayList<Class>();
						types.add(argTypes[i]);
						types.addAll(Arrays.asList(argTypes[i].getInterfaces()));
						for (Class<?> sup = argTypes[i].getSuperclass(); sup != null; sup = sup.getSuperclass()) {
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
			StringBuffer msg = new StringBuffer();
			msg.append("Unable to locate method '").append(m).append("' on ");
			if (target != null) {
				msg.append("object of class '").append(target.getClass().getName()).append("' ");
			} else {
				msg.append("class '").append(clazz.evaluate(req, res)).append("' ");
			}
			msg.append("(argument types follow):");
			for (Class<?> z : argTypes) {
				msg.append("\n\t\targ type=").append(z.getName());
			}
			throw new RuntimeException(msg.toString());
		}

		final Object rslt;
        try {
            rslt = myMethod.invoke(target, argValues);
        }
        catch (IllegalAccessException iae) {
            throw new RuntimeException("Failed to invoke method " + myMethod + " on " + target, iae);
        }
        catch (InvocationTargetException ite) {
            throw new RuntimeException("Failed to invoke method " + myMethod + " on " + target, ite);
        }
        
		String attr = (String) attribute_name.evaluate(req, res);
		res.setAttribute(attr, rslt);

		super.performSubtasks(req, res);
	}

}
