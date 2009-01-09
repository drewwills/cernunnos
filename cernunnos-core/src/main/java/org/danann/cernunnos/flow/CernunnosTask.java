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

import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Grammar;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ResourceHelper;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.Task;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;
import org.danann.cernunnos.runtime.ScriptRunner;

public final class CernunnosTask implements Task {

	// Instance Members.
    private final ResourceHelper resource = new ResourceHelper();
	private final Map<String,Task> loadedTasks = new HashMap<String,Task>();
    private Grammar grammar;
	private ScriptRunner runner = null;
	
	/*
	 * Public API.
	 */

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {ResourceHelper.CONTEXT_SOURCE, ResourceHelper.LOCATION_TASK};
		final Formula rslt = new SimpleFormula(getClass(), reagents);
		return rslt;
	}

	public void init(EntityConfig config) {

		// Instance Members.
        resource.init(config);
		this.grammar = config.getGrammar();
		this.runner = new ScriptRunner(grammar);

	}

	public void perform(TaskRequest req, TaskResponse res) {

        final URL crn = resource.evaluate(req, res);
			
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

		try {
			// Run it...
			runner.run(k, req, res);
		} catch (Throwable t) {
			String msg = "Exception while to invoke the specified script:  " + crn.toExternalForm();
			throw new RuntimeException(msg, t);
		}

	}

}
