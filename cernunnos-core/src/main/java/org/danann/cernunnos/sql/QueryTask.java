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

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import org.danann.cernunnos.AbstractContainerTask;
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
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

/**
 * Performs a specified query, then invokes child tasks once for each row in the
 * result set.
 */
public final class QueryTask extends AbstractContainerTask {

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

	public static final Reagent SQL = new SimpleReagent("SQL", "sql", ReagentType.PHRASE, String.class,
										"The SQL query statement that will be executed.");

	public static final Reagent PARAMETERS = new SimpleReagent("PARAMETERS", "parameter/@value", ReagentType.NODE_LIST, List.class,
					"The parameters (if any) for the PreparedStatement that will perform this query.",
					Collections.emptyList());

	public static final Reagent SUBTASKS = new SimpleReagent("SUBTASKS", "subtasks/*", ReagentType.NODE_LIST, List.class,
									"The set of tasks that are children of this query task.", new LinkedList<Task>());

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {DATA_SOURCE, SQL, PARAMETERS, SUBTASKS};
		final Formula rslt = new SimpleFormula(QueryTask.class, reagents);
		return rslt;
	}

	@Override
    @SuppressWarnings("unchecked")
    public void init(EntityConfig config) {

		super.init(config);

		// Instance Members.
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
        
        //Setup the parameter setter and row callback handler for this task and the request/response
        final PreparedStatementSetter preparedStatementSetter = new PhraseParameterPreparedStatementSetter(this.parameters, req, res);
        final RowCallbackHandler rowCallbackHandler = new ResponseMappingRowCallbackHandler(this, req, res);
        
        //Get the SQL and run the query
        final String finalSql = (String) sql.evaluate(req, res);
        jdbcOperations.query(finalSql, preparedStatementSetter, rowCallbackHandler);
	}
	
	/**
	 * For each row the value of each column is bound to the response by the column name and
	 * index along with the ResultSetMetaData. After binding this information {@link QueryTask#performSubtasks(TaskRequest, TaskResponse)}
	 * is invoked.
	 */
	private static final class ResponseMappingRowCallbackHandler implements RowCallbackHandler {
	    private final QueryTask queryTask;
        private final TaskRequest req;
        private final TaskResponse res;

        private ResponseMappingRowCallbackHandler(QueryTask queryTask, TaskRequest req, TaskResponse res) {
            this.queryTask = queryTask;
            this.req = req;
            this.res = res;
        }

        /* (non-Javadoc)
         * @see org.springframework.jdbc.core.RowCallbackHandler#processRow(java.sql.ResultSet)
         */
        public void processRow(ResultSet rs) throws SQLException {
            final ResultSetMetaData rsmd = rs.getMetaData();
            this.res.setAttribute(SqlAttributes.RESULT_SET_METADATA, rsmd);
            
            //Make all the data on the current row available to subtasks...
            for (int columnIndex = 1; columnIndex <= rsmd.getColumnCount(); columnIndex++) {
                final Object value = rs.getObject(columnIndex);

                // Access either by column name or column index...
                this.res.setAttribute(String.valueOf(columnIndex), value);
                this.res.setAttribute(rsmd.getColumnName(columnIndex).toUpperCase(), value);
            }

            // Invoke subtasks...
            this.queryTask.performSubtasks(this.req, this.res);
        }
	}
}