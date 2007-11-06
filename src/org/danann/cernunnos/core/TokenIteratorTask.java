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
import org.danann.cernunnos.Attributes;
import org.danann.cernunnos.AttributePhrase;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public class TokenIteratorTask extends AbstractContainerTask {

	// Instance Members.
	private Phrase string;
	private Phrase regex;

	/*
	 * Public API.
	 */

	public static final Reagent STRING = new SimpleReagent("STRING", "@string", ReagentType.PHRASE,
				String.class, "Textual content that will be split into tokens based on REGEX.",
				new AttributePhrase(Attributes.STRING));

	public static final Reagent REGEX = new SimpleReagent("REGEX", "@regex", ReagentType.PHRASE, String.class,
				"Delimiting regular expression used to split STRING into tokens.");

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {STRING, REGEX, AbstractContainerTask.SUBTASKS};
		final Formula rslt = new SimpleFormula(TokenIteratorTask.class, reagents);
		return rslt;
	}

	public void init(EntityConfig config) {

		super.init(config);

		// Instance Members.
		this.string = (Phrase) config.getValue(STRING);
		this.regex = (Phrase) config.getValue(REGEX);

	}

	public void perform(TaskRequest req, TaskResponse res) {

		String src = (String) string.evaluate(req, res);
		String exp = (String) regex.evaluate(req, res);
		String[] tokens = src.split(exp);

		for (String k : tokens) {
			res.setAttribute(Attributes.STRING, k);
			super.performSubtasks(req, res);
		}

	}
}
