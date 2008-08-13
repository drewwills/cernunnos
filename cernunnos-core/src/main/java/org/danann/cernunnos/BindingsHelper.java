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

package org.danann.cernunnos;

/**
 * Objects of type <code>BindingsHelper</code> work with Cernunnos attributes to 
 * solve the problem that many syntaxes do not support variable names with 
 * periods in them.  Using a <code>BindingsHelper</code>, the 
 * <code>Attributes.LOCATION</code> request parameter can be referenced with the 
 * Groovy (or JavaScript, etc.) expression 'Attributes.LOCATION'
 */
public interface BindingsHelper {
	
	/**
	 * Indicates the name under which this helper object should be bound.
	 * @return The bound name of this object
	 */
	public String getBindingName();

}
