package org.danann.cernunnos.core;

import java.io.BufferedInputStream;
import java.io.File;
import java.util.Arrays;
import java.util.List;

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

public class ExecuteProcessTask implements Task {
	
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
		String fullCmd = (String) cmd.evaluate(req, res);
		try {
			
			// Prepare the command.
			String[] tokens = fullCmd.split("\\s");
			List<String> list = Arrays.asList(tokens);
			
			ProcessBuilder pb = new ProcessBuilder(list);
			pb.redirectErrorStream(true);
			pb.directory(new File((String) from.evaluate(req, res)));
			
			Process p = pb.start();

//			Process p = Runtime.getRuntime().exec(fullCmd);
			
			value = p.waitFor();
			
			BufferedInputStream inpt = new BufferedInputStream(p.getInputStream());
			int size = inpt.available();
			byte[] bytes = new byte[size];
			inpt.read(bytes, 0, size);
			System.out.write(bytes);
			
		} catch (Throwable t) {
			String msg = "ExecuteProcessTask encountered a problem invoking the specified command:  " + fullCmd;
			throw new RuntimeException(msg, t);
		}
		
		if (value != 0) {
			String msg = "The process terminated abnormally.  Exit value was:  " + value;
			throw new RuntimeException(msg);
		}
		
	}

}