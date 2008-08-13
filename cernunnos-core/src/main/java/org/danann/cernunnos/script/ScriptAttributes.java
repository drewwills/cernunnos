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

import java.util.Map;
import javax.script.ScriptEngine;

import org.danann.cernunnos.Attributes;
import org.danann.cernunnos.BindingsHelper;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public final class ScriptAttributes {

	static {
		Attributes.registerBindings("ScriptAttributes", BindingsHelperImpl.class);
	}

	/*
	 * Public API.
	 */

	/**
	 * Specifies the current <code>ScriptEngine</code> of a given type when 
	 * followed by the appropriate suffix.  E.g. 
	 * <code>ScriptAttributes.ENGINE.groovy</code> for groovy and 
	 * <code>ScriptAttributes.ENGINE.js</code> for JavaScript (rhino).  
	 */
	public static final String ENGINE = ScriptAttributes.class.getSimpleName() + ".ENGINE";

	/**
	 * The current <code>TaskRequest</code> object  will be bound under this 
	 * name when a JSR-223 script executes.
	 */
	public static final String REQUEST = ScriptAttributes.class.getSimpleName() + ".REQUEST";

	/**
	 * The current <code>TaskResponse</code> object  will be bound under this 
	 * name when a JSR-223 script executes.
	 */
	public static final String RESPONSE = ScriptAttributes.class.getSimpleName() + ".RESPONSE";

	/*
	 * Nested Types.
	 */

	public static final class BindingsHelperImpl implements BindingsHelper {

		/*
		 * Public API.
		 */
		
		public final ScriptEngine ENGINE;
		public final TaskRequest REQUEST;
		public final TaskResponse RESPONSE;
		
		public BindingsHelperImpl(Map<String,Object> bindings) {
			
			// Assertions.
			if (bindings == null) {
				String msg = "Argument 'bindings' cannot be null.";
				throw new IllegalArgumentException(msg);
			}
			
			// Instance Members.
			this.ENGINE = (ScriptEngine) bindings.get(ScriptAttributes.ENGINE);
			this.REQUEST = (TaskRequest) bindings.get(ScriptAttributes.REQUEST);
			this.RESPONSE = (TaskResponse) bindings.get(ScriptAttributes.RESPONSE);
			
		}
		
		public String getBindingName() {
			return "ScriptAttributes";
		}
		
	}

}
