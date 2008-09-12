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

import org.danann.cernunnos.AttributePhrase;
import org.danann.cernunnos.Attributes;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.ReturnValueImpl;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.Task;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public final class CernunnosPhrase implements Phrase {

	// Instance Members.
	private Task task = new CernunnosTask();

	/*
	 * Public API.
	 */

	public static final Reagent LOCATION = new SimpleReagent("LOCATION", "descendant-or-self::text()", ReagentType.PHRASE, String.class,
					"Location of a Cernunnos script.  May be a filesystem path (absolute or relative), or a URL.  If "
					+ "relative, the location will be evaluated from the CONTEXT.  If omitted, the value of the "
					+ "'Attributes.LOCATION' request attribute will be used.", new AttributePhrase(Attributes.LOCATION));

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {CernunnosTask.CONTEXT, LOCATION};
		final Formula rslt = new SimpleFormula(CernunnosPhrase.class, reagents);
		return rslt;
	}

	public void init(EntityConfig config) {

		// Instance Members.
		this.task.init(config);
		

	}

	public Object evaluate(TaskRequest req, TaskResponse res) {

		ReturnValueImpl rslt = new ReturnValueImpl();
		res.setAttribute(Attributes.RETURN_VALUE, rslt);
		
		// BEWARE:  we're relying on the underlying Task object to shift the 
		// 'rslt' to the TaskRequest somehow;  currently this behavior will 
		// happen in all known circumstances.
		
		task.perform(req, res);
		return rslt.getValue();

	}
	
}
