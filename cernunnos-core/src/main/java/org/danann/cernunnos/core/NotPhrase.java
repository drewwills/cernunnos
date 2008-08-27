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

@Deprecated
public class NotPhrase implements Phrase {

	// Instance Members.
	private Phrase value;

	/*
	 * Public API.
	 */
	
	public static final Reagent VALUE = new SimpleReagent("VALUE", "descendant-or-self::text()", 
			ReagentType.PHRASE, Boolean.class, "A phrase that evaluates to a Boolean.");

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {VALUE};
		return new SimpleFormula(getClass(), reagents);
	}
	
	public void init(EntityConfig config) {

		// Instance Members.
		this.value = (Phrase) config.getValue(VALUE);

	}

	public Object evaluate(TaskRequest req, TaskResponse res) {

		Boolean b = (Boolean) value.evaluate(req, res);
		return !b.booleanValue();
		
	}

}
