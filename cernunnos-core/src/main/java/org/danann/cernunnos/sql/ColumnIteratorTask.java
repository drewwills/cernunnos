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

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.danann.cernunnos.AbstractContainerTask;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.jdbc.support.SQLStateSQLExceptionTranslator;

public final class ColumnIteratorTask extends AbstractContainerTask {
    private static final SQLExceptionTranslator SQL_EXCEPTION_TRANSLATOR = new SQLStateSQLExceptionTranslator();

	/*
	 * Public API.
	 */

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {AbstractContainerTask.SUBTASKS};
		final Formula rslt = new SimpleFormula(ColumnIteratorTask.class, reagents);
		return rslt;
	}

	@Override
    public void init(EntityConfig config) {
		super.init(config);		
	}

	public void perform(TaskRequest req, TaskResponse res) {
		ResultSetMetaData rsmd = (ResultSetMetaData) req.getAttribute(SqlAttributes.RESULT_SET_METADATA);
		
		final int columnCount;
        try {
            columnCount = rsmd.getColumnCount();
        }
        catch (SQLException sqle) {
            throw SQL_EXCEPTION_TRANSLATOR.translate("Failed to get column count from ResultSetMetaData", "UNKNOWN", sqle);
        }
        
		for (int i=1; i <= columnCount; i++) {
			final String columnName;
            try {
                columnName = rsmd.getColumnLabel(i);
            }
            catch (SQLException sqle) {
                throw SQL_EXCEPTION_TRANSLATOR.translate("Failed to get column name " + i + " from ResultSetMetaData", "UNKNOWN", sqle);
            }
			
            res.setAttribute(SqlAttributes.COLUMN_NAME, columnName);
			super.performSubtasks(req, res);
		}
	}
}