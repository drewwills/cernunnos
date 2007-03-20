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

import org.dom4j.Node;

import org.danann.cernunnos.AttributePhrase;
import org.danann.cernunnos.Attributes;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public final class SingleNodePhrase  implements Phrase {

	// Instance Members.
	private Phrase source;
	private Phrase expression;

	/*
	 * Public API.
	 */

	public static final Reagent SOURCE = new SimpleReagent("NODE", "@source", ReagentType.PHRASE, Node.class,
					"Optional source node against which the EXPRESSION will be evaluated.  If not "
					+ "provided, the value of the 'Attributes.NODE' request attribute will be used.", 
					new AttributePhrase(Attributes.NODE));

	public static final Reagent EXPRESSION = new SimpleReagent("LOCATION", "descendant-or-self::text()", 
					ReagentType.PHRASE, String.class, "An XPATH expression.");

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {SOURCE, EXPRESSION};
		return new SimpleFormula(SingleNodePhrase.class, reagents);
	}
	
	public void init(EntityConfig config) {

		// Instance Members.
		this.source = (Phrase) config.getValue(SOURCE); 
		this.expression = (Phrase) config.getValue(EXPRESSION); 
		
	}

	public Object evaluate(TaskRequest req, TaskResponse res) {

		Node src = (Node) source.evaluate(req, res);
		return src.selectSingleNode((String) expression.evaluate(req, res));
		
	}
	
}