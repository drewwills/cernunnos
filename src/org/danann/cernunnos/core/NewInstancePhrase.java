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

public final class NewInstancePhrase implements Phrase {

	// Instance Members.
	private Phrase clazz;

	/*
	 * Public API.
	 */

	public static final Reagent CLASS_NAME = new SimpleReagent("CLASS_NAME", "descendant-or-self::text()",
				ReagentType.PHRASE, String.class, "The name of a Java class to be instantiated.");

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {CLASS_NAME};
		return new SimpleFormula(NewInstancePhrase.class, reagents);
	}

	public void init(EntityConfig config) {

		// Instance Members.
		this.clazz = (Phrase) config.getValue(CLASS_NAME);

	}

	public Object evaluate(TaskRequest req, TaskResponse res) {

		String s = (String) clazz.evaluate(req, res);
		try {
			return Class.forName(s).newInstance();
		} catch (Throwable t) {
			String msg = "Unable to instantiate the specified class:  " + s;
			throw new RuntimeException(msg, t);
		}

	}

}
