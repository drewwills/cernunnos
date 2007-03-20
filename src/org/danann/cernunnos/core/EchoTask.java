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

import java.io.PrintStream;

import org.danann.cernunnos.AttributePhrase;
import org.danann.cernunnos.Attributes;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.LiteralPhrase;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.Task;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public final class EchoTask implements Task {

	// Instance Members.
	private Phrase stream;
	private Phrase prefix;
	private Phrase message;
	private Phrase suffix;

	/*
	 * Public API.
	 */

	public static final Reagent STREAM = new SimpleReagent("STREAM", "@stream", ReagentType.PHRASE, PrintStream.class,
					"Optional PrintStream to which the message should be written.  If omitted, this task will use "
					+ "either: (1) the value of the 'Attributes.STREAM' request attribute if present; or (2) "
					+ "System.out.", new AttributePhrase(Attributes.STREAM, new LiteralPhrase(System.out)));

	public static final Reagent PREFIX = new SimpleReagent("PREFIX", "@prefix", ReagentType.PHRASE, String.class,
					"Characters that preceed the main message.  The default is an empty string.", new LiteralPhrase(""));
	
	public static final Reagent MESSAGE = new SimpleReagent("MESSAGE", "text()", ReagentType.PHRASE, String.class, 
					"Message to write to the specified PrintStream.  The default is an empty string.", new LiteralPhrase(""));

	public static final Reagent SUFFIX = new SimpleReagent("SUFFIX", "@suffix", ReagentType.PHRASE, String.class,
					"Characters that follow the main message.  The default is an empty string.", new LiteralPhrase(""));

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {STREAM, PREFIX, MESSAGE, SUFFIX};
		final Formula rslt = new SimpleFormula(EchoTask.class, reagents);
		return rslt;
	}

	public void init(EntityConfig config) {

		// Instance Members.
		this.stream = (Phrase) config.getValue(STREAM); 
		this.prefix = (Phrase) config.getValue(PREFIX); 
		this.message = (Phrase) config.getValue(MESSAGE); 
		this.suffix = (Phrase) config.getValue(SUFFIX); 
		
	}

	public void perform(TaskRequest req, TaskResponse res) {

		PrintStream ps = (PrintStream) stream.evaluate(req, res);
		ps.print(prefix.evaluate(req, res));		
		ps.print(message.evaluate(req, res));		
		ps.print(suffix.evaluate(req, res));		
		
	}
	
}