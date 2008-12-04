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

import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.ManagedException;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public final class RuntimePhraseDecorator implements Phrase {

	// Instance Members.
	private final Phrase enclosed;
	private final EntityConfig config;

	/*
	 * Public API.
	 */

	public RuntimePhraseDecorator(Phrase enclosed, EntityConfig config) {

		// Assertions...
		// NB:  'enclosed' may be null.
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

	public Object evaluate(TaskRequest req, TaskResponse res) {

		/*
		 * RuntimePhraseDecorator.evaluate() decorates the evaluate() method of 
		 * all Phrase instances.  It has two responsibilities:
		 *   - (1) Provide enhanced error information for all phrases
		 */

		Object rslt = null;

		// Manage the request attribute stack
        RuntimeRequestResponse rrr = (RuntimeRequestResponse) res;
        rrr.enclose(req);
        
		// Provide enhanced error information for all phrases
		try {
			rslt = enclosed.evaluate(rrr, new RuntimeRequestResponse());
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
			
			throw new ManagedException(config, rrr, re);

		} catch (Throwable t) {
			
			// We're obligated to ensure there isn't 
			// already a ManagedException in the stack trace...
			for (Throwable cursor = t; cursor != null; cursor = cursor.getCause()) {
				if (cursor instanceof ManagedException) {
					throw new RuntimeException(t);
				}
			}
			
			throw new ManagedException(config, rrr, t);
		}

		return rslt;

	}

}
