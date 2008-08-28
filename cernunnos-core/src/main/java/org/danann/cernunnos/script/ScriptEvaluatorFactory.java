/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.danann.cernunnos.script;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

import org.danann.cernunnos.CacheHelper;
import org.danann.cernunnos.Tuple;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
final class ScriptEvaluatorFactory implements CacheHelper.Factory<Tuple<ScriptEngine, String>, ScriptEvaluator> {
    public static final ScriptEvaluatorFactory INSTANCE = new ScriptEvaluatorFactory();

    /* (non-Javadoc)
     * @see org.danann.cernunnos.cache.CacheHelper.Factory#createObject(java.lang.Object)
     */
    public ScriptEvaluator createObject(Tuple<ScriptEngine, String> key) {
        return new ScriptEvaluator(key.first, key.second);
    }

    /* (non-Javadoc)
     * @see org.danann.cernunnos.cache.CacheHelper.Factory#isThreadSafe(java.lang.Object, java.lang.Object)
     */
    public boolean isThreadSafe(Tuple<ScriptEngine, String> key, ScriptEvaluator instance) {
        final ScriptEngineFactory factory = key.first.getFactory();
        final Object threadingAbility = factory.getParameter("THREADING");
        return threadingAbility != null;
    }
}