/*
 * Copyright 2008 Eric Dalquist
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

package org.danann.cernunnos.cache;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.danann.cernunnos.AbstractCacheHelperFactory;
import org.danann.cernunnos.AbstractContainerTask;
import org.danann.cernunnos.Attributes;
import org.danann.cernunnos.CacheHelper;
import org.danann.cernunnos.DynamicCacheHelper;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.LiteralPhrase;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.ReturnValue;
import org.danann.cernunnos.ReturnValueImpl;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.Task;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

/**
 * Caches the results of execting a phrase using a specified cache key and binds the result to an attribute accessible
 * to request attributes.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public final class CacheTask extends AbstractContainerTask {

	// Instance Members.
    private final Object factoryMutex = new Object();
    private CacheHelper<Serializable, Object> cache;
    private Phrase keyPhrase;
	private Phrase cacheKeyPhrase;
	private Phrase threadSafePhrase;
	private Phrase factoryPhrase;
    private List<Task> factoryTasks;
	
	/*
	 * Public API.
	 */

	public static final Reagent KEY = new SimpleReagent("KEY", "@key", ReagentType.PHRASE, String.class,
                "Attribute name the cached object will be bound to for subtasks.");

	public static final Reagent CACHE_KEY = new SimpleReagent("CACHE_KEY", "@cache-key", ReagentType.PHRASE, Serializable.class,
                "The cache key to use for caching the object.");
	
	public static final Reagent THREAD_SAFE = new SimpleReagent("TRHEAD_SAFE", "@thread-safe", ReagentType.PHRASE, String.class,
                "If the cached object is thread-safe or not. Defaults to false.",
                new LiteralPhrase("false"));
	
    public static final Reagent FACTORY = new SimpleReagent("FACTORY", "@factory", ReagentType.PHRASE, Object.class,
                "The Phrase to execute if the object isn't in the cache.", new LiteralPhrase(null));
    
    public static final Reagent SUBTASKS = new SimpleReagent("SUBTASKS", "subtasks/*", ReagentType.NODE_LIST, List.class,
                "The set of tasks that are children of this cache task.", new LinkedList<Task>());

    public static final Reagent FACTORY_TASKS = new SimpleReagent("FACTORY_TASKS", "factory/*", ReagentType.NODE_LIST, List.class,
                "The Tasks to execute if the object isn't in the cache. Specifying factory tasks will override the factory attribute", 
                new LinkedList<Task>());

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {CacheHelper.CACHE, CacheHelper.CACHE_MODEL, KEY, CACHE_KEY, THREAD_SAFE, FACTORY, SUBTASKS, FACTORY_TASKS};
		final Formula rslt = new SimpleFormula(getClass(), reagents);
		return rslt;
	}

	@Override
    public void init(EntityConfig config) {
	    super.init(config);        
        
		// Instance Members.
	    this.cache = new DynamicCacheHelper<Serializable, Object>(config);
	    this.keyPhrase = (Phrase) config.getValue(KEY);
        this.cacheKeyPhrase = (Phrase) config.getValue(CACHE_KEY);
        this.threadSafePhrase = (Phrase) config.getValue(THREAD_SAFE);
        this.factoryPhrase = (Phrase) config.getValue(FACTORY);
        this.factoryTasks = this.loadSubtasks(config, FACTORY_TASKS, false);
	}

	public void perform(TaskRequest req, TaskResponse res) {
	    final Serializable cacheKey = (Serializable)this.cacheKeyPhrase.evaluate(req, res);
	    final Object rslt = this.cache.getCachedObject(req, res, cacheKey, new SubtaskCachedObjectFactory(req, res));
	    
        res.setAttribute((String) this.keyPhrase.evaluate(req, res), rslt);
        super.performSubtasks(req, res);
	}

	private final class SubtaskCachedObjectFactory extends AbstractCacheHelperFactory<Serializable, Object> {
	    private final TaskRequest req;
	    private final TaskResponse res;
	    
        public SubtaskCachedObjectFactory(TaskRequest req, TaskResponse res) {
            this.req = req;
            this.res = res;
        }

        /* (non-Javadoc)
         * @see org.danann.cernunnos.CacheHelper.Factory#createObject(java.lang.Object)
         */
        public Object createObject(Serializable key) {
            //If there are tasks use them
            if (CacheTask.this.factoryTasks != null && CacheTask.this.factoryTasks.size() > 0) {
                //Handling to make sure we don't squash an existing RETURN_VALUE attribute
                //TODO there is probably a better way to do this but I don't know it
                final ReturnValue existingReturnValue;
                if (this.req.hasAttribute(Attributes.RETURN_VALUE)) {
                    existingReturnValue = (ReturnValue) this.req.getAttribute(Attributes.RETURN_VALUE);
                }
                else {
                    existingReturnValue = null;
                }
                
                try {
                    final ReturnValueImpl rslt = new ReturnValueImpl();
                    this.res.setAttribute(Attributes.RETURN_VALUE, rslt);
    
                    CacheTask.this.performSubtasks(this.req, this.res, CacheTask.this.factoryTasks);
                    
                    return rslt.getValue();
                }
                finally {
                    this.res.setAttribute(Attributes.RETURN_VALUE, existingReturnValue);
                }
            }
            
            //No tasks, use the phrase
            return CacheTask.this.factoryPhrase.evaluate(this.req, this.res);
        }

        /* (non-Javadoc)
         * @see org.danann.cernunnos.CacheHelper.Factory#isThreadSafe(java.lang.Object, java.lang.Object)
         */
        @Override
        public boolean isThreadSafe(Serializable key, Object instance) {
            return Boolean.valueOf((String) CacheTask.this.threadSafePhrase.evaluate(this.req, this.res));
        }

        /* (non-Javadoc)
         * @see org.danann.cernunnos.CacheHelper.Factory#getMutex(java.lang.Object)
         */
        public Object getMutex(Serializable key) {
            return CacheTask.this.factoryMutex;
        }
	}
}
