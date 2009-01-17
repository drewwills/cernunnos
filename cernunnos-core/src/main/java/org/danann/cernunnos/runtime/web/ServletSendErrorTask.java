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
 * Implements a Task that sends an error through an 
 * {@link javax.servlet.http.HttpServletResponse HttpServletResponse} from 
 * Cernunnos XML.
 * 
 * @author <A href="mailto:argherna@gmail.com">Andy Gherna</A>
 */
public class ServletSendErrorTask implements Task {

	// Instance Members.
	private Phrase response;
	private Phrase statusCode;
	private Phrase message;

	/*
	 * Public API.
	 */
	public static final Reagent STATUS_CODE = new SimpleReagent("STATUS_CODE", "@status-code", ReagentType.PHRASE, Integer.class,
								"The HTTP status code.  This should be an error code for this Task (although it is not enforced)");
	
	public static final Reagent MESSAGE = new SimpleReagent("MESSAGE", "text()", ReagentType.PHRASE, String.class,
						"Error message corresponding to the status code", new LiteralPhrase(""));
	
	public static final Reagent SOURCE = new SimpleReagent("SOURCE", "@source", ReagentType.PHRASE, HttpServletResponse.class,
								"Optional source response to set the status code of.  If not specified, the value of "
								+ "the response attribute 'WebAttributes.RESPONSE' will be used.", 
								new AttributePhrase(WebAttributes.RESPONSE));
	
	/**
	 * {@inheritDoc }
	 */
	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {STATUS_CODE, MESSAGE, SOURCE};
		final Formula rslt = new SimpleFormula(ServletSendErrorTask.class, reagents);
		return rslt;
	}


	/**
	 * {@inheritDoc }
	 */
	public void init(EntityConfig config) {
		
		this.response = (Phrase) config.getValue(SOURCE);
		this.statusCode = (Phrase) config.getValue(STATUS_CODE);
		this.message = (Phrase) config.getValue(MESSAGE);
	}

	
	/**
	 * {@inheritDoc }
	 */
	public void perform(TaskRequest req, TaskResponse res) {
		
		HttpServletResponse resp = (HttpServletResponse) response.evaluate(req, res);
		Integer sc = (Integer) statusCode.evaluate(req, res);
		String msg = (String) message.evaluate(req, res);
		
		try {

			resp.sendError(sc, msg);
		} catch (Throwable t) {
			throw new RuntimeException("Error setting status code " + sc + " with message " + msg, t);
		}
	}

}
