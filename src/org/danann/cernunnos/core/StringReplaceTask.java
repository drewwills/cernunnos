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
import org.danann.cernunnos.AttributePhrase;
import org.danann.cernunnos.Attributes;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public class StringReplaceTask extends AbstractContainerTask {

	// Instance Members.
	private Phrase string;
	private Phrase regex;
	private Phrase replacement;

	/*
	 * Public API.
	 */

	public static final Reagent STRING = new SimpleReagent("STRING", "@string", ReagentType.PHRASE, String.class, 
				"The String upon which replacement will be performed.  The default is the value of the "
				+ "'Attributes.STRING' request attribute.", new AttributePhrase(Attributes.STRING));

	public static final Reagent REGEX = new SimpleReagent("REGEX", "@regex", ReagentType.PHRASE, String.class, 
				"Regular expression to which STRING will be matched.  All occurances of REGEX will be "
				+ "replaced with REPLACEMENT.");

	public static final Reagent REPLACEMENT = new SimpleReagent("REPLACEMENT", "@replacement", ReagentType.PHRASE, 
				String.class, "Characters that will replace matches of REGEX.");

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {STRING, REGEX, REPLACEMENT, AbstractContainerTask.SUBTASKS};
		final Formula rslt = new SimpleFormula(StringReplaceTask.class, reagents);
		return rslt;
	}

	public void init(EntityConfig config) {

		super.init(config);

		// Instance Members.
		this.string = (Phrase) config.getValue(STRING); 
		this.regex = (Phrase) config.getValue(REGEX); 
		this.replacement = (Phrase) config.getValue(REPLACEMENT); 
		
	}

	public void perform(TaskRequest req, TaskResponse res) {
		
		String s = (String) string.evaluate(req, res);
		String x = (String) regex.evaluate(req, res);
		String r = (String) replacement.evaluate(req, res);
		
		res.setAttribute(Attributes.STRING, s.replaceAll(x, r));
		
		super.performSubtasks(req, res);
		
	}
	
}