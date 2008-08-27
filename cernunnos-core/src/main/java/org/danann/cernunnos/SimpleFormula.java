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

package org.danann.cernunnos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * No-nonsense, immutable <code>Formula</code> implementation for use by task 
 * authors who don't need anything outlandish.
 */
public final class SimpleFormula implements Formula {
	
	// Instance Members.
	private final Class<?> impl;
	private final List<Reagent> reagents;
	private final Deprecation deprecation;
	
	/*
	 * Public API.
	 */

	/**
	 * Creates a new <code>SimpleFormula</code> with the specified class and reagents.  
	 * <code>Task</code> designers should pass a reference to their own 
	 * <code>Class</code> object as they define the formula for their task.
	 * 
	 * @param impl The Java class which this formula will govern.
	 * @param reagents The parameters necessary to bootstrap an instance of the 
	 * task defined by this formula.
	 */
	public SimpleFormula(Class<?> impl, Reagent[] reagents) {
		this(impl, reagents, null);
	}
	
	/**
	 * Creates a new <code>SimpleFormula</code> with the specified class and reagents.  
	 * <code>Task</code> designers should pass a reference to their own 
	 * <code>Class</code> object as they define the formula for their task.
	 * 
	 * @param impl The Java class which this formula will govern.
	 * @param reagents The parameters necessary to bootstrap an instance of the 
	 * task defined by this formula.
	 */
	public SimpleFormula(Class<?> impl, Reagent[] reagents, Deprecation deprecation) {

		// Assertions...
		if (impl == null) {
			String msg = "Argument 'impl' cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		if (reagents == null) {
			String msg = "Argument 'reagents' cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		// NB:  deprecation may be null...

		// Instance Members.
		this.impl = impl;
		List<Reagent> list = new ArrayList<Reagent>();
		list.addAll(Arrays.asList(reagents));
		this.reagents = Collections.unmodifiableList(list);
		this.deprecation = deprecation;
		
	}

	public Class<?> getImplementationClass() {
		return impl;
	}
	
	public List<Reagent> getReagents() {
		return reagents;
	}
	
	public boolean isDeprecated() {
		return deprecation != null;
	}
	
	public Deprecation getDeprecation() {				
		return deprecation;		
	}

}
