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

import java.util.List;

/**
 * Represents a blueprint for a <code>Task</code>.
 */
public interface Formula {
	
	/**
	 * Obtains the <code>Task</code> implementation governed by this formula. 
	 * 
	 * @return A Java class that implements <code>Task</code>.
	 */
	Class<?> getImplementationClass();
	
	/**
	 * Obtains a list of reagents necessary to bootstrap the <code>Task</code> 
	 * implementation represented by this formula. 
	 * 
	 * @return A collection of reagents.
	 */
	List<Reagent> getReagents();

	/**
	 * Indicates whether the <code>Task</code> or <code>Phrase</code> has been 
	 * deprecated.
	 * 
	 * @return <code>True</code> if this type is deprecated, otherwise 
	 * <code>False</code>.
	 */
	boolean isDeprecated();

	/**
	 * Provides details regarding the deprecation of this <code>Task</code> or 
	 * <code>Phrase</code> if applicable, otherwise <code>null</code>.
	 * 
	 * @return An object containing deprecation information or <code>null</code>.
	 */
	Deprecation getDeprecation();

}
