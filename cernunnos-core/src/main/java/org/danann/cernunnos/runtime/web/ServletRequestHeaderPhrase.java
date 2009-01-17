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

import javax.servlet.http.HttpServletRequest;
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

/**
 * Implements a Phrase that returns a named request header from the {@link 
 * javax.servlet.http.HttpServletRequest HttpServletRequest}.  
 * 
 * @author <A href="mailto:argherna@gmail.com">Andy Gherna</A>
 */
public class ServletRequestHeaderPhrase implements Phrase {

	// Instance Members.
	private Phrase source;
	private Phrase header_name;

	public static final Reagent SOURCE = new SimpleReagent("SOURCE", "@source", ReagentType.PHRASE, HttpServletRequest.class,
					"Optional source request against which the request header will be returned from.  If not "
					+ "provided, the value of the 'WebAttributes.REQUEST' request attribute will be used.", 
					new AttributePhrase(WebAttributes.REQUEST));

	public static final Reagent HEADER_NAME = new SimpleReagent("HEADER_NAME", "descendant-or-self::text()", 
					ReagentType.PHRASE, String.class, "The name of the request header.");

	
	/**
	 * {@inheritDoc}
	 */
	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {SOURCE, HEADER_NAME};
		return new SimpleFormula(ServletRequestHeaderPhrase.class, reagents);
	}


	/**
	 * {@inheritDoc}
	 */
	public void init(EntityConfig config) {

		this.source = (Phrase) config.getValue(SOURCE); 
		this.header_name = (Phrase) config.getValue(HEADER_NAME); 
	}


	/**
	 * {@inheritDoc}
	 */
	public Object evaluate(TaskRequest req, TaskResponse res) {
		
		HttpServletRequest hreq = (HttpServletRequest) source.evaluate(req, res);
		String headerName = (String) header_name.evaluate(req, res);
		
		return hreq.getHeader(headerName);
	}

}
