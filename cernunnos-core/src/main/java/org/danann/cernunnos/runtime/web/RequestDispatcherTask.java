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

import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

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

public final class RequestDispatcherTask implements Task {

	// Instance Members.
	private Phrase request;
	private Phrase response;
	private Phrase resource;

	/*
	 * Public API.
	 */

	// NB:  For now, at least, we will assume the PortletRequest and PortletResponse
	// are registered as request attributes under the cononnical names...

	public static final Reagent RESOURCE = new SimpleReagent("RESOURCE", "@resource", ReagentType.PHRASE, String.class,
								"Location of the resource to be included (e.g. '/WEB-INF/jsp/index.jsp').");

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {RESOURCE};
		final Formula rslt = new SimpleFormula(RequestDispatcherTask.class, reagents);
		return rslt;
	}

	public void init(EntityConfig config) {

		// Instance Members.
		this.request = new AttributePhrase(WebAttributes.REQUEST);
		this.response = new AttributePhrase(WebAttributes.RESPONSE);
		this.resource = (Phrase) config.getValue(RESOURCE);

	}

	public void perform(TaskRequest req, TaskResponse res) {

		RenderRequest preq = (RenderRequest) request.evaluate(req, res);
		RenderResponse pres = (RenderResponse) response.evaluate(req, res);
		String rsc = (String) resource.evaluate(req, res);

		try {

			// Load the PortletRequest...
			for (String key : req.getAttributeNames()) {
				preq.setAttribute(key, req.getAttribute(key));
			}

			PortletRequestDispatcher prd = preq.getPortletSession(true).getPortletContext().getRequestDispatcher(rsc);
			prd.include(preq, pres);

		} catch (Throwable t) {
			String msg = "Error dispatching to the specified resource:  " + rsc;
			throw new RuntimeException(msg, t);
		}

	}

}