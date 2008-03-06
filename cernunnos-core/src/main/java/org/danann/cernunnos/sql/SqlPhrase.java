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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.danann.cernunnos.AttributePhrase;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.LiteralPhrase;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

public final class SqlPhrase implements Phrase {

    // Instance Members.
	private Phrase dataSourcePhrase;
	private Phrase connectionPhrase;
	private Phrase sql;
	protected final Log log = LogFactory.getLog(this.getClass());

	/*
	 * Public API.
	 */

    public static final Reagent DATA_SOURCE = new SimpleReagent("DATA_SOURCE", "@data-source", ReagentType.PHRASE, DataSource.class,
            		"The DataSource to use for executing the SQL. If omitted the request attribute under the name " +
            		"'SqlAttributes.DATA_SOURCE' will be used", new AttributePhrase(SqlAttributes.DATA_SOURCE));

	public static final Reagent CONNECTION = new SimpleReagent("CONNECTION", "@connection", ReagentType.PHRASE, Connection.class,
					"**DEPRECATED:  Use DATA_SOURCE instead.**  Optional Connection object.  The default is the value of the " +
					"'SqlAttributes.CONNECTION' request attribute (if specified) or null.", 
					new AttributePhrase(SqlAttributes.CONNECTION, new LiteralPhrase(null)));

	public static final Reagent SQL = new SimpleReagent("SQL", "descendant-or-self::text()",
					ReagentType.PHRASE, String.class, "The SQL expression to evaluate.");

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {DATA_SOURCE, CONNECTION, SQL};
		return new SimpleFormula(SqlPhrase.class, reagents);
	}

	public void init(EntityConfig config) {
		// Instance Members.
		this.dataSourcePhrase = (Phrase) config.getValue(DATA_SOURCE);
		this.connectionPhrase = (Phrase) config.getValue(CONNECTION);
		this.sql = (Phrase) config.getValue(SQL);

	}

	public Object evaluate(TaskRequest req, TaskResponse res) {
	    final DataSource dataSource = DataSourceRetrievalUtil.getDataSource(dataSourcePhrase, connectionPhrase, req, res);
		
	    final SimpleJdbcTemplate jdbcTemplate = new SimpleJdbcTemplate(dataSource);
	    
	    //Get the SQL and execute it
	    final String fSql = (String) this.sql.evaluate(req, res);
	    final List<Object> results = jdbcTemplate.query(fSql, new FirstColumnObjectRowMapper());

	    //Return the first row if there is one, otherwise return null
	    if (results.size() > 0) {
	        return results.get(0);
	    }
	    return null;

	}

    /**
     * RowMapper that always returns the first column as an Object
     */
    private static final class FirstColumnObjectRowMapper implements ParameterizedRowMapper<Object> {
        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getObject(1);
        }
    }
}
