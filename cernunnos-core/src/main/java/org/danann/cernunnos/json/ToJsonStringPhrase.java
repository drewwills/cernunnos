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

import org.danann.cernunnos.AttributePhrase;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public class ToJsonStringPhrase implements Phrase {

    // Instance Members.
    private Phrase json;

    /*
     * Public API.
     */

    public static final Reagent JSON = new SimpleReagent("JSON", "descendant-or-self::text()", 
                ReagentType.PHRASE, JSONObject.class, "Optional instance of JSONObject to "
                + "serialize into String form.  If omitted, the 'JsonAttributes.JSON' "
                + "request attribute will be used", new AttributePhrase(JsonAttributes.JSON));

    public Formula getFormula() {
        Reagent[] reagents = new Reagent[] { JSON };
        return new SimpleFormula(getClass(), reagents);
    }
    
    public void init(EntityConfig config) {

        // Instance Members.
        this.json = (Phrase) config.getValue(JSON);
        
    }

    public Object evaluate(TaskRequest req, TaskResponse res) {
        
        JSONObject j = (JSONObject) json.evaluate(req, res);
        return j.toString(4);
        
    }

}
