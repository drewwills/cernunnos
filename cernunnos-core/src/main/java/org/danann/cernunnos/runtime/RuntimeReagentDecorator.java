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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public final class RuntimeReagentDecorator implements Phrase {

	// Instance Members.
	private final Phrase enclosed;
	private final Reagent reagent;
	private final Log log = LogFactory.getLog(RuntimeReagentDecorator.class);	// Don't declare as static in general libraries

	/*
	 * Public API.
	 */

	public RuntimeReagentDecorator(Phrase enclosed, Reagent r) {

		// Assertions...
		// NB:  'enclosed' may be null.
		if (r == null) {
			String msg = "Argument 'r [Reagent]' cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		
		// Instance Members.
		this.enclosed = enclosed;
		this.reagent = r;

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
		 * all Phrase instances.  It has the following responsibilities:
		 *   - (1) WARN if the response type doesn't match the expected type
		 */

		Object rslt = enclosed.evaluate(req, res);

		// WARN if the response type doesn't match the expected type
		if (rslt != null && log.isWarnEnabled() && !reagent.getExpectedType().isAssignableFrom(rslt.getClass())) {
			StringBuffer msg = new StringBuffer();
			msg.append("POSSIBLE SCRIPT ERROR:  A Phrase returned an unexpected type.")
					.append("\n\t\tReagent Name:  ").append(reagent.getName())
					.append("\n\t\tXPath:  ").append(reagent.getXpath())
					.append("\n\t\tExpected Type:  ").append(reagent.getExpectedType().getName())
					.append("\n\t\tActual Type:  ").append(rslt.getClass().getName()).append("\n");
			log.warn(msg.toString());
		}

		return rslt;

	}

}
