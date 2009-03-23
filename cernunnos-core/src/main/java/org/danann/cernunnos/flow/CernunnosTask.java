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

import org.danann.cernunnos.AbstractCacheHelperFactory;
import org.danann.cernunnos.CacheHelper;
import org.danann.cernunnos.DynamicCacheHelper;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Grammar;
import org.danann.cernunnos.ManagedException;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.ResourceHelper;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.Task;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;
import org.danann.cernunnos.CacheHelper.Factory;
import org.danann.cernunnos.runtime.ScriptRunner;

public final class CernunnosTask implements Task {
    //Hide factory mutex to avoid unforseen sync problems
    private static final Object FACTORY_MUTEX = new Object();

	// Instance Members.
    private Factory<String, Task> taskFactory;
    private CacheHelper<String, Task> taskCache;
    private EntityConfig config;
    private final ResourceHelper resource = new ResourceHelper();
    private Grammar grammar;
	private ScriptRunner runner = null;
	private Phrase task;
	
	/*
	 * Public API.
	 */

    public static final Reagent TASK = new SimpleReagent("TASK", "@task", ReagentType.PHRASE, Task.class,
                        "Cernunnos Task to invoke.  Specify either LOCATION or TASK, but not both.", null);

    public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {CacheHelper.CACHE, CacheHelper.CACHE_MODEL, 
		            ResourceHelper.CONTEXT_SOURCE, ResourceHelper.LOCATION_TASK_NODEFAULT, 
		            TASK};
		final Formula rslt = new SimpleFormula(getClass(), reagents);
		return rslt;
	}

	public void init(EntityConfig config) {

		// Instance Members.
	    this.taskCache = new DynamicCacheHelper<String, Task>(config);
        resource.init(config);
		this.grammar = config.getGrammar();
		this.runner = new ScriptRunner(grammar);
		this.taskFactory = new CachedTaskFactory(this.runner);
		this.config = config;
		this.task = (Phrase) config.getValue(TASK);

	}

	public void perform(TaskRequest req, TaskResponse res) {
	    
	    // Figure out what to run here...
	    Task k = null;
	    if (resource.isSpecified(req, res)) {
	        // We go with LOCATION...
	        final URL crn = resource.evaluate(req, res);
	        final String taskPath = crn.toExternalForm();
	        k = this.taskCache.getCachedObject(req, res, taskPath, this.taskFactory);
	    } else {
	        // We go with TASK...
	        k = (Task) task.evaluate(req, res);
	    }

		try {
			// Run it...
			runner.run(k, req, res);
		} catch (Throwable t) {
			//Always throw a managed-exception so tracing multi-file calls is easier
			throw new ManagedException(this.config, req, t);
		}

	}

    /**
     * Factory to create new Task instances
     */
    protected static class CachedTaskFactory extends AbstractCacheHelperFactory<String, Task> {
        private final ScriptRunner runner;
        
        public CachedTaskFactory(ScriptRunner runner) {
            this.runner = runner;
        }

        /* (non-Javadoc)
         * @see org.danann.cernunnos.CacheHelper.Factory#createObject(java.lang.Object)
         */
        public Task createObject(String key) {
            return this.runner.compileTask(key);
        }

        /* (non-Javadoc)
         * @see org.danann.cernunnos.CacheHelper.Factory#getMutex(java.lang.Object)
         */
        public Object getMutex(String key) {
            return FACTORY_MUTEX;
        }

        /* (non-Javadoc)
         * @see org.danann.cernunnos.CacheHelper.Factory#isThreadSafe(java.lang.Object, java.lang.Object)
         */
        @Override
        public boolean isThreadSafe(String key, Task instance) {
            return true;
        }
    }
}
