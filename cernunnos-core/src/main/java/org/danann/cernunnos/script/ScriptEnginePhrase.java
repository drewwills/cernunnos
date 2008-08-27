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

import javax.script.ScriptEngineManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
	
	// Instance Members.
	private Phrase engineName;
	private final ScriptEngineManager mgr = new ScriptEngineManager();
	private final Log log = LogFactory.getLog(ScriptEnginePhrase.class);	// Don't declare as static in general libraries

	/*
	 * Public API.
	 */
	
	public static final Reagent ENGINE_NAME = new SimpleReagent("ENGINE_NAME", "descendant-or-self::text()", ReagentType.PHRASE, 
					String.class, "Name of the scripting engine to use -- e.g. 'groovy', 'jruby', 'javascript', etc.");

	public ScriptEnginePhrase() {}
	
	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {ENGINE_NAME};
		final Formula rslt = new SimpleFormula(ScriptEnginePhrase.class, reagents);
		return rslt;
	}

	public void init(EntityConfig config) {

		// Instance Members.
		this.engineName = (Phrase) config.getValue(ENGINE_NAME);

	}

	public Object evaluate(TaskRequest req, TaskResponse res) {
		
		Object rslt = null;
		
		// Look for an engine under 'ScriptAttributes.ENGINE.{ENGINE_NAME}'
		String eName = (String) engineName.evaluate(req, res);
		StringBuffer key = new StringBuffer();
		key.append(ScriptAttributes.ENGINE).append(".").append(eName);
		if (req.hasAttribute(key.toString())) {

			// There is one, use it...
			rslt = req.getAttribute(key.toString());
			
		} else {
			
			// Create a new engine of the specified type...
			// ScriptEngineManager will return null if there's
			// no provider for the named platform...
			rslt = mgr.getEngineByName(eName);
			if (rslt == null) {
				String msg = "Unable to locate the specified scripting engine:  "
																	+ eName;
				log.error(msg);
				throw new RuntimeException(msg);
			}
			
		}
		
		return rslt;
		
	}
	
}