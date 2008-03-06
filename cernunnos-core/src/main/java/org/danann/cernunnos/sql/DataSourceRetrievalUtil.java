/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.danann.cernunnos.sql;

import java.sql.Connection;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
class DataSourceRetrievalUtil {
    private static final Log LOG = LogFactory.getLog(DataSourceRetrievalUtil.class); 
    
    public static DataSource getDataSource(Phrase dataSourcePhrase, Phrase connectionPhrase, TaskRequest req, TaskResponse res) {
        final DataSource dataSource;
        final Connection conn = (Connection) connectionPhrase.evaluate(req, res);
        if (conn == null) {
            // This is good... this is what we want...
            dataSource = (DataSource) dataSourcePhrase.evaluate(req, res);
        } else {
            // This is *less* good... the Cernunnos XML should be updated...
            if (LOG.isWarnEnabled()) {
                String msg = "The CONNECTION reagent has been deprecated.  Please " +
                                "update the Cernunnos XML to use DATA_SOURCE.";
                LOG.warn(msg);
            }
            dataSource = new SingleConnectionDataSource(conn, false);
        }
        
        return dataSource;
    }
}
