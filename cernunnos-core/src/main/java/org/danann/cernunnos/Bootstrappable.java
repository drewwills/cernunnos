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

/**
 * Signifies that the implementing class may be bootstrapped in the standard 
 * way.  Both <code>Task</code> and <code>Phrase</code> extend 
 * <code>Bootstrappable</code>.
 */
public interface Bootstrappable {

	/**
	 * Provides the formula necessary to bootstrap an instance of this task.  
	 * The reagents described in the formula will have corresponding entries on 
	 * the <code>SimpleEntityConfig</code> object provided to the <code>init</code> 
	 * method.
	 * 
	 * @return A blueprint for bootstrapping an instance of this task. 
	 */
	Formula getFormula();
	
	/**
	 * Called by the Cernunnos runtime to allow an instance of <code>Task</code> 
	 * to bootstrap its parameters.  
	 * 
	 * @param config Contains information about this task and the runtime 
	 * environment in which it will operate.
	 */
	void init(EntityConfig config);

}