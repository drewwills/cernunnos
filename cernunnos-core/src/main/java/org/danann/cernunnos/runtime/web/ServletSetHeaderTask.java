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
 * Implements a Task that sets a header on an {@link javax.servlet.http.HttpServletResponse
 * HttpServletResponse} from Cernunnos XML.
 * 
 * @author <A href="mailto:argherna@gmail.com">Andy Gherna</A>
 */
public class ServletSetHeaderTask implements Task {

	// Instance Members.
	private Phrase response;
	private Phrase header_name;
	private Phrase header_value;

	public static final Reagent SOURCE = new SimpleReagent("SOURCE", "@source", ReagentType.PHRASE, HttpServletResponse.class,
								"Optional source response to set the header on.  If not specified, the value of "
								+ "the response attribute 'WebAttributes.RESPONSE' will be used.", 
								new AttributePhrase(WebAttributes.RESPONSE));

	public static final Reagent HEADER_NAME = new SimpleReagent("HEADER_NAME", "@header-name", ReagentType.PHRASE, String.class,
								"The name of the attribute.");
	
	public static final Reagent HEADER_VALUE = new SimpleReagent("HEADER_VALUE", "@value", ReagentType.PHRASE, String.class,
								"The string value of the attribute.");
	
	/**
	 * {@inheritDoc }
	 */
	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {HEADER_NAME, HEADER_VALUE, SOURCE};
		final Formula rslt = new SimpleFormula(ServletSetHeaderTask.class, reagents);
		return rslt;
	}


	/**
	 * {@inheritDoc }
	 */
	public void init(EntityConfig config) {

		this.response = (Phrase) config.getValue(SOURCE);
		this.header_name = (Phrase) config.getValue(HEADER_NAME);
		this.header_value = (Phrase) config.getValue(HEADER_NAME);

	}

	
	/**
	 * {@inheritDoc }
	 */
	public void perform(TaskRequest req, TaskResponse res) {

		HttpServletResponse sres = (HttpServletResponse) response.evaluate(req, res);
		String headerName = (String) header_name.evaluate(req, res);
		String headerValue = (String) header_value.evaluate(req, res);
		
		// Send the header
		sres.setHeader(headerName, headerValue);
	}
}
