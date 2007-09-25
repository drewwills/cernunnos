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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

		return getAttributeNames().contains(name);

	}

	public Object getAttribute(String name) {

		// Assertions...
		if (name == null) {
			String msg = "Argument 'name' cannot be null.";
			throw new IllegalArgumentException(msg);
		}

		if (!attributes.containsKey(name)) {
			if (parent != null) {
				// We defer to the parent if two conditions are met:
				//   * The name isn't defined within this scope
				//   * There is a parent
				return parent.getAttribute(name);
			} else {
				String msg = "The specified attribute is not defined:  " + name;
				throw new IllegalArgumentException(msg);
			}
		} else {
			return attributes.get(name);
		}

	}

	public Set<String> getAttributeNames() {
		Set<String> rslt = new HashSet<String>();
		rslt.addAll(attributes.keySet());
		if (parent != null) {
			rslt.addAll(parent.getAttributeNames());
		}
		return Collections.unmodifiableSet(rslt);
	}

	public Map<String,Object> getAttributes() {
		Map<String,Object> rslt = new HashMap<String,Object>();
		for (String s : getAttributeNames()) {
			rslt.put(s, getAttribute(s));
		}
		return Collections.unmodifiableMap(rslt);
	}

	/*
	 * Package API.
	 */

	void enclose(TaskRequest req) {
		parent = req;
	}

}