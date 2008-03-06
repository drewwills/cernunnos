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
import java.sql.SQLException;
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
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

public final class SqlPhrase implements Phrase {

    // Instance Members.
	private Phrase dataSourcePhrase;
	private Phrase sql;

	/*
	 * Public API.
	 */

    public static final Reagent DATA_SOURCE = new SimpleReagent("DATA_SOURCE", "@data-source", ReagentType.PHRASE, DataSource.class,
            "The DataSource to use for executing the SQL. If omitted the request attribute under the name " +
            "'SqlAttributes.DATA_SOURCE' will be used", new AttributePhrase(SqlAttributes.DATA_SOURCE));

	public static final Reagent SQL = new SimpleReagent("SQL", "descendant-or-self::text()",
					ReagentType.PHRASE, String.class, "The SQL expression to evaluate.");

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {DATA_SOURCE, SQL};
		return new SimpleFormula(SqlPhrase.class, reagents);
	}

	public void init(EntityConfig config) {
		// Instance Members.
		this.dataSourcePhrase = (Phrase) config.getValue(DATA_SOURCE);
		this.sql = (Phrase) config.getValue(SQL);

	}

	public Object evaluate(TaskRequest req, TaskResponse res) {
	    //Get the DataSource from the request and create a JdbcTemplate to use.
	    final DataSource dataSource = (DataSource) this.dataSourcePhrase.evaluate(req, res);
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
