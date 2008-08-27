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

package org.danann.cernunnos.core;

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

public final class SequenceTask extends AbstractContainerTask {
		
	// Instance Members.
	private Phrase sequenceName;
	private Phrase start;
	private Phrase increment;

	/*
	 * Public API.
	 */
	
	public static final Reagent SEQUENCE_NAME = new SimpleReagent("SEQUENCE_NAME", "@sequence-name", ReagentType.PHRASE, 
				String.class, "Optional name for the sequence created by this task.  If omitted, the name " +
				"'SequenceTask.DEFAULT_SEQUENCE_NAME' will be used.", new LiteralPhrase("SequenceTask.DEFAULT_SEQUENCE_NAME"));

	public static final Reagent START = new SimpleReagent("START", "@start", ReagentType.PHRASE, Integer.class, 
				"Optional integer which will be the first number used in the sequence.  If omitted, one " +
				"(1) will be used.", new LiteralPhrase(new Integer(1)));

	public static final Reagent INCREMENT = new SimpleReagent("INCREMENT", "@increment", ReagentType.PHRASE, Integer.class, 
				"Optional integer amount by which to increment the sequence each time it is used.  INCREMENT may be " +
				"positive or negative.  If omitted, one (1) will be used.", new LiteralPhrase(new Integer(1)));

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {SEQUENCE_NAME, START, INCREMENT, AbstractContainerTask.SUBTASKS};
		final Formula rslt = new SimpleFormula(getClass(), reagents);
		return rslt;
	}

	public void init(EntityConfig config) {

		super.init(config);

		// Instance Members.
		this.sequenceName = (Phrase) config.getValue(SEQUENCE_NAME);
		this.start = (Phrase) config.getValue(START);
		this.increment = (Phrase) config.getValue(INCREMENT);

	}

	public void perform(TaskRequest req, TaskResponse res) {

		String n = (String) sequenceName.evaluate(req, res);
		Integer s = (Integer) start.evaluate(req, res);
		Integer m = (Integer) increment.evaluate(req, res);
		
		res.setAttribute(n, new SequenceImpl(s, m));
		super.performSubtasks(req, res);
		
	}
	
	/*
	 * Nested Types.
	 */
	
	public static final class SequenceImpl {
		
		// Instance members.
		private int next;
		private final int increment;
		
		/*
		 * Public API.
		 */
		
		public SequenceImpl(int start, int increment) {
			
			// Assertions.
			if (increment == 0) {
				String msg = "Argument 'increment' cannot be zero.";
				throw new IllegalArgumentException(msg);
			}
			
			// Instance members.
			this.next = start;
			this.increment = increment;
			
		}
		
		public int next() {
			int rslt = next;
			next += increment;
			return rslt;
		}

	}

}
