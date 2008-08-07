package org.danann.cernunnos.core;

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

@Deprecated
public class NamedSequencePhrase implements Phrase {
	
	/*
	 * Public API.
	 */
	
	public static final Reagent SEQUENCE = new SimpleReagent("SEQUENCE", "descendant-or-self::text()", 
				ReagentType.PHRASE, String.class, "Optional name of a sequence from which to pull "
				+ "the next value.  If the specified name is not recognized, a new sequence will "
				+ "be started.", new LiteralPhrase("DEFAULT_SEQUENCE_NAME"));

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {SEQUENCE};
		return new SimpleFormula(getClass(), reagents);
	}

	public void init(EntityConfig config) {

		String msg = "The 'NamedSequencePhrase' implementation is plain " +
								"dangerous and MUST NOT BE USED!";
		throw new UnsupportedOperationException(msg);

	}

	public Object evaluate(TaskRequest req, TaskResponse res) {
		
		String msg = "The 'NamedSequencePhrase' implementation is plain " +
		"dangerous and MUST NOT BE USED!";
		throw new UnsupportedOperationException(msg);
		
	}

}
