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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.danann.cernunnos.CurrentDirectoryUrlPhrase;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Grammar;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public final class UrlPhrase implements Phrase {

	// Instance Members.
	private Grammar grammar;
	private Phrase context;
	private Phrase location;

	/*
	 * Public API.
	 */

	public static final Reagent CONTEXT = new SimpleReagent("CONTEXT", "@context", ReagentType.PHRASE, String.class,
						"The context from which missing elements of the LOCATION can be inferred if it "
						+ "is relative.  The default is a URL representing the filesystem location from which "
						+ "Java is executing.", new CurrentDirectoryUrlPhrase());

	public static final Reagent LOCATION = new SimpleReagent("LOCATION", "descendant-or-self::text()", ReagentType.PHRASE, String.class,
						"Location of a resource to be read.  May be a filesystem path (absolute or relative), or a URL.");

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {CONTEXT, LOCATION};
		return new SimpleFormula(UrlPhrase.class, reagents);
	}

	public void init(EntityConfig config) {

		// Instance Members.
		this.grammar = config.getGrammar();
		this.context = (Phrase) config.getValue(CONTEXT);
		this.location = (Phrase) config.getValue(LOCATION);

	}

	public Object evaluate(TaskRequest req, TaskResponse res) {

		Object rslt = null;

		// Now do the heavy-lifting...
		String loc = (String) location.evaluate(req, res);

		InputStream inpt = null;
		try {

			URL ctx = new URL((String) context.evaluate(req, res));
			URL u = new URL(ctx, loc);
			inpt = u.openStream();
			byte[] bytes = new byte[inpt.available()];
			inpt.read(bytes);
			Phrase text = grammar.newPhrase(new String(bytes));
			rslt = text.evaluate(req, res);

		} catch (Throwable t) {
			String msg = "UrlPhrase terminated unexpectedly.";
			throw new RuntimeException(msg, t);
		} finally {
			if (inpt != null) {
				try {
					inpt.close();
				} catch (IOException ioe) {
					throw new RuntimeException(ioe);
				}
			}
		}

		return rslt;

	}

}