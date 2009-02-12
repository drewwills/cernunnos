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

import java.util.List;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.danann.cernunnos.AbstractContainerTask;
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
import org.danann.cernunnos.Tuple;

public class ScriptTask extends AbstractContainerTask {

	// Instance Members.
    private CacheHelper<Tuple<ScriptEngine, String>, ScriptEvaluator> scriptEvaluatorCache;
    private Phrase engine;
	private Phrase script;

	/*
	 * Public API.
	 */

	public static final Reagent ENGINE = new SimpleReagent("ENGINE", "@engine", ReagentType.PHRASE, ScriptEngine.class,
					"Optional instance of ScriptEngine that will be used to invoke SCRIPT.  If one is not provided, " +
					"a new one will be created.  The default is the value of the 'ScriptAttributes.ENGINE.{ENGINE_NAME}' " +
					"request attribute, if present.");

	public static final Reagent SCRIPT = new SimpleReagent("SCRIPT", "script/text()", ReagentType.PHRASE, String.class,
					"Script content to execute.  Must be placed within a child <script> element.");

	public static final Reagent SUBTASKS = new SimpleReagent("SUBTASKS", "subtasks/*", ReagentType.NODE_LIST, List.class,
					"The set of tasks that are children of this task.", AbstractContainerTask.SUPPRESS_EMPTY_SUBTASKS_WARNINGS);

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {CacheHelper.CACHE, CacheHelper.CACHE_MODEL, ENGINE, SCRIPT, SUBTASKS};
		final Formula rslt = new SimpleFormula(getClass(), reagents);
		return rslt;
	}

	@Override
    public void init(EntityConfig config) {
		super.init(config);

		// Instance Members.
		this.engine = (Phrase) config.getValue(ENGINE);
		this.script = (Phrase) config.getValue(SCRIPT);
        this.scriptEvaluatorCache = new DynamicCacheHelper<Tuple<ScriptEngine, String>, ScriptEvaluator>(config);

	}

	public void perform(TaskRequest req, TaskResponse res) {
        final ScriptEngine engine = (ScriptEngine) this.engine.evaluate(req, res);
        final String script = (String) this.script.evaluate(req, res);
        
        final Tuple<ScriptEngine, String> scriptEvaluatorKey = new Tuple<ScriptEngine, String>(engine, script);
        final ScriptEvaluator scriptEvaluator = this.scriptEvaluatorCache.getCachedObject(req, res, scriptEvaluatorKey, ScriptEvaluatorFactory.INSTANCE);
        
        final Bindings bindings = ScriptUtils.generateBindings(req, res);
        
        final ScriptContext scriptContext = new javax.script.SimpleScriptContext();
        scriptContext.setBindings(bindings, ScriptContext.GLOBAL_SCOPE);
        scriptContext.setBindings(new SimpleBindings(), ScriptContext.ENGINE_SCOPE);

        final ScriptEngineFactory scriptEngineFactory = engine.getFactory();
        final String engineName = scriptEngineFactory.getEngineName();
        try {
            scriptEvaluator.eval(scriptContext);
        }
        catch (ScriptException se) {
            throw new RuntimeException("Error while executing the specified script.  " +
                    "\n\t\tENGINE_NAME:  " + engineName +
                    "\n\t\tSCRIPT (follows):\n" + script + "\n", se);
        }
        
        res.setAttribute(ScriptAttributes.ENGINE + "." + engineName, engine);
        super.performSubtasks(req, res);
	}
}
