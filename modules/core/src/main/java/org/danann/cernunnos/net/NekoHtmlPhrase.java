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

package org.danann.cernunnos.net;

import java.net.URL;

import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.DOMReader;

import org.cyberneko.html.parsers.DOMParser;

import org.danann.cernunnos.AttributePhrase;
import org.danann.cernunnos.Attributes;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public final class NekoHtmlPhrase implements Phrase {

	// Instance Members.
	private Phrase context;
	private Phrase location;

	/*
	 * Public API.
	 */

	public static final Reagent CONTEXT = new SimpleReagent("CONTEXT", "@context", ReagentType.PHRASE, String.class,
					"The context from which missing elements of the LOCATION can be inferred if it "
					+ "is relative.  The default is a URL representing the filesystem location from which "
						+ "Java is executing.", new AttributePhrase(Attributes.ORIGIN));

	public static final Reagent LOCATION = new SimpleReagent("LOCATION", "descendant-or-self::text()", ReagentType.PHRASE, String.class,
					"Location of a resource from which a document may be read.  May be a filesystem path (absolute or relative), "
					+ "or a URL.  If relative, the location will be evaluated from the CONTEXT.  If omitted, the value of the "
					+ "'Attributes.LOCATION' request attribute will be used.", new AttributePhrase(Attributes.LOCATION));

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {CONTEXT, LOCATION};
		return new SimpleFormula(NekoHtmlPhrase.class, reagents);
	}

	public void init(EntityConfig config) {

		// Instance Members.
		this.context = (Phrase) config.getValue(CONTEXT);
		this.location = (Phrase) config.getValue(LOCATION);

	}

	public Object evaluate(TaskRequest req, TaskResponse res) {


		Node rslt = null;

		String ctx_str = (String) context.evaluate(req, res);
		String loc_str = (String) location.evaluate(req, res);
		try {

			URL ctx = new URL(ctx_str);
			URL src = new URL(ctx, loc_str);

			DOMParser parser = new DOMParser();
			parser.parse(src.toString());

			Document doc = new DOMReader().read(parser.getDocument());
			rslt = doc.getRootElement();


		} catch (Throwable t) {
			String msg = "Unable to read the specified document:"
						+ "\n\tCONTEXT=" + ctx_str
						+ "\n\tLOCATION=" + loc_str;
			throw new RuntimeException(msg, t);
		}

		return rslt;

	}

}
