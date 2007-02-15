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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.dom4j.Node;

import org.danann.cernunnos.AbstractContainerTask;
import org.danann.cernunnos.AttributePhrase;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Task;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

/**
 * Performs a specified query, then invokes child tasks once for each row in the 
 * result set.
 */
public final class QueryTask extends AbstractContainerTask {

	// Instance Members.
	private Phrase connection;
	private Phrase sql;
	private List<Phrase> parameters;

	/*
	 * Public API.
	 */

	public static final Reagent CONNECTION = new SimpleReagent("CONNECTION", "@connection", ReagentType.PHRASE, Connection.class,
										"Optional name of a request attribute containing a dgatabase connection.  If omitted, "
										+ "the name 'SqlAttributes.CONNECTION' will be used.", 
										new AttributePhrase(SqlAttributes.CONNECTION));
	
	public static final Reagent SQL = new SimpleReagent("SQL", "sql", ReagentType.PHRASE, String.class, 
										"The SQL query statement that will be executed.");

	public static final Reagent PARAMETERS = new SimpleReagent("PARAMETERS", "parameter/@value", ReagentType.NODE_LIST, List.class, 
					"The parameters (if any) for the PreparedStatement that will perform this query.", 
					Collections.emptyList());

	public static final Reagent SUBTASKS = new SimpleReagent("SUBTASKS", "subtasks/*", ReagentType.NODE_LIST, List.class, 
									"The set of tasks that are children of this query task.", new LinkedList<Task>());

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {CONNECTION, SQL, PARAMETERS, SUBTASKS};
		final Formula rslt = new SimpleFormula(QueryTask.class, reagents);
		return rslt;
	}

	public void init(EntityConfig config) {

		super.init(config);		

		// Instance Members.
		this.connection = (Phrase) config.getValue(CONNECTION);
		this.sql = (Phrase) config.getValue(SQL); 
		this.parameters = new LinkedList<Phrase>();
		List nodes = (List) config.getValue(PARAMETERS);		
		for (Iterator it = nodes.iterator(); it.hasNext();) {
			Node n = (Node) it.next();
			parameters.add(config.getGrammar().newPhrase(n.getText()));
		}
		
	}
	
	public void perform(TaskRequest req, TaskResponse res) {
		
		String finalSql = (String) sql.evaluate(req, res);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {

			// Obtain the specified connection, create the statement...
			Connection conn = (Connection) connection.evaluate(req, res);
			pstmt = conn.prepareStatement(finalSql);


			// Set parameters...
			int index = 0;
			for (Phrase p : parameters) {
				pstmt.setObject(++index, p.evaluate(req, res));
			}
						
			// Execute the query, perform subtasks...
			rs = pstmt.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			while (rs.next()) {

				// Make all the data on the current row available to subtasks...
				for (int i=1; i <= rsmd.getColumnCount(); i++) {
					Object value = rs.getObject(i);

					// Access either by column name or column index...
					res.setAttribute(String.valueOf(i), value);
					res.setAttribute(rsmd.getColumnName(i), value);
				}
				
				// Invoke subtasks...
				super.performSubtasks(req, res);
				
			}
			
		} catch (Throwable t) {
			String msg = "Unable to perform the specified query:  " + finalSql;
			throw new RuntimeException(msg, t);
		} finally {
			
			// Cleanup...
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (Throwable t) {
					String msg = "Unable to close the specified statement:  " + finalSql;
					throw new RuntimeException(msg, t);
				}
			}
			
		}
		
	}
	
}