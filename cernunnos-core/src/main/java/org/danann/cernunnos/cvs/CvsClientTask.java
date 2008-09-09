/*
 * Copyright 2008 Andrew Wills
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

package org.danann.cernunnos.cvs;

import java.io.IOException;
import java.io.PrintStream;

import org.netbeans.lib.cvsclient.Client;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.admin.StandardAdminHandler;
import org.netbeans.lib.cvsclient.connection.PServerConnection;
import org.netbeans.lib.cvsclient.event.CVSAdapter;
import org.netbeans.lib.cvsclient.event.MessageEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

public class CvsClientTask extends AbstractContainerTask {
	
	// Instance Members.
	private Phrase attribute_name;
	private Phrase cvsroot;
	private Phrase encoded_password;
	private Phrase adapter;
	private final Log log = LogFactory.getLog(getClass());

	/*
	 * Public API.
	 */

	public static final Reagent ATTRIBUTE_NAME = new SimpleReagent("ATTRIBUTE_NAME", "@attribute-name", ReagentType.PHRASE, String.class,
					"Optional name under which the new Client object will be registered as a request attribute.  If omitted, " +
					"the name 'CvsAttributes.CLIENT' will be used.", new LiteralPhrase(CvsAttributes.CLIENT));

	public static final Reagent CVSROOT = new SimpleReagent("CVSROOT", "@cvsroot", ReagentType.PHRASE, String.class,
					"CVSRoot string for connecting to the CVS server (e.g. ':pserver:user@host:/usr/local/cvsroot').");
	
	public static final Reagent ENCODED_PASSWORD = new SimpleReagent("ENCODED_PASSWORD", "@encoded-password", 
					ReagentType.PHRASE, String.class, "The CVS password encoded appropriately.");

    public static final Reagent ADAPTER = new SimpleReagent("ADAPTER", "@adapter", ReagentType.PHRASE, CVSAdapter.class,
            "Event handling adapter class (this must be coded in Java)", new LiteralPhrase(new CVSAdapterImpl()));

    public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {ATTRIBUTE_NAME, CVSROOT, ENCODED_PASSWORD, ADAPTER, AbstractContainerTask.SUBTASKS};
		final Formula rslt = new SimpleFormula(getClass(), reagents);
		return rslt;
	}

	public void init(EntityConfig config) {

		super.init(config);

		// Instance Members.
		this.attribute_name = (Phrase) config.getValue(ATTRIBUTE_NAME);
		this.cvsroot = (Phrase) config.getValue(CVSROOT);
		this.encoded_password = (Phrase) config.getValue(ENCODED_PASSWORD);
		this.adapter = (Phrase) config.getValue(ADAPTER);
		
	}

	public void perform(TaskRequest req, TaskResponse res) {
		
		String cvsr = (String) cvsroot.evaluate(req, res);
		String passwd = (String) encoded_password.evaluate(req, res);

		PServerConnection conn = null;
		try {
			
			// Analyze CVSRoot...
			CVSRoot root = CVSRoot.parse(cvsr);
			
			// Open connection/create client...
			conn = new PServerConnection(root);
		    conn.setEncodedPassword(passwd);
		    conn.open();
		    
			Client client = new Client(conn, new StandardAdminHandler());
			client.getEventManager().addCVSListener((CVSAdapter) adapter.evaluate(req, res));
						
			// Execute Children...
			res.setAttribute((String) attribute_name.evaluate(req, res), client);
			res.setAttribute(CvsAttributes.CVSROOT, cvsr);
			super.performSubtasks(req, res);
			
		} catch (Throwable t) {
			String msg = "Error creating the specified CVS client:" +
							"\n\t\tCVSROOT:  " + cvsr +
							"\n\t\tENCODED_PASSWORD:  " + passwd;
			throw new RuntimeException(msg, t);
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (IOException ioe) {
				log.warn("PServerConnection failed to close() properly.");
			}
		}
		
	}
	
	/*
	 * Nested Types.
	 */
	
    public static class CVSAdapterImpl extends CVSAdapter {

		@Override
        public void messageSent(MessageEvent e) {
        	
            String msg = e.getMessage();
            PrintStream stream = e.isError() ? System.err : System.out;

            final StringBuffer taggedMsgBuffer = new StringBuffer();
            if (!e.isTagged()) {
            	// Simple case -- write the msg...
                stream.println(msg);
            } else {
            	// Gather the msg into a StringBuffer until we have a complete line...
                String line = MessageEvent.parseTaggedMessage(taggedMsgBuffer, msg);
                if (line != null) {
                    stream.println(line);
                }
            }
        }
        
    }

}
