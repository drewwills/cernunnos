/*
 * Copyright 2009 Andrew Wills
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

package org.danann.cernunnos.json;

import net.sf.json.JSONObject;

import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public final class NewJsonPhrase implements Phrase {
	
	// Instance Members.
    private Phrase expression;

	/*
	 * Public API.
	 */

    public static final Reagent EXPRESSION = new SimpleReagent("EXPRESSION", "descendant-or-self::text()", 
                ReagentType.PHRASE, String.class, "Optional JSON expression from which to form a JSONObject.  "
            	+ "If omitted, an empty JSONObject will be returned.", null);

    public Formula getFormula() {
		Reagent[] reagents = new Reagent[] { EXPRESSION };
		return new SimpleFormula(getClass(), reagents);
	}
	
	public void init(EntityConfig config) {

		// Instance Members.
        this.expression = (Phrase) config.getValue(EXPRESSION);
		
	}

	public Object evaluate(TaskRequest req, TaskResponse res) {
	    
	    JSONObject rslt = null;
	    
	    if (expression != null) {
	        rslt = JSONObject.fromObject(expression.evaluate(req, res));
	    } else {
	        rslt = new JSONObject();
	    }

		return rslt;
		
	}
	
}