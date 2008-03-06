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

import javax.sql.DataSource;

import org.danann.cernunnos.AbstractContainerTask;
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
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

public final class TransactionTask extends AbstractContainerTask {

    // Instance Members.
    private Phrase dataSourcePhrase;
    private Phrase attributeNamePhrase;
    private Phrase transactionManagerPhrase;

    /*
     * Public API.
     */

    public static final Reagent DATA_SOURCE = new SimpleReagent("DATA_SOURCE", "@data-source", ReagentType.PHRASE, DataSource.class,
            "Optional DataSource object.  The default is the value of the 'SqlAttributes.DATA_SOURCE' request attribute. " +
            "Only used if TRANSACTION_MANAGER is not specified.", 
            new AttributePhrase(SqlAttributes.DATA_SOURCE));

    public static final Reagent ATTRIBUTE_NAME = new SimpleReagent("ATTRIBUTE_NAME", "@attribute-name", ReagentType.PHRASE, String.class,
            "Optional name under which a newly created PlatformTransactionManager will be registered as a request attribute.  If omitted, " +
            "the name 'SqlAttributes.TRANSACTION_MANAGER' will be used.", new LiteralPhrase(SqlAttributes.TRANSACTION_MANAGER));
    
    public static final Reagent TRANSACTION_MANAGER = new SimpleReagent("TRANSACTION_MANAGER", "@transaction-manager", ReagentType.PHRASE, PlatformTransactionManager.class,
            "Optional PlatformTransactionManager object. If not specified a DataSourceTransactionManager is created using the specified DATA_SOURCE.", 
            new AttributePhrase(SqlAttributes.TRANSACTION_MANAGER, new LiteralPhrase(null)));
    

    public Formula getFormula() {
        Reagent[] reagents = new Reagent[] {DATA_SOURCE, ATTRIBUTE_NAME, TRANSACTION_MANAGER, AbstractContainerTask.SUBTASKS};
        final Formula rslt = new SimpleFormula(TransactionTask.class, reagents);
        return rslt;
    }

    @Override
    public void init(EntityConfig config) {
        super.init(config);        

        // Instance Members.
        this.dataSourcePhrase = (Phrase) config.getValue(DATA_SOURCE);
        this.attributeNamePhrase = (Phrase) config.getValue(ATTRIBUTE_NAME);
        this.transactionManagerPhrase = (Phrase) config.getValue(TRANSACTION_MANAGER);
    }

    public void perform(final TaskRequest req, final TaskResponse res) {
        PlatformTransactionManager transactionManager = (PlatformTransactionManager) this.transactionManagerPhrase.evaluate(req, res);
        if (transactionManager == null) {
            //If no transaction manager was found there MUST be a DataSource
            final DataSource dataSource = (DataSource) this.dataSourcePhrase.evaluate(req, res);        

            //Create a local DataSourceTransactionManager
            transactionManager = new DataSourceTransactionManager(dataSource);
            
            //Register the new tx manager in the response for later use if needed
            final String transactionManagerAttrName = (String) this.attributeNamePhrase.evaluate(req, res);
            res.setAttribute(transactionManagerAttrName, transactionManager);
            
            if (log.isDebugEnabled()) {
            	String msg ="Created PlatformTransactionManager '" + transactionManager 
            					+ "' for DataSource '" + dataSource 
            					+ "' and bound in response under attribute name '" 
            					+ transactionManagerAttrName + "'."; 
                this.log.debug(msg);
            }
        }
        else {
            if (log.isDebugEnabled()) {
            	String msg = "Found PlatformTransactionManager '" 
            				+ transactionManager + "' in request.";
                this.log.debug(msg);
            }
        }
        
        //Create the tx template
        final TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        
        //Execute the transaction in a callback
        final TransactionCallback transactionCallback = new PerformSubtasksTransactionCallback(this, req, res);
        transactionTemplate.execute(transactionCallback);
    }
    
    /**
     * Callback which executes the {@link TransactionTask#performSubtasks(TaskRequest, TaskResponse)} method
     * inside a transaction.
     */
    private static final class PerformSubtasksTransactionCallback extends TransactionCallbackWithoutResult {
        private final TransactionTask transactionTask;
        private final TaskRequest req;
        private final TaskResponse res;

        private PerformSubtasksTransactionCallback(TransactionTask transactionTask, TaskRequest req, TaskResponse res) {
            this.transactionTask = transactionTask;
            this.req = req;
            this.res = res;
        }

        @Override
        protected void doInTransactionWithoutResult(TransactionStatus status) {
        	
        	if (this.transactionTask.log.isDebugEnabled()) {
        		String msg = "Executing subtasks in transaction with status '" 
        												+ status + "'.";
                this.transactionTask.log.debug(msg);
        	}
            this.transactionTask.performSubtasks(this.req, this.res);
        }
    }
}