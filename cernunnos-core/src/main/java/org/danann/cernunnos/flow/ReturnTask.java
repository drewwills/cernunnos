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

package org.danann.cernunnos.flow;

import org.danann.cernunnos.Attributes;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.ReturnValue;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.Task;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public final class ReturnTask implements Task {

	// Instance Members.
	private Phrase value;

	/*
	 * Public API.
	 */

	public static final Reagent VALUE = new SimpleReagent("VALUE", "@value", ReagentType.PHRASE, Object.class,
								"Object that will be returned by the current Cernunnos operation.");

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {VALUE};
		final Formula rslt = new SimpleFormula(ReturnTask.class, reagents);
		return rslt;
	}

	public void init(EntityConfig config) {

		// Instance Members.
		this.value = (Phrase) config.getValue(VALUE);

	}

	public void perform(TaskRequest req, TaskResponse res) {

		ReturnValue rslt = (ReturnValue) req.getAttribute(Attributes.RETURN_VALUE);
		rslt.setValue(value.evaluate(req, res));

	}

}