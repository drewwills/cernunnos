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

package org.danann.cernunnos.runtime;

import java.util.Collections;
import java.util.Map;

import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Grammar;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.EntityConfig;

public final class SimpleEntityConfig implements EntityConfig {

	// Instance Members.
	private final Grammar grammar;
	private final Formula formula;
	private final Map<Reagent,Object> mappings;
	
	/*
	 * Public API.
	 */

	public SimpleEntityConfig(Grammar grammar, Formula f, Map<Reagent,Object> mappings) {

		// Assertions...
		if (grammar == null) {
			String msg = "Argument 'grammar' cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		if (f == null) {
			String msg = "Argument 'f [Formula]' cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		if (mappings == null) {
			String msg = "Argument 'mappings' cannot be null.";
			throw new IllegalArgumentException(msg);
		}

		// Instance Members.
		this.grammar = grammar;
		this.formula = f;
		this.mappings = (Map<Reagent,Object>) Collections.unmodifiableMap(mappings);
		
	}
	
	public Grammar getGrammar() {
		return grammar;
	}
	
	public Formula getFormula() {
		return formula;
	}
	
	public Object getValue(Reagent r) {

		// Assertions...
		if (r == null) {
			String msg = "Argument 'r [Reagent]' cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		if (!formula.getReagents().contains(r)) {
			String msg = "This task does not define the specified reagent:  " 
														+ r.getXpath();
			throw new IllegalArgumentException(msg);
		}
		if (!mappings.keySet().contains(r)) {
			String msg = "No value is established for the specified reagent "
											+ "(error in construction).";
			throw new IllegalArgumentException(msg);
		}

		return mappings.get(r);
		
	}
	
	public Map<Reagent,Object> getValues() {
		return mappings;
	}
	
}