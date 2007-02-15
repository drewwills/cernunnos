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

package org.danann.cernunnos.sql;

import java.sql.Connection;
import java.sql.DriverManager;

import org.danann.cernunnos.AbstractContainerTask;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.LiteralPhrase;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

/**
 * <code>Task</code> implementation that opens a JDBC connection and makes it 
 * available to subtasks.  The new connection will automatically be closed when 
 * execution of this task concludes. 
 */
public final class OpenConnectionTask extends AbstractContainerTask {
	
	// Instance Members.
	private Phrase attribute_name;
	private Phrase driver;
	private Phrase url;
	private Phrase username;
	private Phrase password;

	/*
	 * Public API.
	 */

	public static final Reagent ATTRIBUTE_NAME = new SimpleReagent("ATTRIBUTE_NAME", "@attribute-name", ReagentType.PHRASE, String.class,
								"Optional name under which the new connection will be registered as a request attribute.  If omitted, the name "
								+ "'SqlAttributes.CONNECTION' will be used.", new LiteralPhrase(SqlAttributes.CONNECTION));

	public static final Reagent DRIVER = new SimpleReagent("DRIVER", "@driver", ReagentType.PHRASE, String.class,
								"The JDBC driver class to use when opening the connection.");

	public static final Reagent URL = new SimpleReagent("LOCATION", "@url", ReagentType.PHRASE, String.class,
								"The JDBC connection LOCATION to use when opening the connection.");

	public static final Reagent USERNAME = new SimpleReagent("USERNAME", "@username", ReagentType.PHRASE, String.class,
								"The username to use when opening the connection.");

	public static final Reagent PASSWORD = new SimpleReagent("PASSWORD", "@password", ReagentType.PHRASE, String.class,
								"The password to use when opening the connection.");

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {ATTRIBUTE_NAME, DRIVER, URL, USERNAME, 
									PASSWORD, AbstractContainerTask.SUBTASKS};
		final Formula rslt = new SimpleFormula(OpenConnectionTask.class, reagents);
		return rslt;
	}

	public void init(EntityConfig config) {

		super.init(config);		

		// Instance Members.
		this.attribute_name = (Phrase) config.getValue(ATTRIBUTE_NAME);
		this.driver = (Phrase) config.getValue(DRIVER);
		this.url = (Phrase) config.getValue(URL);
		this.username = (Phrase) config.getValue(USERNAME);
		this.password = (Phrase) config.getValue(PASSWORD);
		
	}

	public void perform(TaskRequest req, TaskResponse res) {

		String finalUrl = (String) url.evaluate(req, res);
		Connection conn = null;
		try {

			// Open the Connection...
			Class.forName((String) driver.evaluate(req, res));
			conn = DriverManager.getConnection(finalUrl, (String) username.evaluate(req, res), 
													(String) password.evaluate(req, res));
			
			// Make it available as a request attribute...
			res.setAttribute((String) attribute_name.evaluate(req, res), conn);
			
			// Invoke subtasks...
			super.performSubtasks(req, res);
			
		} catch (Throwable t) {
			String msg = "Unable to connect to the specified database:  " + finalUrl;
			throw new RuntimeException(msg, t);
		} finally {
			
			// Cleanup...
			if (conn != null) {
				try {
					conn.close();
				} catch (Throwable t) {
					String msg = "Unable to close the specified connection:  " + finalUrl;
					throw new RuntimeException(msg, t);
				}
			}
			
		}
		
	}
		
}
