/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.danann.cernunnos.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;
import org.springframework.jdbc.core.PreparedStatementSetter;

/**
 * Provides the common function of binding a List of evaluated Phrase values to a PreparedStatement
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
class PhraseParameterPreparedStatementSetter implements PreparedStatementSetter {
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private final List<Phrase> parameters;
    private final TaskRequest req;
    private final TaskResponse res;

    public PhraseParameterPreparedStatementSetter(List<Phrase> parameters, TaskRequest req, TaskResponse res) {
        this.parameters = parameters;
        this.req = req;
        this.res = res;
    }

    /* (non-Javadoc)
     * @see org.springframework.jdbc.core.PreparedStatementSetter#setValues(java.sql.PreparedStatement)
     */
    public final void setValues(PreparedStatement ps) throws SQLException {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Setting parameters " + this.parameters + " on prepared statement " + ps);
        }
        
        int parameterIndex = 0;
        for (final Phrase parameterPhrase : this.parameters) {
            final Object parameter = parameterPhrase.evaluate(this.req, this.res);
            ps.setObject(++parameterIndex, parameter);
        }
    }
}
