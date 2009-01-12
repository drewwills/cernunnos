/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.danann.cernunnos.script;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

import org.danann.cernunnos.AbstractCacheHelperFactory;
import org.danann.cernunnos.Tuple;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
final class ScriptEvaluatorFactory extends AbstractCacheHelperFactory<Tuple<ScriptEngine, String>, ScriptEvaluator> {
    //Hide factory mutex to avoid unforseen sync problems
    private static final Object FACTORY_MUTEX = new Object();
    
    public static final ScriptEvaluatorFactory INSTANCE = new ScriptEvaluatorFactory();
    
    private ScriptEvaluatorFactory() {
    }

    /* (non-Javadoc)
     * @see org.danann.cernunnos.cache.CacheHelper.Factory#createObject(java.lang.Object)
     */
    public ScriptEvaluator createObject(Tuple<ScriptEngine, String> key) {
        return new ScriptEvaluator(key.first, key.second);
    }

    /* (non-Javadoc)
     * @see org.danann.cernunnos.cache.CacheHelper.Factory#isThreadSafe(java.lang.Object, java.lang.Object)
     */
    @Override
    public boolean isThreadSafe(Tuple<ScriptEngine, String> key, ScriptEvaluator instance) {
        final ScriptEngineFactory factory = key.first.getFactory();
        final Object threadingAbility = factory.getParameter("THREADING");
        return threadingAbility != null;
    }

    /* (non-Javadoc)
     * @see org.danann.cernunnos.CacheHelper.Factory#getMutex(java.lang.Object)
     */
    public Object getMutex(Tuple<ScriptEngine, String> key) {
        return FACTORY_MUTEX;
    }
}