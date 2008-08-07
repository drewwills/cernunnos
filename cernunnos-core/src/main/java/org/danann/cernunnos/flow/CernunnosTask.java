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

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.danann.cernunnos.AttributePhrase;
import org.danann.cernunnos.Attributes;
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
import org.danann.cernunnos.runtime.ScriptRunner;

public final class CernunnosTask implements Task {

	// Instance Members.
	private Grammar grammar;
	private Phrase context;
	private Phrase location;
	private final Map<String,Task> loadedTasks = new HashMap<String,Task>();
	private ScriptRunner runner = null;
	
	/*
	 * Public API.
	 */

	public static final Reagent CONTEXT = new SimpleReagent("CONTEXT", "@context", ReagentType.PHRASE, String.class,
					"The context from which missing elements of the LOCATION can be inferred if it "
					+ "is relative.  The default is the value of the 'Attributes.ORIGIN' request attribute.",
					new AttributePhrase(Attributes.ORIGIN));

	public static final Reagent LOCATION = new SimpleReagent("LOCATION", "@location", ReagentType.PHRASE, String.class,
					"Location of a Cernunnos script.  May be a filesystem path (absolute or relative), or a URL.  If "
					+ "relative, the location will be evaluated from the CONTEXT.  If omitted, the value of the "
					+ "'Attributes.LOCATION' request attribute will be used.", new AttributePhrase(Attributes.LOCATION));

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {CONTEXT, LOCATION};
		final Formula rslt = new SimpleFormula(getClass(), reagents);
		return rslt;
	}

	public void init(EntityConfig config) {

		// Instance Members.
		this.grammar = config.getGrammar();
		this.runner = new ScriptRunner(grammar);
		this.context = (Phrase) config.getValue(CONTEXT);
		this.location = (Phrase) config.getValue(LOCATION);

	}

	public void perform(TaskRequest req, TaskResponse res) {

		String loc = (String) location.evaluate(req, res);

		try {

			// Choose a script...
			final URL ctx = new URL((String) context.evaluate(req, res));
			final URL crn = new URL(ctx, loc);
			
			// Choose a Task...
			final String taskPath = crn.toExternalForm();
			Task k = null;
			synchronized (loadedTasks) {
				if (loadedTasks.containsKey(taskPath)) {
					
					// Use what we have...
					k = loadedTasks.get(taskPath);

				} else {

					// Compile the Task at the specified location...
					k = runner.compileTask(taskPath);
					
					// NB:  For now we're going to limit the size of loadedTasks 
					// to 1 to prevent memory issues; 1 is enough for the 
					// majority of cases.
					loadedTasks.clear();

					// Add the newly-compiled Task to loadedTasks...
					loadedTasks.put(taskPath, k);
					
				}
			}

			// Run it...
			runner.run(k, req, res);

		} catch (Throwable t) {
			String msg = "Unable to invoke the specified script:  " + loc;
			throw new RuntimeException(msg, t);
		}

	}

}
