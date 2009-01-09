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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.netbeans.lib.cvsclient.Client;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.command.KeywordSubstitutionOptions;
import org.netbeans.lib.cvsclient.command.add.AddCommand;

public class AddTask implements Task {

	// Instance Members.
	private Phrase client;
	private Phrase cvsroot;
	private Phrase local_path;
	private Phrase files;
	private Phrase recurse;
	private Phrase binary_extensions;
	private Phrase message;
	private final Log log = LogFactory.getLog(getClass());

	/*
	 * Public API.
	 */

	public static final Reagent CLIENT = new SimpleReagent("CLIENT", "@client", ReagentType.PHRASE, Client.class,
					"Optional Client object to use in performing this add.  If omitted, the value of the " +
					"'CvsAttributes.CLIENT' request attribute will be used.", new AttributePhrase(CvsAttributes.CLIENT));

	public static final Reagent CVSROOT = new SimpleReagent("CVSROOT", "@cvsroot", ReagentType.PHRASE, String.class,
					"Optional CVSRoot string for preparing the GlobalOptions (e.g. ':pserver:user@host:/usr/local/cvsroot').  " +
					"If omitted, the value of the 'CvsAttributes.CVSROOT' request attribute will be used.", 
					new AttributePhrase(CvsAttributes.CVSROOT));

	public static final Reagent LOCAL_PATH = new SimpleReagent("LOCAL_PATH", "@local-path", ReagentType.PHRASE, String.class,
					"Path expression indicating the directory where the application is logically working from.");

	public static final Reagent FILES = new SimpleReagent("FILES", "@files", ReagentType.PHRASE, Object.class,
					"Either a List of String objects or a single String indicating the files to add.  If " +
					"RECURSE is 'true' (default), each directory specified will also have its entire contents removed.");

	public static final Reagent RECURSE = new SimpleReagent("RECURSE", "@recurse", ReagentType.PHRASE, Boolean.class,
					"Optional Boolean expression specifying whether to add these FILES recursively or not.  " +
					"The default is Boolean.TRUE.", new LiteralPhrase(Boolean.TRUE));

	public static final Reagent BINARY_EXTENSIONS = new SimpleReagent("BINARY_EXTENSIONS", "@binary-extensions", 
					ReagentType.PHRASE, List.class, "Optional List of file extensions (e.g. 'gif', 'jar', " +
					"etc.) that will be added as binary files using the '-kb' option applied.  NB:  specify " +
					"extentions in *lower case* and *without* preceeding period characters ('.'); e.g. 'zip' " +
					"not '.zip'", Collections.EMPTY_LIST);

	public static final Reagent MESSAGE = new SimpleReagent("MESSAGE", "@message", ReagentType.PHRASE, String.class,
					"Optional message to include for the CVS add.", null);

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {CLIENT, CVSROOT, LOCAL_PATH, FILES, 
									RECURSE, BINARY_EXTENSIONS, MESSAGE};
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
		this.binary_extensions = (Phrase) config.getValue(BINARY_EXTENSIONS);
		this.message = (Phrase) config.getValue(MESSAGE);

	}

	public void perform(TaskRequest req, TaskResponse res) {
		
		Client c = (Client) client.evaluate(req, res);
		String path = (String) local_path.evaluate(req, res);
		c.setLocalPath(path);
		String cvsr = (String) cvsroot.evaluate(req, res);

		Boolean r = (Boolean) recurse.evaluate(req, res);
		
		List<File> fList = new LinkedList<File>();
		FileHelper h = new FileHelper();
		Object filesValue = files.evaluate(req, res);
		if (filesValue instanceof List) {
			for (Object o : (List<?>) filesValue) {
				fList.addAll(h.collectFiles(path, (String) o, r));
			}
		} else if (filesValue instanceof String) {
			fList.addAll(h.collectFiles(path, (String) filesValue, r));
		}
		
		// Divide files into normal/binary...
		List<?> bext = (List<?>) binary_extensions.evaluate(req, res);
		List<File> normalFiles = new LinkedList<File>();
		List<File> binaryFiles = new LinkedList<File>();
		for (File f : fList) {
			if (!f.isFile()) {
				// Directories aren't binary...
				if (log.isTraceEnabled()) {
					log.trace("Adding normal file:  " + f.getAbsolutePath());
				}
				normalFiles.add(f);
			}
			String[] tokens = f.getName().split("\\.");
			if (bext.contains(tokens[tokens.length - 1].toLowerCase())) {
				if (log.isTraceEnabled()) {
					log.trace("Adding binary file:  " + f.getAbsolutePath());
				}
				binaryFiles.add(f);
			} else {
				if (log.isTraceEnabled()) {
					log.trace("Adding normal file:  " + f.getAbsolutePath());
				}
				normalFiles.add(f);
			}
		}

		// Add the message...
		String m = null;
		if (message != null) {
			m = (String) message.evaluate(req, res);
		}		


		GlobalOptions optns = new GlobalOptions();
		optns.setCVSRoot(cvsr);
		
		// Normal Files...
		if (!normalFiles.isEmpty()) {
			AddCommand normal = new AddCommand();
			if (m != null) normal.setMessage(m);
			normal.setFiles(normalFiles.toArray(new File[normalFiles.size()]));
			executeCommand(c, normal, optns, m, path, r, fList);
		}

		// Binary Files...
		if (!binaryFiles.isEmpty()) {
			AddCommand binary = new AddCommand();
			binary.setKeywordSubst(KeywordSubstitutionOptions.BINARY);
			if (m != null) binary.setMessage(m);
			binary.setFiles(binaryFiles.toArray(new File[binaryFiles.size()]));
			executeCommand(c, binary, optns, m, path, r, fList);
		}
	}

    private void executeCommand(Client c, AddCommand cmd, GlobalOptions optns, String message,
            String path, Boolean recurse, List<File> fList) {
        
        try {
            c.executeCommand(cmd, optns);
        }
        catch (Exception e) {
            StringBuilder msg = new StringBuilder();
            msg.append("Unable to perform the specified CVS add:")
                        .append("\n\t\tLOCAL_PATH:  ").append(path)
                        .append("\n\t\tRECURSE:  ").append(recurse)
                        .append("\n\t\tMESSAGE:  ").append(message)
                        .append("\n\t\tMatching Files:  (below)");
            for (File f : fList) {
                msg.append("\n\t\t\t").append(f.getAbsolutePath());
            }
            throw new RuntimeException(msg.toString(), e);
        }
    }

}
