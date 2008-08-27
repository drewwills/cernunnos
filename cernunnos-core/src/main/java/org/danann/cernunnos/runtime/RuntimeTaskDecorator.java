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
import org.danann.cernunnos.ManagedException;
import org.danann.cernunnos.Task;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public final class RuntimeTaskDecorator implements Task {

	// Instance Members.
	private final Task enclosed;
	private final EntityConfig config;
	
	/*
	 * Public API.
	 */
	
	RuntimeTaskDecorator(Task enclosed, EntityConfig config) {
		
		// Assertions...
		if (enclosed == null) {
			String msg = "Argument 'enclosed' cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		if (config == null) {
			String msg = "Argument 'config' cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		
		// Instance Members.
		this.enclosed = enclosed;
		this.config = config;

	}

	public Formula getFormula() {
		throw new UnsupportedOperationException();
	}

	public void init(EntityConfig config) {
		throw new UnsupportedOperationException();
	}
	
	public void perform(TaskRequest req, TaskResponse res) {
		
		/*
		 * RuntimeTaskDecorator.perform() decorates the perform() method of 
		 * every Task in Cernunnos.  It has two responsibilities:
		 *   - (1) Manage the request attribute stack
		 *   - (2) Provide enhanced error information for all tasks
		 */
		
		// Manage the request attribute stack
		RuntimeRequestResponse rrr = (RuntimeRequestResponse) res;
		rrr.enclose(req);
		
		// Provide enhanced error information for all tasks
		try {
			enclosed.perform(rrr, new RuntimeRequestResponse());
		} catch (ManagedException me) {
			
			// Already processed...
			throw me;
			
		} catch (RuntimeException re) {
			
			// We're obligated to ensure there isn't 
			// already a ManagedException in the stack trace...
			for (Throwable cursor = re; cursor != null; cursor = cursor.getCause()) {
				if (cursor instanceof ManagedException) {
					throw re;
				}
			}
			
			throw new ManagedException(config, re);

		} catch (Throwable t) {
			
			// We're obligated to ensure there isn't 
			// already a ManagedException in the stack trace...
			for (Throwable cursor = t; cursor != null; cursor = cursor.getCause()) {
				if (cursor instanceof ManagedException) {
					throw new RuntimeException(t);
				}
			}
			
			throw new ManagedException(config, t);
		}

	}

}