package org.danann.cernunnos.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
public final class NewInstancePhrase implements Phrase {

	// Instance Members.
	private Phrase clazz;
	private final Log log = LogFactory.getLog(getClass());	// Don't declare as static in general libraries

	/*
	 * Public API.
	 */

	public static final Reagent CLASS_NAME = new SimpleReagent("CLASS_NAME", "descendant-or-self::text()",
				ReagentType.PHRASE, String.class, "The name of a Java class to be instantiated.");

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {CLASS_NAME};
		return new SimpleFormula(getClass(), reagents);
	}

	public void init(EntityConfig config) {

		// Instance Members.
		this.clazz = (Phrase) config.getValue(CLASS_NAME);

	}

	public Object evaluate(TaskRequest req, TaskResponse res) {

		String s = (String) clazz.evaluate(req, res);
		try {
			return Class.forName(s).newInstance();
		} catch (ClassNotFoundException cfne) {
			if (log.isErrorEnabled()) {
				StringBuilder msg = new StringBuilder();
				msg.append("Unable to instantiate the specified class (").append(s)
						.append(");  Check to be sure the class is present in the classpath.");
				log.error(msg.toString());
			}
			throw new RuntimeException(cfne);
		} catch (Throwable t) {
			String msg = "Unable to instantiate the specified class:  " + s;
			throw new RuntimeException(msg, t);
		}

	}

}
