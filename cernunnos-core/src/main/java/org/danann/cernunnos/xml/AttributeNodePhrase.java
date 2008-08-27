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

package org.danann.cernunnos.xml;

import org.dom4j.tree.FlyweightAttribute;

import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public final class AttributeNodePhrase implements Phrase {

	// Instance Members.
	private Phrase tuple;

	/*
	 * Public API.
	 */

	public static final Reagent TUPLE = new SimpleReagent("TUPLE", "descendant-or-self::text()", ReagentType.PHRASE, String.class,
					"String expression that must provide both an attribute name and value.  The first equals operator " +
					"('=') will be used as a separator;  characters to the left will be used as the name of the attribute, " +
					"characters to the right will be the value.");

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {TUPLE};
		return new SimpleFormula(AttributeNodePhrase.class, reagents);
	}

	public void init(EntityConfig config) {

		// Instance Members.
		this.tuple = (Phrase) config.getValue(TUPLE);

	}

	public Object evaluate(TaskRequest req, TaskResponse res) {

		String s = (String) tuple.evaluate(req, res);

		int index = s.indexOf("=");
		switch (index) {
			case -1:
				String msg = "The TUPLE expression must contain an equals ('=') " +
				"character.  The specified expression is invalid:  " + s;
				throw new IllegalArgumentException(msg);
			default:
				// All is well...
				String name = s.substring(0, index);
				String value = s.substring(index + 1);
				return new FlyweightAttribute(name, value);
		}

	}

}