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

import java.net.URL;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;

import org.danann.cernunnos.CurrentDirectoryUrlPhrase;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Grammar;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.Task;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public final class CernunnosTask implements Task {

	// Instance Members.
	private Grammar grammar;
	private Phrase context;
	private Phrase location;

	/*
	 * Public API.
	 */

	public static final Reagent CONTEXT = new SimpleReagent("CONTEXT", "@context", ReagentType.PHRASE, String.class, 
					"The context from which missing elements of the LOCATION can be inferred if it "
					+ "is relative.  The default is a URL representing the filesystem location from which "
					+ "Java is executing.", new CurrentDirectoryUrlPhrase());

	public static final Reagent LOCATION = new SimpleReagent("LOCATION", "@location", ReagentType.PHRASE, String.class,
					"Location of a Cernunnos script.  May be a filesystem path (absolute or relative), or a URL.");

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {CONTEXT, LOCATION};
		final Formula rslt = new SimpleFormula(CernunnosTask.class, reagents);
		return rslt;
	}

	public void init(EntityConfig config) {

		// Instance Members.
		this.grammar = config.getGrammar();
		this.context = (Phrase) config.getValue(CONTEXT); 
		this.location = (Phrase) config.getValue(LOCATION); 
		
	}

	public void perform(TaskRequest req, TaskResponse res) {

		String loc = (String) location.evaluate(req, res);

		try {
			URL ctx = new URL((String) context.evaluate(req, res));
			URL crn = new URL(ctx, loc);
			Document doc = new SAXReader().read(crn.openStream());
			Task k = grammar.newTask(doc.getRootElement(), this);
			k.perform(req, res);
		} catch (Throwable t) {
			String msg = "Unable to invoke the specified script:  " + loc;
			throw new RuntimeException(msg, t);
		}
		
	}
	
}
