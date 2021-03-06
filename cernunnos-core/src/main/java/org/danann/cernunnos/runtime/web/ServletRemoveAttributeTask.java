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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.danann.cernunnos.AttributePhrase;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.LiteralPhrase;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.Task;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

/**
 * Implements a Task that removes an attribute from a request, the session or 
 * the context.  
 * 
 * This task needs an {@link javax.servlet.http.HttpServletRequest HttpServletRequest}
 * to act on.
 * 
 * @author <A href="mailto:argherna@gmail.com">Andy Gherna</A>
 */
public class ServletRemoveAttributeTask implements Task {

	// Instance Members.
	private Phrase request;
	private Phrase scope;
	private Phrase attribute_name;
	
	/*
	 * Public API.
	 */
	public static final Reagent SCOPE = new SimpleReagent("SCOPE", "@scope", ReagentType.PHRASE, String.class,
								"Where to put the named attribute.  The scopes are the same as in the Servlet API" +
								"i.e., the values are 'request' (the default), 'session' and 'context'",
								new LiteralPhrase("request"));
	
	public static final Reagent ATTRIBUTE_NAME = new SimpleReagent("ATTRIBUTE_NAME", "@attribute-name", ReagentType.PHRASE, String.class,
								"The name of the attribute.");
	
	public static final Reagent SOURCE = new SimpleReagent("SOURCE", "@source", ReagentType.PHRASE, HttpServletRequest.class,
						"Optional source request to add the attribute to.  If not specified, the value of "
						+ "the request attribute 'WebAttributes.REQUEST' will be used.", new AttributePhrase(WebAttributes.REQUEST));
	
	/**
	 * {@inheritDoc }
	 */
	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {SCOPE, ATTRIBUTE_NAME, SOURCE};
		final Formula rslt = new SimpleFormula(ServletRemoveAttributeTask.class, reagents);
		return rslt;
	}


	/**
	 * {@inheritDoc }
	 */
	public void init(EntityConfig config) {
		
		this.request = (Phrase) config.getValue(SOURCE);
		this.scope = (Phrase) config.getValue(SCOPE);
		this.attribute_name = (Phrase) config.getValue(ATTRIBUTE_NAME);
		
	}

	/**
	 * {@inheritDoc }
	 */
	public void perform(TaskRequest req, TaskResponse res) {
		
		HttpServletRequest hreq = (HttpServletRequest) request.evaluate(req, res);
		String myScope = (String) scope.evaluate(req, res);
		
		if (myScope.equalsIgnoreCase("request")) {
			
			hreq.removeAttribute((String) attribute_name.evaluate(req, res));
			
		} else if (myScope.equalsIgnoreCase("session")) {
			
			HttpSession session = hreq.getSession();
			session.removeAttribute((String) attribute_name.evaluate(req, res));
			
		} else if (myScope.equalsIgnoreCase("context")) {
			
			ServletContext context = hreq.getSession().getServletContext();
			context.removeAttribute((String) attribute_name.evaluate(req, res));
			
		} else {
			
			throw new RuntimeException("Unknown scope [" + myScope + "] for" +
					" attribute " + (String) attribute_name.evaluate(req, res));
		}
	}


}
