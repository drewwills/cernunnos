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

package org.danann.cernunnos.xml;

import org.danann.cernunnos.AttributePhrase;
import org.danann.cernunnos.Attributes;
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
import org.dom4j.Node;
import org.dom4j.XPath;

/**
 * <code>PhraseComponent</code> implementation that evaluates by performing an 
 * XPATH query on the NODE node and returns the textual value of the 
 * identified node(s).
 */
public final class ValueOfPhrase implements Phrase {

	// Instance Members.
    private CacheHelper<String, XPath> xpathCache;
    private Phrase source;
	private Phrase expression;

	/*
	 * Public API.
	 */

	public static final Reagent SOURCE = new SimpleReagent("NODE", "@source", ReagentType.PHRASE, Node.class,
					"Optional source node against which the EXPRESSION will be evaluated.  If not "
					+ "provided, the value of the 'Attributes.NODE' request attribute will be used.", 
					new AttributePhrase(Attributes.NODE));

	public static final Reagent EXPRESSION = new SimpleReagent("EXPRESSION", "descendant-or-self::text()", 
										ReagentType.PHRASE, String.class, "An XPath expression that describes a node.");

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {CacheHelper.CACHE, CacheHelper.CACHE_MODEL, SOURCE, EXPRESSION};
		return new SimpleFormula(ValueOfPhrase.class, reagents);
	}
	
	public void init(EntityConfig config) {

		// Instance Members.
	    this.xpathCache = new DynamicCacheHelper<String, XPath>(config);
		this.source = (Phrase) config.getValue(SOURCE); 
		this.expression = (Phrase) config.getValue(EXPRESSION); 
		
	}

	public Object evaluate(TaskRequest req, TaskResponse res) {

		Node src = (Node) source.evaluate(req, res);
		final String xpathExpresion = (String) expression.evaluate(req, res);
		final XPath xpath = this.xpathCache.getCachedObject(req, res, xpathExpresion, XPathCacheFactory.INSTANCE);
		try {
            xpath.setVariableContext(new RequestVariableContext(req));
            return xpath.valueOf(src);
        }
        finally {
            xpath.setVariableContext(null);
        }
		
	}
	
}