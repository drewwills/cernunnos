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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public class TaskRequestDecorator implements TaskRequest {
	
	private final TaskRequest req;
	private final TaskResponse res;

	/*
	 * Public API.
	 */
	
	public TaskRequestDecorator(TaskRequest req, TaskResponse res) {
		
		// Assertions.
		if (req == null) {
			String msg = "Argument 'req' cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		if (res == null) {
			String msg = "Argument 'res' cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		
		this.req = req;
		this.res = res;

	}

	public Object getAttribute(String name) {
		if (name.equals(ScriptAttributes.REQUEST)) {
			return req;
		} else if (name.equals(ScriptAttributes.RESPONSE)) {
			return res;
		} else {
			return req.getAttribute(name);
		}		
	}

	public Set<String> getAttributeNames() {
		Set<String> rslt = new HashSet<String>();
		rslt.addAll(req.getAttributeNames());
		rslt.add(ScriptAttributes.REQUEST);
		rslt.add(ScriptAttributes.RESPONSE);
		return Collections.unmodifiableSet(rslt);
	}

	public Map<String, Object> getAttributes() {
		Map<String,Object> rslt = new HashMap<String,Object>();
		for (String s : getAttributeNames()) {
			rslt.put(s, getAttribute(s));
		}
		return Collections.unmodifiableMap(rslt);
	}

	public boolean hasAttribute(String name) {
		if (name.equals(ScriptAttributes.REQUEST) || name.equals(ScriptAttributes.RESPONSE)) {
			return true;
		} else {
			return req.hasAttribute(name);
		}
	}
	
}
