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

public final class SqlPhrase implements Phrase {

	// Instance Members.
	private Phrase connection;
	private Phrase sql;

	/*
	 * Public API.
	 */

	public static final Reagent CONNECTION = new SimpleReagent("CONNECTION", "@connection", ReagentType.PHRASE,
					Connection.class, "Database connection object.  If omitted, the request attribute under "
					+ "the name 'SqlAttributes.CONNECTION' will be used.", new AttributePhrase(SqlAttributes.CONNECTION));

	public static final Reagent SQL = new SimpleReagent("SQL", "descendant-or-self::text()",
					ReagentType.PHRASE, String.class, "The SQL expression to evaluate.");

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {CONNECTION, SQL};
		return new SimpleFormula(SqlPhrase.class, reagents);
	}

	public void init(EntityConfig config) {

		// Instance Members.
		this.connection = (Phrase) config.getValue(CONNECTION);
		this.sql = (Phrase) config.getValue(SQL);

	}

	public Object evaluate(TaskRequest req, TaskResponse res) {

		Object rslt = null;

		String fSql = (String) sql.evaluate(req, res);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {

			Connection conn = (Connection) connection.evaluate(req, res);

			pstmt = conn.prepareStatement(fSql);
			rs = pstmt.executeQuery();

			rslt = rs.next() ? rs.getObject(1) : null;

		} catch (Throwable t) {
			String msg = "Unable to execute the specified sql:  " + fSql;
			throw new RuntimeException(msg, t);
		} finally {

			// Cleanup...
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (Throwable t) {
					String msg = "Unable to close the specified statement:  " + fSql;
					throw new RuntimeException(msg, t);
				}
			}

		}

		return rslt;

	}

}
