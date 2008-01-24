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

package org.danann.cernunnos.script;

public final class ScriptAttributes {

	/**
	 * Specifies the current <code>ScriptEngine</code> of a given type when 
	 * followed by the appropriate suffix.  E.g. 
	 * <code>ScriptAttributes.ENGINE.groovy</code> for groovy and 
	 * <code>ScriptAttributes.ENGINE.js</code> for JavaScript (rhino).  
	 */
	public static final String ENGINE = "ScriptAttributes.ENGINE";

}
