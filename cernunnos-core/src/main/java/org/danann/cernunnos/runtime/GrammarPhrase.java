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

package org.danann.cernunnos.runtime;

import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Grammar;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public class GrammarPhrase implements Phrase {

	private Grammar grammar;
	
	/*
	 * Public API.
	 */
	
	public Formula getFormula() {
		return new SimpleFormula(getClass(), new Reagent[0]);
	}
	
	public void init(EntityConfig config) {
		
		// Instance Members.
		this.grammar = config.getGrammar();
		
	}

	public Object evaluate(TaskRequest req, TaskResponse res) {
		return grammar;
	}

}