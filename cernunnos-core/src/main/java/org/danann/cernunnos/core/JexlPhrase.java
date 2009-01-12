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

import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.JexlHelper;
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

public final class JexlPhrase implements Phrase {
    //Hide factory mutex to avoid unforseen sync problems
    private static final Object FACTORY_MUTEX = new Object();

	// Instance Members.
    private CacheHelper<String, Expression> expressionCache;
    private Phrase expression;
	
	/*
	 * Public API.
	 */
	
	public static final Reagent EXPRESSION = new SimpleReagent("EXPRESSION", "descendant-or-self::text()", 
									ReagentType.PHRASE, String.class, "A valid JEXL expression.");

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {CacheHelper.CACHE, CacheHelper.CACHE_MODEL, EXPRESSION};
		return new SimpleFormula(JexlPhrase.class, reagents);
	}
	
	public void init(EntityConfig config) {

		// Instance Members.
	    this.expressionCache = new DynamicCacheHelper<String, Expression>(config);
        this.expression = (Phrase) config.getValue(EXPRESSION);

	}

	public Object evaluate(TaskRequest req, TaskResponse res) {
		final String exp = (String) expression.evaluate(req, res);
		
		// Get or Create a JEXL expression object...
        final Expression e = this.expressionCache.getCachedObject(req, res, exp, CachableExpressionFactory.INSTANCE);
        
		try {
		    // Prepare the JEXL context...
		    JexlContext jc = JexlHelper.createContext();
		    jc.setVars(req.getAttributes());
		    
		    return e.evaluate(jc);
		} 
		catch (Throwable t) {
			throw new RuntimeException("Unable to evaluate the following JEXL expression:  " + exp, t);
		}
	}
	

    
    protected static final class CachableExpressionFactory extends AbstractCacheHelperFactory<String, Expression> {
        public static final CachableExpressionFactory INSTANCE = new CachableExpressionFactory();

        /* (non-Javadoc)
         * @see org.danann.cernunnos.cache.CacheHelper.Factory#createObject(java.lang.Object)
         */
        public Expression createObject(String key) {
            try {
                return ExpressionFactory.createExpression(key);
            }
            catch (Exception e) {
                throw new RuntimeException("Failed to create Expression for '" + key + "'", e);
            }
        }

        /* (non-Javadoc)
         * @see org.danann.cernunnos.CacheHelper.Factory#getMutex(java.lang.Object)
         */
        public Object getMutex(String key) {
            return FACTORY_MUTEX;
        }
    }
}