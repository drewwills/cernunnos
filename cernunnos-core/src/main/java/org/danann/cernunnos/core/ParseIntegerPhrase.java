package org.danann.cernunnos.core;

import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public final class ParseIntegerPhrase implements Phrase {

	// Instance Members.
	private Phrase expression;
	
	/*
	 * Public API.
	 */
	
	public static final Reagent EXPRESSION = new SimpleReagent("EXPRESSION", "descendant-or-self::text()", 
								ReagentType.PHRASE, String.class, "String expression representing an Integer.");

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {EXPRESSION};
		return new SimpleFormula(ParseIntegerPhrase.class, reagents);
	}
	
	public void init(EntityConfig config) {

		// Instance Members.
		this.expression = (Phrase) config.getValue(EXPRESSION);

	}

	public Object evaluate(TaskRequest req, TaskResponse res) {

		Integer rslt = null;	// default...
		
		String s = (String) expression.evaluate(req, res);
		if (s != null && s.trim().length() > 0) {
			rslt = Integer.valueOf(s);
		}
		
		return rslt;
		
	}
	
}