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

import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

@Deprecated
public final class IsNullPhrase implements Phrase {

	// Instance Members.
	private Phrase value;
	
	/*
	 * Public API.
	 */
	
	public static final Reagent VALUE = new SimpleReagent("VALUE", "descendant-or-self::text()", 
					ReagentType.PHRASE, Object.class, "A phrase that evaluates to a value.");

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {VALUE};
		return new SimpleFormula(getClass(), reagents);
	}
	
	public void init(EntityConfig config) {

		// Instance Members.
		this.value = (Phrase) config.getValue(VALUE);

	}

	public Object evaluate(TaskRequest req, TaskResponse res) {

		return value.evaluate(req, res) == null ? true : false;
		
	}
	
}
