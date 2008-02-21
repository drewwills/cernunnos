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

public final class SetAttributeTask extends AbstractContainerTask {

	// Instance Members.
	private Phrase key;
	private Phrase value;

	/*
	 * Public API.
	 */

	public static final Reagent KEY = new SimpleReagent("KEY", "@key", ReagentType.PHRASE, String.class,
									"Name to be used for the new request attribute.");

	public static final Reagent VALUE = new SimpleReagent("VALUE", "@value", ReagentType.PHRASE, 
									Object.class, "Value of the request attribute.");

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {KEY, VALUE, AbstractContainerTask.SUBTASKS};
		final Formula rslt = new SimpleFormula(SetAttributeTask.class, reagents);
		return rslt;
	}

	public void init(EntityConfig config) {
		
		super.init(config);

		// Instance Members.
		this.key = (Phrase) config.getValue(KEY); 
		this.value = (Phrase) config.getValue(VALUE); 
		
	}

	public void perform(TaskRequest req, TaskResponse res) {

		res.setAttribute((String) key.evaluate(req, res), value.evaluate(req, res));
		
		super.performSubtasks(req, res);
		
	}
	
}