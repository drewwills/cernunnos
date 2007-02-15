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
 * Provides static access to some very common attribute names that facilitate 
 * elegant cooperation between <code>Task</code> implementations.
 */
public class Attributes {

	/**
	 * Use this name to create a request attribute representing the URL context 
	 * from which relative paths should be resolved.
	 */
	public static final String CONTEXT = "Attributes.CONTEXT";
	
	/**
	 * Use this name to create a request attribute representing the location of 
	 * a resource given the current CONTEXT.
	 */
	public static final String LOCATION = "Attributes.LOCATION";
	
	/**
	 * Use this name to create a request attribute containing an XML node.
	 */
	public static final String NODE = "Attributes.NODE";
	
}
