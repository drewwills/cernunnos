package org.danann.cernunnos.sql;

import java.sql.ResultSetMetaData;

import org.danann.cernunnos.AbstractContainerTask;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public class ColumnIteratorTask extends AbstractContainerTask {

	/*
	 * Public API.
	 */

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {AbstractContainerTask.SUBTASKS};
		final Formula rslt = new SimpleFormula(ColumnIteratorTask.class, reagents);
		return rslt;
	}

	public void init(EntityConfig config) {
		super.init(config);		
	}

	public void perform(TaskRequest req, TaskResponse res) {
		
		try {
			ResultSetMetaData rsmd = (ResultSetMetaData) req.getAttribute(SqlAttributes.RESULT_SET_METADATA);
			for (int i=1; i <= rsmd.getColumnCount(); i++) {
				res.setAttribute(SqlAttributes.COLUMN_NAME, rsmd.getColumnName(i));
				super.performSubtasks(req, res);
			}
		} catch (Throwable t) {
			String msg = "Error iterating the columns of a result set.";
			throw new RuntimeException(msg, t);
		}
		
	}
}