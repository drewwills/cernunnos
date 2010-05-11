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

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.netbeans.lib.cvsclient.Client;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.command.commit.CommitCommand;

import org.danann.cernunnos.AttributePhrase;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.LiteralPhrase;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.Task;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public class CommitTask implements Task {

	// Instance Members.
	private Phrase client;
	private Phrase cvsroot;
	private Phrase local_path;
	private Phrase files;
	private Phrase recurse;
	private Phrase message;

	public static final Reagent CLIENT = new SimpleReagent("CLIENT", "@client", ReagentType.PHRASE, Client.class,
					"Optional Client object to use in performing this commit.  If omitted, the value of the " +
					"'CvsAttributes.CLIENT' request attribute will be used.", new AttributePhrase(CvsAttributes.CLIENT));

	public static final Reagent CVSROOT = new SimpleReagent("CVSROOT", "@cvsroot", ReagentType.PHRASE, String.class,
					"Optional CVSRoot string for preparing the GlobalOptions (e.g. ':pserver:user@host:/usr/local/cvsroot').  " +
					"If omitted, the value of the 'CvsAttributes.CVSROOT' request attribute will be used.", 
					new AttributePhrase(CvsAttributes.CVSROOT));

	public static final Reagent LOCAL_PATH = new SimpleReagent("LOCAL_PATH", "@local-path", ReagentType.PHRASE, String.class,
					"Path expression indicating the directory where the application is logically working from.");

	public static final Reagent FILES = new SimpleReagent("FILES", "@files", ReagentType.PHRASE, Object.class,
					"Either a List of String objects or a single String indicating the files to commit.  If " +
					"RECURSE is 'true' (default), each directory specified will also have its entire contents removed.");

	public static final Reagent RECURSE = new SimpleReagent("RECURSE", "@recurse", ReagentType.PHRASE, Boolean.class,
					"Optional Boolean expression specifying whether to commit these FILES recursively or not.  " +
					"The default is Boolean.TRUE.", new LiteralPhrase(Boolean.TRUE));

	public static final Reagent MESSAGE = new SimpleReagent("MESSAGE", "@message", ReagentType.PHRASE, String.class,
				"Optional message to include with the CVS commit.", null);

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {CLIENT, CVSROOT, LOCAL_PATH, 
												FILES, RECURSE, MESSAGE};
		final Formula rslt = new SimpleFormula(getClass(), reagents);
		return rslt;
	}

	public void init(EntityConfig config) {

		// Instance Members.
		this.client = (Phrase) config.getValue(CLIENT);
		this.cvsroot = (Phrase) config.getValue(CVSROOT);
		this.local_path = (Phrase) config.getValue(LOCAL_PATH);
		this.files = (Phrase) config.getValue(FILES);
		this.recurse = (Phrase) config.getValue(RECURSE);
		this.message = (Phrase) config.getValue(MESSAGE);

	}

	public void perform(TaskRequest req, TaskResponse res) {
		
		Client c = (Client) client.evaluate(req, res);
		String path = (String) local_path.evaluate(req, res);
		c.setLocalPath(path);
		String cvsr = (String) cvsroot.evaluate(req, res);
		
		CommitCommand commit = new CommitCommand();
		boolean r = (Boolean) recurse.evaluate(req, res);
		commit.setRecursive(r);

		// Gather the files.
		List<File> fList = new LinkedList<File>();
		FileHelper h = new FileHelper();
		Object filesValue = files.evaluate(req, res);
		if (filesValue instanceof List) {
			for (Object o : (List<?>) filesValue) {
				// NB:  Recursion handled by setting above...
				fList.addAll(h.collectFiles(path, (String) o, false));
			}
		} else if (filesValue instanceof String) {
			// NB:  Recursion handled by setting above...
			fList.addAll(h.collectFiles(path, (String) filesValue, false));
		}
		commit.setFiles(fList.toArray(new File[fList.size()]));

		// Add the message...
		String m = null;
		if (message != null) {
			m = (String) message.evaluate(req, res);
			commit.setMessage(m); 
		}		

		try {
			GlobalOptions optns = new GlobalOptions();
			optns.setCVSRoot(cvsr);
			c.executeCommand(commit, optns);
		} catch (Throwable t) {
			StringBuilder msg = new StringBuilder();
			msg.append("Unable to perform the specified CVS commit:")
						.append("\n\t\tLOCAL_PATH:  ").append(path)
						.append("\n\t\tRECURSE:  ").append(r)
						.append("\n\t\tMESSAGE:  ").append(m);
			for (File f : fList) {
				msg.append("\n\t\t\t").append(f.getAbsolutePath());
			}
			throw new RuntimeException(msg.toString(), t);
		}

	}
	
}
