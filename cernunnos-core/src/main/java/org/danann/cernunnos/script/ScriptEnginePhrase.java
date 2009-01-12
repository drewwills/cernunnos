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

package org.danann.cernunnos.script;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.danann.cernunnos.AbstractCacheHelperFactory;
import org.danann.cernunnos.CacheHelper;
import org.danann.cernunnos.DynamicCacheHelper;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public final class ScriptEnginePhrase implements Phrase {
    //Hide factory mutex to avoid unforseen sync problems
    private static final Object FACTORY_MUTEX = new Object();
    
    private static final Log LOG = LogFactory.getLog(ScriptEnginePhrase.class); // Don't declare as static in general libraries
    private static final ScriptEngineManager SCRIPT_ENGINE_MANAGER = new ScriptEngineManager();
    private static final CachedScriptEngineFactory cachedScriptEngineFactory = new CachedScriptEngineFactory();
	
	// Instance Members.
    private CacheHelper<String, ScriptEngine> scriptEngineCache;
	private Phrase engineName;

	/*
	 * Public API.
	 */
	
	public static final Reagent ENGINE_NAME = new SimpleReagent("ENGINE_NAME", "descendant-or-self::text()", ReagentType.PHRASE, 
					String.class, "Name of the scripting engine to use -- e.g. 'groovy', 'jruby', 'javascript', etc.");

	public ScriptEnginePhrase() {}
	
	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {CacheHelper.CACHE, CacheHelper.CACHE_MODEL, ENGINE_NAME};
		final Formula rslt = new SimpleFormula(ScriptEnginePhrase.class, reagents);
		return rslt;
	}

	public void init(EntityConfig config) {

		// Instance Members.
        this.scriptEngineCache = new DynamicCacheHelper<String, ScriptEngine>(config);

        this.engineName = (Phrase) config.getValue(ENGINE_NAME);

	}

	public Object evaluate(TaskRequest req, TaskResponse res) {
		ScriptEngine scriptEngine = null;
		
		// Look for an engine under 'ScriptAttributes.ENGINE.{ENGINE_NAME}'
		String eName = (String) engineName.evaluate(req, res);
		String engineAttributeKey = ScriptAttributes.ENGINE + "." + eName;
		if (req.hasAttribute(engineAttributeKey.toString())) {

			// There is one, use it...
			scriptEngine = (ScriptEngine)req.getAttribute(engineAttributeKey.toString());
			
		}
		// No attribute, try the cache
		else {
		    scriptEngine = this.scriptEngineCache.getCachedObject(req, res, eName, cachedScriptEngineFactory);
		}
		
		return scriptEngine;
	}

    protected final static class CachedScriptEngineFactory extends AbstractCacheHelperFactory<String, ScriptEngine> {

        /* (non-Javadoc)
         * @see org.danann.cernunnos.cache.CacheHelper.Factory#createObject(java.lang.Object)
         */
        public ScriptEngine createObject(String key) {
            
        	ScriptEngine scriptEngine = null;
        	try {
        	    scriptEngine = SCRIPT_ENGINE_MANAGER.getEngineByName(key);
        	} catch (Throwable t) {
        		final String msg = "SCRIPT_ENGINE_MANAGER could not get the specified engine:  " + key;
        		throw new RuntimeException(msg, t);
        	}
        	
            if (scriptEngine == null) {
                final RuntimeException re = new RuntimeException("Unable to locate the specified scripting engine:  " + key);
                LOG.error(re, re);
                throw re;
            }
            
            return scriptEngine;
            
        }

        /* (non-Javadoc)
         * @see org.danann.cernunnos.cache.CacheHelper.Factory#isThreadSafe(java.lang.Object, java.lang.Object)
         */
        @Override
        public boolean isThreadSafe(String key, ScriptEngine instance) {
            final ScriptEngineFactory factory = instance.getFactory();
            final Object threadingAbility = factory.getParameter("THREADING");
            return threadingAbility != null;
        }

        /* (non-Javadoc)
         * @see org.danann.cernunnos.CacheHelper.Factory#getMutex(java.lang.Object)
         */
        public Object getMutex(String key) {
            return FACTORY_MUTEX;
        }
    }
}
