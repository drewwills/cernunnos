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

import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Map.Entry;
import java.util.Properties;

import org.danann.cernunnos.AbstractContainerTask;
import org.danann.cernunnos.AttributePhrase;
import org.danann.cernunnos.Attributes;
import org.danann.cernunnos.CurrentDirectoryUrlPhrase;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public final class PropertiesTask extends AbstractContainerTask {

	// Instance Members.
	private Phrase context;
	private Phrase location;

	/*
	 * Public API.
	 */

	public static final Reagent CONTEXT = new SimpleReagent("CONTEXT", "@context", ReagentType.PHRASE, String.class,
					"The context from which missing elements of the LOCATION can be inferred if it "
					+ "is relative.  The default is a URL representing the filesystem location from which "
					+ "Java is executing.", new CurrentDirectoryUrlPhrase());

	public static final Reagent LOCATION = new SimpleReagent("LOCATION", "@location", ReagentType.PHRASE, String.class,
					"Location of a .properties file.  May be a filesystem path (absolute or relative), or a URL.  If "
					+ "relative, the location will be evaluated from the CONTEXT.  If omitted, the value of the "
					+ "'Attributes.LOCATION' request attribute will be used.", new AttributePhrase(Attributes.LOCATION));

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {CONTEXT, LOCATION, AbstractContainerTask.SUBTASKS};
		final Formula rslt = new SimpleFormula(PropertiesTask.class, reagents);
		return rslt;
	}

	public void init(EntityConfig config) {

		super.init(config);

		// Instance Members.
		this.context = (Phrase) config.getValue(CONTEXT);
		this.location = (Phrase) config.getValue(LOCATION);

	}

	public void perform(TaskRequest req, TaskResponse res) {

		String loc = (String) location.evaluate(req, res);

		InputStream inpt = null;
		try {

			URL ctx = new URL((String) context.evaluate(req, res));
			URL u = new URL(ctx, loc);
			inpt = u.openStream();

			Properties p = new Properties();
			p.load(inpt);

			for (Entry e : p.entrySet()) {
				res.setAttribute((String) e.getKey(), e.getValue());
			}

			super.performSubtasks(req, res);

		} catch (Throwable t) {
			String msg = "Unable to invoke the specified script:  " + loc;
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

	}

}