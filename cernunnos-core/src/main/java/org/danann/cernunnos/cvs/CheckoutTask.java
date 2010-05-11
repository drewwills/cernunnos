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

import org.netbeans.lib.cvsclient.Client;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.command.checkout.CheckoutCommand;

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

public class CheckoutTask implements Task {

	// Instance Members.
	private Phrase client;
	private Phrase cvsroot;
	private Phrase local_path;
	private Phrase recurse;
	private Phrase module;
	private Phrase dir;
    private Phrase prune_empty_dirs;

	/*
	 * Public API.
	 */

	public static final Reagent CLIENT = new SimpleReagent("CLIENT", "@client", ReagentType.PHRASE, Client.class,
					"Optional Client object to use in performing this checkout.  If omitted, the value of the " +
					"'CvsAttributes.CLIENT' request attribute will be used.", new AttributePhrase(CvsAttributes.CLIENT));

	public static final Reagent CVSROOT = new SimpleReagent("CVSROOT", "@cvsroot", ReagentType.PHRASE, String.class,
					"Optional CVSRoot string for preparing the GlobalOptions (e.g. ':pserver:user@host:/usr/local/cvsroot').  " +
					"If omitted, the value of the 'CvsAttributes.CVSROOT' request attribute will be used.", 
					new AttributePhrase(CvsAttributes.CVSROOT));

	public static final Reagent LOCAL_PATH = new SimpleReagent("LOCAL_PATH", "@local-path", ReagentType.PHRASE, String.class,
					"Path expression indicating the directory where the application is logically working from.");

	public static final Reagent RECURSE = new SimpleReagent("RECURSE", "@recurse", ReagentType.PHRASE, Boolean.class,
					"Optional Boolean expression specifying whether to checkout this module recursively or not.  " +
					"The default is Boolean.TRUE.", new LiteralPhrase(Boolean.TRUE));

	public static final Reagent MODULE = new SimpleReagent("MODULE", "@module", ReagentType.PHRASE, String.class,
					"Name of the module to checkout.");

	public static final Reagent DIR = new SimpleReagent("DIR", "@dir", ReagentType.PHRASE, String.class,
					"Optional directory into which the MODULE should be checked out.  Equivelent to " +
					"the '-d' option in CVS.", null);

	public static final Reagent PRUNE_EMPTY_DIRS = new SimpleReagent("PRUNE_EMPTY_DIRS", "@prune-empty-dirs", ReagentType.PHRASE, Boolean.class,
                     "Optional flag that tells CVS whether or not to check out empty directories.  Equivelent to " +
                     "the '-P' option in the CVS checkout command.  Default is true", new LiteralPhrase(Boolean.TRUE));

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {CLIENT, CVSROOT, LOCAL_PATH, 
													RECURSE, MODULE, DIR,
                                                    PRUNE_EMPTY_DIRS};

		final Formula rslt = new SimpleFormula(getClass(), reagents);
		return rslt;
	}

	public void init(EntityConfig config) {

		// Instance Members.
		this.client = (Phrase) config.getValue(CLIENT);
		this.cvsroot = (Phrase) config.getValue(CVSROOT);
		this.local_path = (Phrase) config.getValue(LOCAL_PATH);
		this.recurse = (Phrase) config.getValue(RECURSE);
		this.module = (Phrase) config.getValue(MODULE);
		this.dir = (Phrase) config.getValue(DIR);
        this.prune_empty_dirs = (Phrase) config.getValue(PRUNE_EMPTY_DIRS);

	}
	
	public void perform(TaskRequest req, TaskResponse res) {
				
		Client c = (Client) client.evaluate(req, res);
		String path = (String) local_path.evaluate(req, res);
		c.setLocalPath(path);
		String cvsr = (String) cvsroot.evaluate(req, res);

		Boolean r = (Boolean) recurse.evaluate(req, res);
		String mod = (String) module.evaluate(req, res);
        Boolean prune = (Boolean) prune_empty_dirs.evaluate(req, res);

		CheckoutCommand co = new CheckoutCommand(r, mod);
		co.setPruneDirectories(prune.booleanValue());	
		
		if (dir != null) {
			co.setCheckoutDirectory((String) dir.evaluate(req, res));
		}
		
		try {
			GlobalOptions optns = new GlobalOptions();
			optns.setCVSRoot(cvsr);
			c.executeCommand(co, optns);
		} catch (Throwable t) {
			String msg = "Unable to perform the specified CVS checkout:" +
							"\n\t\tMODULE:  " + mod +
							"\n\t\tLOCAL_PATH:  " + path +
							"\n\t\tRECURSE:  " + r;
			throw new RuntimeException(msg, t);
		}

	}

}
