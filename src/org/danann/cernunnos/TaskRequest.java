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
 * Encapsulates input for a <code>Task</code>.
 */
public interface TaskRequest {

	/**
	 * Obtains the value of the named attribute, or throws 
	 * <code>IllegalArgumentException</code> if there is no attribute with the 
	 * specified name.
	 * 
	 * @param name The name of an existing attribute.
	 * @return The value of the attribute with the specified name.
	 */
	Object getAttribute(String name);
	
	/**
	 * Checks to see if the specified attribute is present on the request. 
	 * 
	 * @param name The name of an attribute that may exist.
	 * @return <code>true</code> if there is a value for the named attribute, 
	 * otherwise <code>false</code>.
	 */
	boolean hasAttribute(String name);
	
}
