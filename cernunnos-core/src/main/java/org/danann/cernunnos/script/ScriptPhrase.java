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
import javax.script.SimpleBindings;

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
	private Phrase engine;
	private Phrase expression;

	/*
	 * Public API.
	 */

	public static final Reagent ENGINE = new SimpleReagent("ENGINE", "@engine", ReagentType.PHRASE, ScriptEngine.class,
					"Optional instance of ScriptEngine that will be used to invoke SCRIPT.  If one is not provided, " +
					"a new one will be created.  The default is the value of the 'ScriptAttributes.ENGINE.{ENGINE_NAME}' " +
					"request attribute, if present.");

	public static final Reagent EXPRESSION = new SimpleReagent("EXPRESSION", "descendant-or-self::text()", ReagentType.PHRASE,
					String.class, "Script expression to evaluate.");

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {ENGINE, EXPRESSION};
		final Formula rslt = new SimpleFormula(ScriptPhrase.class, reagents);
		return rslt;
	}

	public void init(EntityConfig config) {

		// Instance Members.
		this.engine = (Phrase) config.getValue(ENGINE);
		this.expression = (Phrase) config.getValue(EXPRESSION);

	}

	public Object evaluate(TaskRequest req, TaskResponse res) {

		ScriptEngine eng = (ScriptEngine) engine.evaluate(req, res);
		String x = (String) expression.evaluate(req, res);
		try {

			Bindings n = new SimpleBindings();
			n.putAll(req.getAttributes());

			return eng.eval(x, n);

		} catch (Throwable t) {
			String msg = "Error while evaluating the specified script expression.  " +
							"\n\t\tENGINE_NAME:  " + eng.getFactory().getEngineName() +
							"\n\t\tEXPRESSION:  " + x;
			throw new RuntimeException(msg, t);
		}

	}

}
