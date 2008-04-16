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

import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Task;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public final class RuntimeTaskDecorator implements Task {

	// Instance Members.
	private final Task enclosed;
	
	/*
	 * Public API.
	 */
	
	RuntimeTaskDecorator(Task enclosed) {
		
		// Assertions...
		if (enclosed == null) {
			String msg = "Argument 'enclosed' cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		
		// Instance Members.
		this.enclosed = enclosed;

	}

	public Formula getFormula() {
		throw new UnsupportedOperationException();
	}

	public void init(EntityConfig config) {
		throw new UnsupportedOperationException();
	}
	
	public void perform(TaskRequest req, TaskResponse res) {
		RuntimeRequestResponse rrr = (RuntimeRequestResponse) res;
		rrr.enclose(req);
		enclosed.perform(rrr, new RuntimeRequestResponse());
	}

}