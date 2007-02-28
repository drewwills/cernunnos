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
import java.sql.SQLException;

import org.danann.cernunnos.AbstractContainerTask;
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

public class TransactionTask extends AbstractContainerTask {

	// Instance Members.
	private Phrase connection;

	/*
	 * Public API.
	 */

	public static final Reagent CONNECTION = new SimpleReagent("CONNECTION", "@connection", ReagentType.PHRASE, Connection.class,
				"Optional Connection object.  The default is the value of the 'SqlAttributes.CONNECTION' request attribute.", 
				new AttributePhrase(SqlAttributes.CONNECTION));

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {CONNECTION, AbstractContainerTask.SUBTASKS};
		final Formula rslt = new SimpleFormula(TransactionTask.class, reagents);
		return rslt;
	}

	public void init(EntityConfig config) {

		super.init(config);		

		// Instance Members.
		this.connection = (Phrase) config.getValue(CONNECTION);
		
	}

	public void perform(TaskRequest req, TaskResponse res) {
		
		Connection conn = null;
		try {

			// Obtain the specified connection...
			conn = (Connection) connection.evaluate(req, res);
			
			// Begin the transaction...
			boolean autoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false);
			
			super.performSubtasks(req, res);
			
			conn.commit();
			conn.setAutoCommit(autoCommit);
			
		} catch (Throwable t) {
			try {
				conn.rollback();
			} catch (SQLException sqle) {
				// Do something here?
			}
			String msg = "Error encountered in transaction.  Changes have been rolled back.";
			throw new RuntimeException(msg, t);
		}

	}
	
}