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

package org.danann.cernunnos.core;

import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.LiteralPhrase;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public final class SequencePhrase implements Phrase {

	// Instance Members.
	private Phrase sequenceName;

	/*
	 * Public API.
	 */
	
	public static final Reagent SEQUENCE_NAME = new SimpleReagent("SEQUENCE_NAME", "@sequence-name", ReagentType.PHRASE, 
				String.class, "Optional name for the sequence created by this task.  If omitted, the name " +
				"'SequenceTask.DEFAULT_SEQUENCE_NAME' will be used.", new LiteralPhrase("SequenceTask.DEFAULT_SEQUENCE_NAME"));

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {SEQUENCE_NAME};
		final Formula rslt = new SimpleFormula(getClass(), reagents);
		return rslt;
	}

	public void init(EntityConfig config) {

		// Instance Members.
		this.sequenceName = (Phrase) config.getValue(SEQUENCE_NAME);

	}

	public Object evaluate(TaskRequest req, TaskResponse res) {
		
		String n = (String) sequenceName.evaluate(req, res);
		SequenceTask.SequenceImpl seq = (SequenceTask.SequenceImpl) req.getAttribute(n);
		return seq.next();
		
	}

}
