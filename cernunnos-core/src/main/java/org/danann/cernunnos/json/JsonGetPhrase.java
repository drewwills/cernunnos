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

public class JsonGetPhrase implements Phrase {

    // Instance Members.
    private Phrase json;
    private Phrase key;

    /*
     * Public API.
     */

    public static final Reagent JSON = new SimpleReagent("JSON", "@json", ReagentType.PHRASE, 
            JSONObject.class, "Optional instance of JSONObject from which to get the specified "
            + "element.  If omitted, the 'JsonAttributes.JSON' request attribute will be used.", 
            new AttributePhrase(JsonAttributes.JSON));

    public static final Reagent KEY = new SimpleReagent("KEY", "descendant-or-self::text()", 
            ReagentType.PHRASE, String.class, "Name of an element to get from JSON.");

    public Formula getFormula() {
        Reagent[] reagents = new Reagent[] { JSON, KEY };
        return new SimpleFormula(getClass(), reagents);
    }
    
    public void init(EntityConfig config) {

        // Instance Members.
        this.json = (Phrase) config.getValue(JSON);
        this.key = (Phrase) config.getValue(KEY);
        
    }

    public Object evaluate(TaskRequest req, TaskResponse res) {
        
        JSONObject j = (JSONObject) json.evaluate(req, res);
        String k = (String) key.evaluate(req, res);
        return j.get(k);
        
    }

}
