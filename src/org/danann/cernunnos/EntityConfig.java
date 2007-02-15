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

import java.util.Map;

/**
 * Contains the information necessary for a <code>Task</code> to bootstrap 
 * itself.  The Cernunnos runtime is responsible for providing an appropriate 
 * <code>EntityConfig</code> instance to each task's <code>init</code> method.
 */
public interface EntityConfig {
	
	/**
	 * Provides a reference to the <code>Grammar</code> instance that is 
	 * responsible for creating this task.
	 * 
	 * @return The current grammar.
	 */
	Grammar getGrammar();
	
	/**
	 * Provides the <code>Formula</code> used by the Cernunnos runtime to 
	 * prepare parameters for this task.
	 * 
	 * @return The formula used to prepare this task.
	 */
	Formula getFormula();
	
	/**
	 * Obtains the current value for the specified <code>Reagent</code>.  Each 
	 * reagent will be of a type defined by the <code>ReagentType</code> 
	 * enumeration. 
	 * 
	 * @param r A reagent defined by this task.
	 * @return The value of the specified reagent.
	 */
	Object getValue(Reagent r);

	/**
	 * Obtains a collection of all the values held by this 
	 * <code>EntityConfig</code>. 
	 * 
	 * @return All known reagent values.
	 */
	Map<Reagent,Object> getValues();

}
