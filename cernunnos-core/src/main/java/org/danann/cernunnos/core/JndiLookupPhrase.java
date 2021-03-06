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

import javax.naming.Context;
import javax.naming.InitialContext;

import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public final class JndiLookupPhrase implements Phrase {

	// Instance Members.
	private Phrase expression;

	/*
	 * Public API.
	 */

	public static final Reagent EXPRESSION = new SimpleReagent("EXPRESSION", "descendant-or-self::text()",
								ReagentType.PHRASE, String.class, "Retrieves the named object.");

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {EXPRESSION};
		return new SimpleFormula(JndiLookupPhrase.class, reagents);
	}

	public void init(EntityConfig config) {

		// Instance Members.
		this.expression = (Phrase) config.getValue(EXPRESSION);

	}

	public Object evaluate(TaskRequest req, TaskResponse res) {

		String s = (String) expression.evaluate(req, res);
		try {
            Context ctx = new InitialContext();
			return ctx.lookup(s);
		} catch (Throwable t) {
			String msg = "Unable to lookup specified object:  " + s;
			throw new RuntimeException(msg, t);
		}

	}

}
