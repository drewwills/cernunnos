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

import org.dom4j.DocumentFactory;

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

public final class CommentNodePhrase implements Phrase {
	
	// Instance Members.
	private Phrase value;
	private DocumentFactory fac = new DocumentFactory();

	/*
	 * Public API.
	 */

	public static final Reagent VALUE = new SimpleReagent("VALUE", "descendant-or-self::text()", ReagentType.PHRASE, String.class, 
					"String expression that will be converted into a camment node.  The default is the value of the "
					+ "'Attributes.STRING' request attribute.", new AttributePhrase(Attributes.STRING));

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {VALUE};
		return new SimpleFormula(CommentNodePhrase.class, reagents);
	}
	
	public void init(EntityConfig config) {

		// Instance Members.
		this.value = (Phrase) config.getValue(VALUE);
		
	}

	public Object evaluate(TaskRequest req, TaskResponse res) {

		String txt = (String) value.evaluate(req, res); 		
		return fac.createComment(txt);
		
	}
	
}