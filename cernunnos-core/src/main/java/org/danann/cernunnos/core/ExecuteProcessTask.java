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

package org.danann.cernunnos.core;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

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

public final class ExecuteProcessTask implements Task {
    
    private static final Pattern CMD_TOKEN_DELIM = Pattern.compile("\\s");

	// Instance Members.
	private Phrase cmd;
	private Phrase from;

	/*
	 * Public API.
	 */

	public static final Reagent CMD = new SimpleReagent("COMMAND", "text()", ReagentType.PHRASE, String.class,
											"The command to execute within a new process.");

	public static final Reagent FROM = new SimpleReagent("FROM", "@from", ReagentType.PHRASE, String.class,
						"The working directory from which the process should "
						+ "be executed.  Default is the current directory.", new LiteralPhrase("."));

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {CMD, FROM};
		final Formula rslt = new SimpleFormula(ExecuteProcessTask.class, reagents);
		return rslt;
	}

	public void init(EntityConfig config) {

		// Instance Members.
		this.cmd = (Phrase) config.getValue(CMD);
		this.from = (Phrase) config.getValue(FROM);

	}

	public void perform(TaskRequest req, TaskResponse res) {

		int value;
		InputStream inpt = null;
		String fullCmd = (String) cmd.evaluate(req, res);
		try {

			// Prepare the command.
			String[] tokens = CMD_TOKEN_DELIM.split(fullCmd);
			List<String> list = Arrays.asList(tokens);

			ProcessBuilder pb = new ProcessBuilder(list);
			pb.redirectErrorStream(true);
			pb.directory(new File((String) from.evaluate(req, res)));

			Process p = pb.start();
			value = p.waitFor();

			inpt = new BufferedInputStream(p.getInputStream());

			StringBuffer buff = new StringBuffer();
			byte[] bytes = new byte[1024];
			for (int len = inpt.read(bytes); len > 0; len = inpt.read(bytes)) {
				buff.append(new String(bytes, 0, len));
			}

			System.out.println(buff.toString());

		} catch (Throwable t) {
			String msg = "ExecuteProcessTask encountered a problem invoking the specified command:  " + fullCmd;
			throw new RuntimeException(msg, t);
		} finally {
			if (inpt != null) {
				try {
					inpt.close();
				} catch (IOException ioe) {
					String msg = "ExecuteProcessTask failed to close the process InputStream.";
					throw new RuntimeException(msg, ioe);
				}
			}
		}

		if (value != 0) {
			String msg = "The process terminated abnormally.  Exit value was:  " + value;
			throw new RuntimeException(msg);
		}

	}

}