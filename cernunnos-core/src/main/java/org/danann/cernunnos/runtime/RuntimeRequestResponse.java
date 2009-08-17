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
import java.util.Map;
import java.util.Set;

import java.util.SortedSet;
import java.util.TreeSet;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public final class RuntimeRequestResponse implements TaskRequest, TaskResponse {

	// Instance Members.
	private TaskRequest parent;
	private final Map<String,Object> attributes;
	private Map<String,Object> mergedAttributes;

	/*
	 * Public API.
	 */

	public RuntimeRequestResponse() {

		// Instance members.
		this.parent = null;
		this.attributes = new HashMap<String,Object>();
		this.mergedAttributes = this.attributes;

	}
	
	public RuntimeRequestResponse(Map<String,Object> attributes) {
	    this();
	    
        // Assertions...
        if (attributes == null) {
            String msg = "Argument 'attributes' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        
         this.attributes.putAll(attributes);
	    
	}

	public void setAttribute(String name, Object value) {
		this.attributes.put(name, value);
		this.mergedAttributes.put(name, value);
	}

	public boolean hasAttribute(String name) {

		// Assertions...
		if (name == null) {
			String msg = "Argument 'name' cannot be null.";
			throw new IllegalArgumentException(msg);
		}

		return this.mergedAttributes.containsKey(name);

	}

	public Object getAttribute(String name) {

		// Assertions...
		if (name == null) {
			String msg = "Argument 'name' cannot be null.";
			throw new IllegalArgumentException(msg);
		}

		if (!this.mergedAttributes.containsKey(name)) {
			throw new IllegalArgumentException("The specified attribute is not defined:  " + name);
		}

        return this.mergedAttributes.get(name);
	}

	public Set<String> getAttributeNames() {
		return Collections.unmodifiableSet(this.mergedAttributes.keySet());
	}

	public Map<String,Object> getAttributes() {
		return Collections.unmodifiableMap(this.mergedAttributes);
	}

	SortedSet<String> getSortedAttributeNames() {
		return Collections.unmodifiableSortedSet(new TreeSet<String>(this.mergedAttributes.keySet()));
	}
	
	/*
	 * Package API.
	 */

	void enclose(TaskRequest req) {
		parent = req;
		if (this.parent != null) {
    		final HashMap<String, Object> mergedAttributes = new HashMap<String, Object>(this.parent.getAttributes());
    		mergedAttributes.putAll(this.attributes);
    		this.mergedAttributes = mergedAttributes;
		}
		else {
		    this.mergedAttributes = this.attributes;
		}
	}

}