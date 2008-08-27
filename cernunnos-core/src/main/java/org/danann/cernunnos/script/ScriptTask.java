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

package org.danann.cernunnos.script;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.SimpleBindings;

import org.danann.cernunnos.AbstractContainerTask;
import org.danann.cernunnos.Attributes;
import org.danann.cernunnos.BindingsHelper;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.Task;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public class ScriptTask extends AbstractContainerTask {

	// Instance Members.
	private Phrase engine;
	private Phrase script;

	/*
	 * Public API.
	 */

	public static final Reagent ENGINE = new SimpleReagent("ENGINE", "@engine", ReagentType.PHRASE, ScriptEngine.class,
					"Optional instance of ScriptEngine that will be used to invoke SCRIPT.  If one is not provided, " +
					"a new one will be created.  The default is the value of the 'ScriptAttributes.ENGINE.{ENGINE_NAME}' " +
					"request attribute, if present.");

	public static final Reagent SCRIPT = new SimpleReagent("SCRIPT", "script/text()", ReagentType.PHRASE, String.class,
					"Script content to execute.  Must be placed within a child <script> element.");

	public static final Reagent SUBTASKS = new SimpleReagent("SUBTASKS", "subtasks/*", ReagentType.NODE_LIST, List.class,
					"The set of tasks that are children of this task.", new LinkedList<Task>());

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {ENGINE, SCRIPT, SUBTASKS};
		final Formula rslt = new SimpleFormula(getClass(), reagents);
		return rslt;
	}

	public void init(EntityConfig config) {

		super.init(config);

		// Instance Members.
		this.engine = (Phrase) config.getValue(ENGINE);
		this.script = (Phrase) config.getValue(SCRIPT);

	}

	public void perform(TaskRequest req, TaskResponse res) {

		ScriptEngine eng = (ScriptEngine) engine.evaluate(req, res);
		String s = (String) script.evaluate(req, res);
		try {

			Bindings n = new SimpleBindings();

			// Bind simple things (non-Attributes)...
			for (Map.Entry<String,Object> y : req.getAttributes().entrySet()) {
				if (y.getKey().indexOf(".") == -1) {
					n.put(y.getKey(), y.getValue());
				}
			}
			
			// Bind Attributes based on BindingsHelper objects...
			List<BindingsHelper> helpers = Attributes.prepareBindings(
								new TaskRequestDecorator(req, res));
			for (BindingsHelper h : helpers) {
				n.put(h.getBindingName(), h);
			}
			
			ScriptContext ctx = new javax.script.SimpleScriptContext();
			ctx.setBindings(n, ScriptContext.GLOBAL_SCOPE);
			ctx.setBindings(new SimpleBindings(), ScriptContext.ENGINE_SCOPE);

			eng.eval(s, ctx);
			
			// Add the engine to the request attributes and invoke subtasks...
			StringBuffer key = new StringBuffer();
			key.append(ScriptAttributes.ENGINE).append(".")
					.append(eng.getFactory().getEngineName());
			res.setAttribute(key.toString(), eng);
			super.performSubtasks(req, res);

		} catch (Throwable t) {
			String msg = "Error while executing the specified script.  " +
							"\n\t\tENGINE_NAME:  " + eng.getFactory().getEngineName() +
							"\n\t\tSCRIPT (follows):\n" + s + "\n";
			throw new RuntimeException(msg, t);
		}

	}
	
}
