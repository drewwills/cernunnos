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
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.dom4j.Node;

import org.danann.cernunnos.AttributePhrase;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.Task;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public final class UpsertTask implements Task {

	// Instance Members.
	private Phrase connection;
	private Phrase update_sql;
	private Phrase insert_sql;
	private List<Phrase> parameters;
	private List<Phrase> update_parameters;
	private List<Phrase> insert_parameters;

	/*
	 * Public API.
	 */

	public static final Reagent CONNECTION = new SimpleReagent("CONNECTION", "@connection", ReagentType.PHRASE, Connection.class,
										"Optional name of a request attribute containing a dgatabase connection.  If omitted, "
										+ "the name 'SqlAttributes.CONNECTION' will be used.",
										new AttributePhrase(SqlAttributes.CONNECTION));

	public static final Reagent UPDATE_SQL = new SimpleReagent("UPDATE_SQL", "update-statement", ReagentType.PHRASE, String.class,
										"The SQL statement that performs the Update portion of the 'Upsert' operation.");

	public static final Reagent INSERT_SQL = new SimpleReagent("INSERT_SQL", "insert-statement", ReagentType.PHRASE, String.class,
										"The SQL statement that performs the Insert portion of the 'Upsert' operation.");

	public static final Reagent PARAMETERS = new SimpleReagent("PARAMETERS", "parameter/@value", ReagentType.NODE_LIST, List.class,
										"The parameters (if any) for the PreparedStatement objects that will perform this upsert.  "
										+ "WARNING:  Parameters must appear in the same order as the associated SQL.",
										Collections.emptyList());

	public static final Reagent UPDATE_PARAMETERS = new SimpleReagent("UPDATE_PARAMETERS", "update-parameter/@value", ReagentType.NODE_LIST, List.class,
										"If provided, UPDATE_PARAMETERS will override the PARAMETERS list for the 'update' operation only.  "
										+ "Use UPDATE_PARAMETERS and INSERT_PARAMETERS instead of PARAMETERS if update and insert parameters must "
										+ "differ in number or order.", null);

	public static final Reagent INSERT_PARAMETERS = new SimpleReagent("INSERT_PARAMETERS", "insert-parameter/@value", ReagentType.NODE_LIST, List.class,
										"If provided, INSERT_PARAMETERS will override the PARAMETERS list for the 'insert' operation only.  "
										+ "Use UPDATE_PARAMETERS and INSERT_PARAMETERS instead of PARAMETERS if update and insert parameters must "
										+ "differ in number or order.", null);

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {CONNECTION, UPDATE_SQL, INSERT_SQL,
						PARAMETERS, UPDATE_PARAMETERS, INSERT_PARAMETERS};
		final Formula rslt = new SimpleFormula(UpsertTask.class, reagents);
		return rslt;
	}

	public void init(EntityConfig config) {

		this.connection = (Phrase) config.getValue(CONNECTION);
		this.update_sql = (Phrase) config.getValue(UPDATE_SQL);
		this.insert_sql = (Phrase) config.getValue(INSERT_SQL);

		List nodes = null;

		// PARAMETERS...
		this.parameters = new LinkedList<Phrase>();
		nodes = (List) config.getValue(PARAMETERS);
		for (Iterator it = nodes.iterator(); it.hasNext();) {
			Node n = (Node) it.next();
			parameters.add(config.getGrammar().newPhrase(n.getText()));
		}

		// UPDATE_PARAMETERS...
		nodes = (List) config.getValue(UPDATE_PARAMETERS);
		if (nodes != null) {
			this.update_parameters = new LinkedList<Phrase>();
			for (Iterator it = nodes.iterator(); it.hasNext();) {
				Node n = (Node) it.next();
				update_parameters.add(config.getGrammar().newPhrase(n.getText()));
			}
		} else {
			// Go with PARAMETERS...
			this.update_parameters = null;
		}

		// INSERT_PARAMETERS...
		nodes = (List) config.getValue(INSERT_PARAMETERS);
		if (nodes != null) {
			this.insert_parameters = new LinkedList<Phrase>();
			for (Iterator it = nodes.iterator(); it.hasNext();) {
				Node n = (Node) it.next();
				insert_parameters.add(config.getGrammar().newPhrase(n.getText()));
			}
		} else {
			// Go with PARAMETERS...
			this.insert_parameters = null;
		}

	}

	public void perform(TaskRequest req, TaskResponse res) {

		PreparedStatement update = null;
		PreparedStatement insert = null;
		try {

			Connection conn = (Connection) connection.evaluate(req, res);

			// We will need to choose between PARAMETERS or UPDATE_PARAMETERS/INSERT_PARAMETERS...
			List<Phrase> parametersInUse = null;

			update = conn.prepareStatement((String) update_sql.evaluate(req, res));
			parametersInUse = update_parameters != null ? update_parameters : parameters;
			int i=0;
			for (Phrase p : parametersInUse) {
				update.setObject(++i, p.evaluate(req, res));
			}
			switch (update.executeUpdate()) {
				case 1:
					// This is nice -- row updated, nothing to do...
					break;
				case 0:
					// No information present, add the row...
					insert = conn.prepareStatement((String) insert_sql.evaluate(req, res));
					parametersInUse = insert_parameters != null ? insert_parameters : parameters;
					i=0;
					for (Phrase p : parametersInUse) {
						insert.setObject(++i, p.evaluate(req, res));
					}
					insert.executeUpdate();
					break;
				default:
					// Problem???
					break;
			}

		} catch (Throwable t) {
			String msg = "Unable to perform the specified upsert operation.";
			throw new RuntimeException(msg, t);
		} finally {

			// Cleanup...
			if (update != null) {
				try {
					update.close();
				} catch (Throwable t) {
					String msg = "Unable to close the update statement.";
					throw new RuntimeException(msg, t);
				}
			}
			if (insert != null) {
				try {
					insert.close();
				} catch (Throwable t) {
					String msg = "Unable to close the insert statement.";
					throw new RuntimeException(msg, t);
				}
			}

		}

	}

}