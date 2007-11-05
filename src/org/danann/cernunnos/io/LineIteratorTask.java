package org.danann.cernunnos.io;

import org.danann.cernunnos.AbstractContainerTask;
import org.danann.cernunnos.Attributes;
import org.danann.cernunnos.AttributePhrase;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public class LineIteratorTask extends AbstractContainerTask {

	// Instance Members.
	private Phrase string;

	/*
	 * Public API.
	 */

	public static final Reagent STRING = new SimpleReagent("STRING", "@string", ReagentType.PHRASE,
			String.class, "Textual content that will be iterated over line-by-line.",
			new AttributePhrase(Attributes.STRING));

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {STRING, AbstractContainerTask.SUBTASKS};
		final Formula rslt = new SimpleFormula(LineIteratorTask.class, reagents);
		return rslt;
	}

	public void init(EntityConfig config) {

		super.init(config);

		// Instance Members.
		this.string = (Phrase) config.getValue(STRING);

	}

	public void perform(TaskRequest req, TaskResponse res) {

		String str = (String) string.evaluate(req, res);
		String[] tokens = str.split("\n");

		for (String k : tokens) {
			res.setAttribute(Attributes.STRING, k);
			super.performSubtasks(req, res);
		}

	}
}
