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

import java.util.List;

import org.dom4j.Element;

/**
 * Signals that a reagent or grammar entry has been deprecated and provides 
 * relevant information.
 */
public interface Deprecation {
	
	/**
	 * Indicates when this item was deprecated.  The version <code>String</code> 
	 * takes the following standard format:  <i>MAJOR.MINOR.PATCH</i>. 
	 * 
	 * @return The Cernunnos release in which this item was first deprecated.
	 */
	String getVersion();

	/**
	 * Provides a textual description of why this item was deprecated and what 
	 * should be used instead of this item.  This description should be valid 
	 * XHTML, suitable for inclusion in reference documentation.
	 * 
	 * @return XHTML describing why this item is deprecated and what to use 
	 * instead.
	 */
	List<Element> getDescription();

}
