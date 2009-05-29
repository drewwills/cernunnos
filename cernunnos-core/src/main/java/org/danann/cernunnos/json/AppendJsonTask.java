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
import org.danann.cernunnos.Task;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public final class AppendJsonTask implements Task {

	// Instance Members.
    private Phrase key;
    private Phrase value;
	private Phrase parent;
//	private Phrase sibling;
    private Phrase expression;
//    private Phrase prepend;

	/*
	 * Public API.
	 */

    public static final Reagent KEY = new SimpleReagent("KEY", "@key", ReagentType.PHRASE, String.class,
                    "Name under which the specified JSON object or CONTENT will be added to the PARENT.");

    public static final Reagent VALUE = new SimpleReagent("VALUE", "@value", ReagentType.PHRASE, Object.class,
					"Optional value that will be added to PARENT.  Values may be any of the following types:  "
					+ "Boolean, Double, Integer, JSONArray, JSONObject, Long, String, or the JSONNull object.  "
					+ "If not provided, the 'JsonAttributes.JSON' request attribute will be used.", 
					new AttributePhrase(JsonAttributes.JSON));

	public static final Reagent PARENT = new SimpleReagent("PARENT", "@parent", ReagentType.PHRASE, JSONObject.class,
					"Optional JSONObject under which the specified content will be added.  Specify only PARENT or "
					+ "SIBLING, not both.  If neither is specified, the 'JsonAttributes.JSON' request attribute "
					+ "will be used as a PARENT.", new AttributePhrase(JsonAttributes.JSON));

//	public static final Reagent SIBLING = new SimpleReagent("SIBLING", "@sibling", ReagentType.PHRASE, JSONObject.class,
//					"Optional JSONObject next to which the specified content will be added.  Specify only PARENT or "
//					+ "SIBLING, not both.", null);

	public static final Reagent EXPRESSION = new SimpleReagent("EXPRESSION", "text()", ReagentType.PHRASE, String.class,
					"Optional JSON expression to append.  Use this reagent to specify content in-line.  If "
					+ "EXPRESSION is specified, it will be prefered over VALUE.", null);

//	public static final Reagent PREPEND = new SimpleReagent("PREPEND", "@prepend", ReagentType.PHRASE, 
//					Boolean.class, "Tells this task whether new content should be added before or after the "
//					+ "location specified by PARENT or SIBLING.  If true, content will be added before;  "
//				    + "the default is Boolean.FALSE.", new LiteralPhrase(Boolean.FALSE));

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {KEY, VALUE, PARENT, EXPRESSION};
		final Formula rslt = new SimpleFormula(getClass(), reagents);
		return rslt;
	}

	public void init(EntityConfig config) {

		// Instance Members.
        this.key = (Phrase) config.getValue(KEY);
        this.value = (Phrase) config.getValue(VALUE);
		this.parent = (Phrase) config.getValue(PARENT);
//		this.sibling = (Phrase) config.getValue(SIBLING);
		this.expression = (Phrase) config.getValue(EXPRESSION);
//		this.prepend = (Phrase) config.getValue(PREPEND);

	}

	public void perform(TaskRequest req, TaskResponse res) {

	    // Evaluate the key...
	    String k = (String) key.evaluate(req, res);
	    
		// Figure out where to put the content...
	    JSONObject p = (JSONObject) parent.evaluate(req, res);

		// Figure out what content to add...
	    Object o = null;
		if (expression != null) {
			o = JSONObject.fromObject(expression.evaluate(req, res));
		} else {
            o = value.evaluate(req, res);
		}

		p.element(k, o);

	}

}