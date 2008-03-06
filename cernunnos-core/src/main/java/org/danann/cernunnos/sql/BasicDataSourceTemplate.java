/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.danann.cernunnos.sql;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

/**
 * Creates a BasicDataSource for evaluated values of the specified Phrases. The DataSource is
 * bound to the response using the value from the attributeNamePhrase and also passed
 * explicitly to {@link #performWithDataSource(TaskRequest, TaskResponse, DataSource)}. 
 * 
 * Note that while the DataSource is attached to the response as well before {@link #performWithDataSource(TaskRequest, TaskResponse, DataSource)}
 * is called it is only valid for the duration of that call and is closed immediately after
 * {@link #performWithDataSource(TaskRequest, TaskResponse, DataSource)} returns.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class BasicDataSourceTemplate {
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private final Phrase attributeNamePhrase;
    private final Phrase driverPhrase;
    private final Phrase urlPhrase;
    private final Phrase usernamePhrase;
    private final Phrase passwordPhrase;

    public BasicDataSourceTemplate(Phrase attributeNamePhrase, Phrase driverPhrase, Phrase urlPhrase, Phrase usernamePhrase, Phrase passwordPhrase) {
        this.attributeNamePhrase = attributeNamePhrase;
        this.driverPhrase = driverPhrase;
        this.urlPhrase = urlPhrase;
        this.usernamePhrase = usernamePhrase;
        this.passwordPhrase = passwordPhrase;
    }

    
    public final void perform(TaskRequest req, TaskResponse res) {
        //Get the JDBC properties
        final String driverClassName = (String) this.driverPhrase.evaluate(req, res);
        final String url = (String) this.urlPhrase.evaluate(req, res);
        final String username = (String) this.usernamePhrase.evaluate(req, res);
        final String password = (String) this.passwordPhrase.evaluate(req, res);
        
        final String dataSourceInfo = "driverClassName='" + driverClassName + "', url='" + url + "', username='" + username + "'";
        this.logger.debug("Creating DataSource for " + dataSourceInfo + ".");
        
        final BasicDataSource dataSource = new BasicDataSource();
        try {
            //Configure the pooling DataSource
            dataSource.setUrl(url);
            dataSource.setDriverClassName(driverClassName);
            dataSource.setUsername(username);
            dataSource.setPassword(password);
            
            //Provide the DataSource on the response environment
            final String dataSourceAttrName = (String) this.attributeNamePhrase.evaluate(req, res);
            res.setAttribute(dataSourceAttrName, dataSource);
            this.logger.debug("Attached DataSource '" + dataSource + "' for " + dataSourceInfo + " to response under attribute '" + dataSourceAttrName + "'.");
            
            //Execute subtasks
            this.performWithDataSource(req, res, dataSource);
        }
        finally {
            try {
                //Cleanup after the subtasks
                dataSource.close();
                this.logger.debug("Closed DataSource '" + dataSource + "' for " + dataSourceInfo + ".");
            }
            catch (SQLException e) {
                throw new RuntimeException("Failed to close BasicDataSource '" + dataSource + "' for " + dataSourceInfo + ".", e);
            }
        }
    }
    
    /**
     * Sub-classes implement this method to execute with the specified DataSource
     */
    protected abstract void performWithDataSource(TaskRequest req, TaskResponse res, DataSource dataSource);
}
