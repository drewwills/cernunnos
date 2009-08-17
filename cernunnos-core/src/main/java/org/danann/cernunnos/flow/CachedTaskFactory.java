/*
 * Copyright 2009 the original author or authors
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

import org.danann.cernunnos.AbstractCacheHelperFactory;
import org.danann.cernunnos.Task;
import org.danann.cernunnos.runtime.ScriptRunner;

/**
 * Factory to create new Task instances
 */
public class CachedTaskFactory extends AbstractCacheHelperFactory<String, Task> {
    //Hide factory mutex to avoid unforseen sync problems
    private static final Object FACTORY_MUTEX = new Object();

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
