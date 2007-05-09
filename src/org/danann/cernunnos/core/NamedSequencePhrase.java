package org.danann.cernunnos.core;

import java.util.HashMap;
import java.util.Map;

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

public class NamedSequencePhrase implements Phrase {

	// Static Members.
	private static final Map<String,Integer> sequences = new HashMap<String,Integer>();
	
	// Instance Members.
	private Phrase sequence;
	
	/*
	 * Public API.
	 */
	
	public static final Reagent SEQUENCE = new SimpleReagent("SEQUENCE", "descendant-or-self::text()", 
				ReagentType.PHRASE, String.class, "Optional name of a sequence from which to pull "
				+ "the next value.  If the specified name is not recognized, a new sequence will "
				+ "be started.", new LiteralPhrase("DEFAULT_SEQUENCE_NAME"));

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {SEQUENCE};
		return new SimpleFormula(NamedSequencePhrase.class, reagents);
	}

	public void init(EntityConfig config) {

		// Instance Members.
		this.sequence = (Phrase) config.getValue(SEQUENCE);

	}

	public Object evaluate(TaskRequest req, TaskResponse res) {
		
		String name = (String) sequence.evaluate(req, res);

		int rslt = (sequences.containsKey(name) ? sequences.get(name) : 0) + 1;
		sequences.put(name, rslt);
		
		return rslt;
		
	}

}