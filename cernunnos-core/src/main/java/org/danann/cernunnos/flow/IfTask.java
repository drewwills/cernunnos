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

package org.danann.cernunnos.flow;

import org.danann.cernunnos.AbstractContainerTask;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public final class IfTask extends AbstractContainerTask {

	// Instance Members.
	private Phrase test;

	/*
	 * Public API.
	 */

	public static final Reagent TEST = new SimpleReagent("TEST", "@test", ReagentType.PHRASE, Boolean.class,
				"Boolean value indicating whether SUBTASKS should be executed.");

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {TEST, AbstractContainerTask.SUBTASKS};
		final Formula rslt = new SimpleFormula(IfTask.class, reagents);
		return rslt;
	}

	public void init(EntityConfig config) {

		super.init(config);

		// Instance Members.
		this.test = (Phrase) config.getValue(TEST);

	}

	public void perform(TaskRequest req, TaskResponse res) {

		if (isApplicable(req, res)) {
		    this.performSubtasks(req, res);
		}

	}
	
	public boolean isApplicable(TaskRequest req, TaskResponse res) {
        return (Boolean) test.evaluate(req, res);
    }
	
	public void performSubtasks(TaskRequest req, TaskResponse res) {
	    super.performSubtasks(req, res);
	}

}
