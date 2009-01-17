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

package org.danann.cernunnos.runtime.web;

import javax.servlet.http.HttpServletResponse;
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

/**
 * Implements a Task that sends a redirect through an {@link javax.servlet.http.HttpServletResponse
 * HttpServletResponse} from Cernunnos XML.
 *
 * @author <A href="argherna@gmail.com">Andy Gherna</A>
 */
public class ServletRedirectTask implements Task {

	// Instance Members.
	private Phrase response;
	private Phrase resource;

	/*
	 * Public API.
	 */
	public static final Reagent RESOURCE = new SimpleReagent("RESOURCE", "@resource", ReagentType.PHRASE, String.class,
								"Location of the resource to be included (e.g. '/webapp' or 'http://www.google.com').");
	
	public static final Reagent SOURCE = new SimpleReagent("SOURCE", "@source", ReagentType.PHRASE, HttpServletResponse.class,
								"Optional source response to send the redirect through.  If not specified, the value of "
								+ "the response attribute 'WebAttributes.RESPONSE' will be used.", 
								new AttributePhrase(WebAttributes.RESPONSE));

	/**
	 * {@inheritDoc }
	 */
	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {RESOURCE, SOURCE};
		final Formula rslt = new SimpleFormula(ServletRedirectTask.class, reagents);
		return rslt;
	}

	/**
	 * {@inheritDoc }
	 */
	public void init(EntityConfig config) {

		// Instance Members.
		this.response = (Phrase) config.getValue(SOURCE);
		this.resource = (Phrase) config.getValue(RESOURCE);
	}

	/**
	 * {@inheritDoc }
	 */
	public void perform(TaskRequest req, TaskResponse res) {

		HttpServletResponse sres = (HttpServletResponse) response.evaluate(req, res);
		String rsc = (String) resource.evaluate(req, res);

		try {

			// Send the redirect (HTTP status code 302)
			sres.sendRedirect(rsc);

		} catch (Throwable t) {
			String msg = "Error redirecting to the specified resource:  " + rsc;
			throw new RuntimeException(msg, t);
		}

	}


}
