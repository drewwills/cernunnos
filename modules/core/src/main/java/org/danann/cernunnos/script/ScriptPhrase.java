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

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public class ScriptPhrase implements Phrase {

	// Instance Members.
	private Phrase engineName;
	private Phrase expression;
	private final ScriptEngineManager mgr = new ScriptEngineManager();
	private final Log log = LogFactory.getLog(ScriptPhrase.class);	// Don't declare as static in general libraries

	/*
	 * Public API.
	 */

	public static final Reagent ENGINE_NAME = new SimpleReagent("ENGINE_NAME", "@engine-name", ReagentType.PHRASE, String.class,
					"Name of the scripting engine to use -- e.g. 'groovy', 'jruby', 'javascript', etc.");

	public static final Reagent EXPRESSION = new SimpleReagent("EXPRESSION", "descendant-or-self::text()", ReagentType.PHRASE,
					String.class, "Script expression to evaluate.");

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {ENGINE_NAME, EXPRESSION};
		final Formula rslt = new SimpleFormula(ScriptPhrase.class, reagents);
		return rslt;
	}

	public void init(EntityConfig config) {

		// Instance Members.
		this.engineName = (Phrase) config.getValue(ENGINE_NAME);
		this.expression = (Phrase) config.getValue(EXPRESSION);

	}

	public Object evaluate(TaskRequest req, TaskResponse res) {

		String x = (String) expression.evaluate(req, res);
		String eName = (String) engineName.evaluate(req, res);

		try {

			ScriptEngine eng = mgr.getEngineByName(eName);

			// ScriptEngineManager will return null if there's
			// no provider for the named platform...
			if (eng == null) {
				String msg = "Unable to locate the specified scripting engine:  " + eName;
				log.error(msg);
				throw new RuntimeException(msg);
			}

			Bindings n = new SimpleBindings();
			n.putAll(req.getAttributes());

			return eng.eval(x, n);

		} catch (Throwable t) {
			String msg = "Error while evaluating the specified script expression.  " +
							"\n\t\tENGINE_NAME:  " + eName +
							"\n\t\tEXPRESSION:  " + x;
			throw new RuntimeException(msg, t);
		}

	}

}
