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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

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
import org.dom4j.Node;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

public class StatementTask implements Task {
    // Instance Members.
    private Phrase dataSourcePhrase;
    private Phrase sql;
	private List<Phrase> parameters;

	/*
	 * Public API.
	 */

    public static final Reagent DATA_SOURCE = new SimpleReagent("DATA_SOURCE", "@data-source", ReagentType.PHRASE, DataSource.class,
            "The DataSource to use for executing the SQL. If omitted the request attribute under the name " +
            "'SqlAttributes.DATA_SOURCE' will be used", new AttributePhrase(SqlAttributes.DATA_SOURCE));

	public static final Reagent SQL = new SimpleReagent("SQL", "@sql", ReagentType.PHRASE, String.class,
										"The SQL statement that will be executed.");

	public static final Reagent PARAMETERS = new SimpleReagent("PARAMETERS", "parameter/@value", ReagentType.NODE_LIST, List.class,
										"The parameters (if any) for the PreparedStatement that will execute the SQL.  "
										+ "WARNING:  Parameters must appear in the same order in both update and insert statements.",
										Collections.emptyList());

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {DATA_SOURCE, SQL, PARAMETERS};
		final Formula rslt = new SimpleFormula(StatementTask.class, reagents);
		return rslt;
	}

	@SuppressWarnings("unchecked")
    public void init(EntityConfig config) {
	    this.dataSourcePhrase = (Phrase) config.getValue(DATA_SOURCE);
        this.sql = (Phrase) config.getValue(SQL);
		this.parameters = new LinkedList<Phrase>();
		
		final List<Node> nodes = (List<Node>) config.getValue(PARAMETERS);
		for (final Node n : nodes) {
			parameters.add(config.getGrammar().newPhrase(n.getText()));
		}
	}

	public void perform(TaskRequest req, TaskResponse res) {
        //Get the DataSource from the request and create a JdbcTemplate to use.
        final DataSource dataSource = (DataSource) this.dataSourcePhrase.evaluate(req, res);
        final SimpleJdbcTemplate jdbcTemplate = new SimpleJdbcTemplate(dataSource);
        final JdbcOperations jdbcOperations = jdbcTemplate.getJdbcOperations();

        //Setup the callback class to do the PreparedStatement parameter binding and statement execution
		final PreparedStatementCallback preparedStatementCallback = new PhraseParameterPreparedStatementCallback(this.parameters, req, res);
        
		//Get the SQL and execute it in a PreparedStatement
		final String fSql = (String) sql.evaluate(req, res);
        jdbcOperations.execute(fSql, preparedStatementCallback);
	}
	

    /**
     * Uses PhraseParameterPreparedStatementSetter to bind the parameters then executes the PreparedSetatement.
     * 
     * Always returns null.
     */
    private static final class PhraseParameterPreparedStatementCallback implements PreparedStatementCallback {
        private final PhraseParameterPreparedStatementSetter preparedStatementSetter;

        private PhraseParameterPreparedStatementCallback(List<Phrase> parameters, TaskRequest req, TaskResponse res) {
            this.preparedStatementSetter = new PhraseParameterPreparedStatementSetter(parameters, req, res);
        }

        public Object doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
            this.preparedStatementSetter.setValues(ps);
            
            ps.execute();
            
            return null;
        }
    }
}