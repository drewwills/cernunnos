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

package org.danann.cernunnos.runtime;

import java.util.HashMap;
import java.util.Map;

import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public final class RuntimeRequestResponse implements TaskRequest, TaskResponse {

	// Instance Members.
	private TaskRequest parent;
	private Map<String,Object> attributes;
	
	/*
	 * Public API.
	 */
	
	public RuntimeRequestResponse() {
		
		// Instance members.
		this.parent = null;
		this.attributes = new HashMap<String,Object>();
		
	}
	
	public void setAttribute(String name, Object value) {
		attributes.put(name, value);
	}

	public boolean hasAttribute(String name) {

		// Assertions...
		if (name == null) {
			String msg = "Argument 'name' cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		
		return attributes.containsKey(name);

	}
	
	public Object getAttribute(String name) {
		
		// Assertions...
		if (name == null) {
			String msg = "Argument 'name' cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		
		Object rslt = attributes.get(name);

		// We defer to the parent if two conditions are met:
		//   * Our value is null
		//   * There is a parent
		if (rslt == null && parent != null) {
			rslt = parent.getAttribute(name);
		}

		// Throw an exception if we still don't have a value...
		if (rslt == null) {
			String msg = "The specified attribute is not defined:  " + name;
			throw new IllegalArgumentException(msg);
		}
		
		return rslt;
		
	}

	/*
	 * Package API.
	 */

	public void enclose(TaskRequest req) {
		parent = req;
	}

}