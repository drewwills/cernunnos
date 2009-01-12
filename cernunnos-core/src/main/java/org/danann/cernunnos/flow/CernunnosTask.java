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
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ResourceHelper;
import org.danann.cernunnos.SimpleFormula;
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
    
    
    private final ResourceHelper resource = new ResourceHelper();
    private Grammar grammar;
	private ScriptRunner runner = null;
	
	/*
	 * Public API.
	 */

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {CacheHelper.CACHE, CacheHelper.CACHE_MODEL, ResourceHelper.CONTEXT_SOURCE, ResourceHelper.LOCATION_TASK};
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

	}

	public void perform(TaskRequest req, TaskResponse res) {

        final URL crn = resource.evaluate(req, res);
			
		// Choose a Task...
		final String taskPath = crn.toExternalForm();
		final Task k = this.taskCache.getCachedObject(req, res, taskPath, this.taskFactory);

		try {
			// Run it...
			runner.run(k, req, res);
		} catch (Throwable t) {
			String msg = "Exception while to invoke the specified script:  " + crn.toExternalForm();
			throw new RuntimeException(msg, t);
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
