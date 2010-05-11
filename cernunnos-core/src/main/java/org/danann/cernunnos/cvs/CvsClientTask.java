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
import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.Client;
import org.netbeans.lib.cvsclient.admin.StandardAdminHandler;
import org.netbeans.lib.cvsclient.command.CommandAbortedException;
import org.netbeans.lib.cvsclient.connection.AuthenticationException;
import org.netbeans.lib.cvsclient.connection.Connection;
import org.netbeans.lib.cvsclient.connection.ExtConnection;
import org.netbeans.lib.cvsclient.connection.PServerConnection;
import org.netbeans.lib.cvsclient.event.CVSAdapter;
import org.netbeans.lib.cvsclient.event.MessageEvent;

public class CvsClientTask extends AbstractContainerTask {
	
	// Instance Members.
	private Phrase attribute_name;
	private Phrase cvsroot;
	private Phrase encoded_password;
	private Phrase adapter;
	private Phrase cvs_ext;
	
	private final Log log = LogFactory.getLog(getClass());

	// Used to detect when a setting necessary for a protocol
	// was not set.  This is static so == tests can be done below.
	private static final LiteralPhrase DEFAULT_PHRASE = 
			new LiteralPhrase("");
	
	private static final String DEFAULT_VALUE = "";
	
	/*
	 * Public API.
	 */

	public static final Reagent ATTRIBUTE_NAME = new SimpleReagent("ATTRIBUTE_NAME", "@attribute-name", ReagentType.PHRASE, String.class,
					"Optional name under which the new Client object will be registered as a request attribute.  If omitted, " +
					"the name 'CvsAttributes.CLIENT' will be used.", new LiteralPhrase(CvsAttributes.CLIENT));

	public static final Reagent CVSROOT = new SimpleReagent("CVSROOT", "@cvsroot", ReagentType.PHRASE, String.class,
					"CVSRoot string for connecting to the CVS server (e.g. ':pserver:user@host:/usr/local/cvsroot').");
	
	public static final Reagent ENCODED_PASSWORD = new SimpleReagent("ENCODED_PASSWORD", "@encoded-password", 
					ReagentType.PHRASE, String.class, "The CVS password encoded appropriately.", DEFAULT_PHRASE);

	public static final Reagent CVS_EXT = new SimpleReagent("CVS_EXT", "@cvs-ext", ReagentType.PHRASE, String.class,
					"External command to use when :ext protocol is specified.", DEFAULT_PHRASE);
	
    public static final Reagent ADAPTER = new SimpleReagent("ADAPTER", "@adapter", ReagentType.PHRASE, CVSAdapter.class,
            "Event handling adapter class (this must be coded in Java)", new LiteralPhrase(new CVSAdapterImpl()));

    public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {ATTRIBUTE_NAME, CVSROOT, ENCODED_PASSWORD, CVS_EXT, ADAPTER, AbstractContainerTask.SUBTASKS};
		final Formula rslt = new SimpleFormula(getClass(), reagents);
		return rslt;
	}

	public void init(EntityConfig config) {

		super.init(config);

		// Instance Members.
		this.attribute_name = (Phrase) config.getValue(ATTRIBUTE_NAME);
		this.cvsroot = (Phrase) config.getValue(CVSROOT);
		this.encoded_password = (Phrase) config.getValue(ENCODED_PASSWORD);
		this.cvs_ext = (Phrase) config.getValue(CVS_EXT);
		this.adapter = (Phrase) config.getValue(ADAPTER);
		
	}

	public void perform(TaskRequest req, TaskResponse res) {
		
		String cvsr = (String) cvsroot.evaluate(req, res);
		String passwd = (String) encoded_password.evaluate(req, res);
		String cvsExt = (String) cvs_ext.evaluate(req, res);
		CVSAdapter cvsAdapter = (CVSAdapter) adapter.evaluate(req, res);
		
		Connection conn = null;
		try {
			
			// Analyze CVSRoot...
			CVSRoot root = CVSRoot.parse(cvsr);
			
			if (root.getMethod().equals(CVSRoot.METHOD_PSERVER)) {
				
				if (passwd == DEFAULT_VALUE) {
					throw new RuntimeException("@encrypted-password not set " +
							"for :pserver: cvsroot.  @cvsroot=" + cvsr);
				}
				
				PServerConnection psconn = new PServerConnection(root);
				psconn.setEncodedPassword(passwd);
				
				conn = psconn;
			} else if (root.getMethod().equals(CVSRoot.METHOD_EXT)) {
				
				if (cvsExt == DEFAULT_VALUE) {
					throw new RuntimeException("@cvs-ext not set for " +
							":ext: cvsroot.  @cvsroot=" +cvsr);
				}
				
				ExtConnection extconn = new ExtConnection(cvsExt);
				extconn.setRepository(root.getRepository());
				
				conn = extconn;
			} else {
				
				throw new RuntimeException("Unable to build connection for " +
						cvsr);
			}
			
			
			// Open connection/create client...
		    try {
                conn.open();
            }
            catch (CommandAbortedException cae) {
                throw new RuntimeException("Error opening CVS connection: " +
                          "\n\t\tCVSROOT:  " + cvsr +
                          "\n\t\tENCODED_PASSWORD:  " + passwd +
                          "\n\t\tCVS_EXT:  " + cvsExt +
                          "\n\t\tADAPTER:  " + cvsAdapter.getClass().getName(), cae);
            }
            catch (AuthenticationException ae) {
                throw new RuntimeException("Error authenticating CVS connection: " +
                        "\n\t\tCVSROOT:  " + cvsr +
                        "\n\t\tENCODED_PASSWORD:  " + passwd +
                        "\n\t\tCVS_EXT:  " + cvsExt +
                        "\n\t\tADAPTER:  " + cvsAdapter.getClass().getName(), ae);
            }
		    
			Client client = new Client(conn, new StandardAdminHandler());
			client.getEventManager().addCVSListener(cvsAdapter);
						
			// Execute Children...
			res.setAttribute((String) attribute_name.evaluate(req, res), client);
			res.setAttribute(CvsAttributes.CVSROOT, cvsr);
			super.performSubtasks(req, res);
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (IOException ioe) {
			    if (log.isDebugEnabled()) {
			        log.debug("Failed to close CVS connection properly, this can be ignored", ioe);
			    }
			    else {
			        log.warn("Failed to close CVS connection properly, this can be ignored, set logging to DEBUG for stack trace.");
			    }
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

            if (!e.isTagged()) {
            	// Simple case -- write the msg...
                if (msg.length() > 0 && !System.getProperty("line.separator").equals(msg)) {
                    stream.println(msg);
                }
            } else {
                /*
                 * This is a sticky wicket.  The cvsclient lib wants us to use 
                 * MessageEvent.parseTaggedMessage(StringBuffer,String) to 
                 * gather message chunks until we have a complete line.  But we 
                 * can't (easily) hold on to a StringBuffer due to the 
                 * non-stateful, REST-like requirements of Tasks.
                 * 
                 * We need to (try very hard to) process each MessageEvent 
                 * completely with each call to messageSent().  To that end, 
                 * we're loosely duplicating/approximating the behavior of 
                 * MessageEvent.parseTaggedMessage() right here.
                 */
                if (msg.charAt(0) == '+' || msg.charAt(0) == '-') {
                    return;
                }
                if (msg.equals("newline") /* WARN:  magic string */ || msg.startsWith("\n")) {
                    stream.println();
                } else if (msg.indexOf(' ') > 0) {
                    stream.print(msg.substring(msg.indexOf(' ') + 1));
                }
            }

        }
        
    }
	
}
