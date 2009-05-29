/*
 * Copyright 2009 Andrew Wills
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

package org.danann.cernunnos.json;

import java.util.Map;

import net.sf.json.JSONObject;

import org.danann.cernunnos.Attributes;
import org.danann.cernunnos.BindingsHelper;

public final class JsonAttributes {

	static {
		Attributes.registerBindings("JsonAttributes", BindingsHelperImpl.class);
	}

	/*
	 * Public API.
	 */

    /**
     * Use this name to create a request attribute that refers to a
     * <code>net.sf.json.JSONObject</code>.
     */
	public static final String JSON = JsonAttributes.class.getSimpleName() + ".JSON";

	/*
	 * Nested Types.
	 */

	public static final class BindingsHelperImpl implements BindingsHelper {

		/*
		 * Public API.
		 */
		
		public final JSONObject JSON;
		
		public BindingsHelperImpl(Map<String,Object> bindings) {
			
			// Assertions.
			if (bindings == null) {
				String msg = "Argument 'bindings' cannot be null.";
				throw new IllegalArgumentException(msg);
			}
			
			// Instance Members.
			this.JSON = (JSONObject) bindings.get(JsonAttributes.JSON);
			
		}
		
		public String getBindingName() {
			return "JsonAttributes";
		}
		
	}

}
