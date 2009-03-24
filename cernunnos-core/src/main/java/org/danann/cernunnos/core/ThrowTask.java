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

package org.danann.cernunnos.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.danann.cernunnos.AttributePhrase;
import org.danann.cernunnos.Attributes;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.ManagedException;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.Task;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public final class ThrowTask implements Task {
    protected final Log log = LogFactory.getLog(this.getClass());

	// Instance Members.
    private EntityConfig config;
	private Phrase exceptionPhrase;

	/*
	 * Public API.
	 */

    public static final Reagent EXCEPTION = new SimpleReagent("EXCEPTION", "@exception", ReagentType.PHRASE, Throwable.class,
            "The exception to throw. Will use the attribute named Attributes.EXCEPTION by default.", new AttributePhrase(Attributes.EXCEPTION));

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {EXCEPTION};
		final Formula rslt = new SimpleFormula(ThrowTask.class, reagents);
		return rslt;
	}

	public void init(EntityConfig config) {
	    
		// Instance Members.
		this.exceptionPhrase = (Phrase) config.getValue(EXCEPTION);
		this.config = config;
	}

	public void perform(TaskRequest req, TaskResponse res) {
	    final Throwable t = (Throwable) exceptionPhrase.evaluate(req, res);
	    if (this.log.isDebugEnabled()) {
	        this.log.debug("Retrieved Throwable " + t + " from request");
	    }
	    
	    if (t instanceof ManagedException) {
	        throw (ManagedException)t;
	    }
	    
	    throw new ManagedException(this.config, req, t);
	}
}