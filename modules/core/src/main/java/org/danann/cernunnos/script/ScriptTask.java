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

package org.danann.cernunnos.script;

import java.util.LinkedList;
import java.util.List;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.danann.cernunnos.AbstractContainerTask;
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
	private String engineName;
	private Phrase script;
	private final ScriptEngineManager mgr = new ScriptEngineManager();
	private final Log log = LogFactory.getLog(ScriptTask.class);	// Don't declare as static in general libraries

	/*
	 * Public API.
	 */

	public static final Reagent ENGINE_NAME = new SimpleReagent("ENGINE_NAME", "name(*[position() = 1])", ReagentType.STRING, String.class,
					"Name of the scripting engine to use -- e.g. 'groovy', 'jruby', 'javascript', etc.  Use a child element " +
					"named for the desired engine to designate this reagent (e.g. <groovy>, <js>).");

	public static final Reagent SCRIPT = new SimpleReagent("SCRIPT", "*[position() = 1]/text()", ReagentType.PHRASE, String.class,
					"Script content to execute.  Should be placed within a child element named for the script engine " +
					"(e.g. <groovy>, <js>).");

	public static final Reagent SUBTASKS = new SimpleReagent("SUBTASKS", "subtasks/*", ReagentType.NODE_LIST, List.class,
					"The set of tasks that are children of this task.", new LinkedList<Task>());

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {ENGINE_NAME, SCRIPT, SUBTASKS};
		final Formula rslt = new SimpleFormula(ScriptTask.class, reagents);
		return rslt;
	}

	public void init(EntityConfig config) {

		super.init(config);

		// Instance Members.
		this.engineName = (String) config.getValue(ENGINE_NAME);
		this.script = (Phrase) config.getValue(SCRIPT);

	}

	public void perform(TaskRequest req, TaskResponse res) {

		String s = (String) script.evaluate(req, res);

		try {

			ScriptEngine eng = mgr.getEngineByName(engineName);

			// ScriptEngineManager will return null if there's
			// no provider for the named platform...
			if (eng == null) {
				String msg = "Unable to locate the specified scripting engine:  "
																+ engineName;
				log.error(msg);
				throw new RuntimeException(msg);
			}

			Bindings n = new SimpleBindings();
			n.putAll(req.getAttributes());

			eng.eval(s, n);

// stuff...
ScriptContext ctx = eng.getContext();
List<Integer> scopes = ctx.getScopes();
for (Integer p : scopes) {
	Bindings b = ctx.getBindings(p);
	System.out.println("Bindings:  " + p);
	for (java.util.Map.Entry y : b.entrySet()) {
		System.out.println("\t" + y.getKey() + "=" + y.getValue());
	}
}

			super.performSubtasks(req, res);

		} catch (Throwable t) {
			String msg = "Error while executing the specified script.  " +
							"\n\t\tENGINE_NAME:  " + engineName +
							"\n\t\tSCRIPT (follows):\n" + s + "\n";
			throw new RuntimeException(msg, t);
		}

	}

}
