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

import org.dom4j.XPath;

/**
 * Represents a configurable parameter of a task.  Reagents are specified by 
 * <code>Task</code> implementations, but their values are prepared by the 
 * Cernunnos runtime.
 */
public interface Reagent {
	
	/**
	 * Obtains the name of this <code>Reagent</code>.  Two <code>Reagent</code> 
	 * instances with the same name are logically equal. 
	 * 
	 * @return A descriptive name.
	 */
	String getName();
	
	/**
	 * Obtains the XPath expression used to caculate values for this 
	 * <code>Reagent</code>.
	 * 
	 * @return An valid XPath expression.
	 */
	XPath getXpath();
	
	/**
	 * Provides the type of this <code>Reagent</code> as defined by the 
	 * <code>ReagentType</code> enumeration.
	 * 
	 * @return A member of the <code>ReagentType</code> enumeration.
	 */
	ReagentType getReagentType();
	
	/**
	 * Provides the runtime data type that instances of this 
	 * <code>Reagent</code> must return.
	 * 
	 * @return A <code>Class</code> that matches the objects returned by this 
	 * reagent.
	 */
	Class<?> getExpectedType();
	
	/**
	 * Provides a description of how this <code>Reagent</code> will be used by 
	 * the task that specified it.
	 * 
	 * @return A textual description of this reagent.
	 */
	String getDescription();
	
	/**
	 * Indicates whether there is a default value for this <code>Reagent</code>.  
	 * If (and only if) there is a default, the XPath expression associated with 
	 * this reagent need not return a value.
	 * 
	 * @return <code>true</code> if there is a default value for this reagent, 
	 * otherwise <code>false</code>.
	 */
	boolean hasDefault();
	
	/**
	 * Obtains the default value for this <code>Reagent</code>, or throws a 
	 * <code>RuntimeException</code> if none is available.
	 * 
	 * @return The default value of this reagent.
	 */
	Object getDefault();

	/**
	 * Indicates whether the <code>Reagent</code> has been deprecated.
	 * 
	 * @return <code>True</code> if this reagent is deprecated, otherwise 
	 * <code>False</code>.
	 */
	boolean isDeprecated();

	/**
	 * Provides details regarding the deprecation of this <code>Reagent</code> 
	 * if applicable, otherwise <code>null</code>.
	 * 
	 * @return An object containing deprecation information or <code>null</code>.
	 */
	Deprecation getDeprecation();
		
}
