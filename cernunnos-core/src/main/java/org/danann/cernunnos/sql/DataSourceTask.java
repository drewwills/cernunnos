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

import org.apache.commons.dbcp.BasicDataSource;
import org.danann.cernunnos.AbstractContainerTask;
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

/**
 * Creates a pooled {@link javax.sql.DataSource} using {@link BasicDataSource}. All of
 * the default pooling options are used. The {@link javax.sql.DataSource} resources
 * are cleaned up at the end of execution.
 */
public final class DataSourceTask extends AbstractContainerTask {

    //	// Instance Members.
    private BasicDataSourceTemplate basicDataSourceTemplate;

	/*
	 * Public API.
	 */

	public static final Reagent ATTRIBUTE_NAME = new SimpleReagent("ATTRIBUTE_NAME", "@attribute-name", ReagentType.PHRASE, String.class,
								"Optional name under which the new DataSource will be registered as a request attribute.  If omitted, " +
								"the name 'SqlAttributes.DATA_SOURCE' will be used.", new LiteralPhrase(SqlAttributes.DATA_SOURCE));

	public static final Reagent DRIVER = new SimpleReagent("DRIVER", "@driver", ReagentType.PHRASE, String.class,
								"Optional JDBC driver class name to use when opening the connection.  You must " +
								"provide either DATA_SOURCE or DRIVER, URL, USERNAME, and PASSWORD.");

	public static final Reagent URL = new SimpleReagent("URL", "@url", ReagentType.PHRASE, String.class,
								"Optional JDBC connection URL to use when opening the connection.  You " +
								"must provide either DATA_SOURCE or DRIVER, URL, USERNAME, and PASSWORD.");

	public static final Reagent USERNAME = new SimpleReagent("USERNAME", "@username", ReagentType.PHRASE, String.class,
								"Optional username to use when opening the connection.  You must provide either " +
								"DATA_SOURCE or DRIVER, URL, USERNAME, and PASSWORD.");

	public static final Reagent PASSWORD = new SimpleReagent("PASSWORD", "@password", ReagentType.PHRASE, String.class,
								"Optional password to use when opening the connection.  You must provide either " +
								"DATA_SOURCE or DRIVER, URL, USERNAME, and PASSWORD.");

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {ATTRIBUTE_NAME, DRIVER, URL, USERNAME, PASSWORD, AbstractContainerTask.SUBTASKS};
		return new SimpleFormula(DataSourceTask.class, reagents);
	}

	@Override
    public void init(EntityConfig config) {
		super.init(config);

		// Instance Members.
		final Phrase attributeNamePhrase = (Phrase) config.getValue(ATTRIBUTE_NAME);
		final Phrase driverPhrase = (Phrase) config.getValue(DRIVER);
		final Phrase urlPhrase = (Phrase) config.getValue(URL);
		final Phrase usernamePhrase = (Phrase) config.getValue(USERNAME);
		final Phrase passwordPhrase = (Phrase) config.getValue(PASSWORD);
		
		this.basicDataSourceTemplate = new BasicDataSourceTemplateImpl(attributeNamePhrase, driverPhrase, urlPhrase, usernamePhrase, passwordPhrase);
	}

	public void perform(TaskRequest req, TaskResponse res) {
	    //Delegate to the template class
	    this.basicDataSourceTemplate.perform(req, res);
	}
	
    /**
     * Local DataSource template that just executes {@link DataSourceTask#performSubtasks(TaskRequest, TaskResponse)}
     * in the call-back.
     */
    private final class BasicDataSourceTemplateImpl extends BasicDataSourceTemplate {
        private BasicDataSourceTemplateImpl(Phrase attributeNamePhrase, Phrase driverPhrase, Phrase urlPhrase, Phrase usernamePhrase, Phrase passwordPhrase) {
            super(attributeNamePhrase, driverPhrase, urlPhrase, usernamePhrase, passwordPhrase);
        }

        @Override
        protected void performWithDataSource(TaskRequest req, TaskResponse res, DataSource dataSource) {
            DataSourceTask.this.performSubtasks(req, res);
        }
    }
}
