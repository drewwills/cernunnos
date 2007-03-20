package org.danann.cernunnos.xml;

import org.dom4j.Branch;
import org.dom4j.DocumentFactory;

import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public final class NewDocumentPhrase  implements Phrase {
	
	// Instance Members.
	private Phrase name;

	/*
	 * Public API.
	 */

	public static final Reagent NAME = new SimpleReagent("NAME", "descendant-or-self::text()", ReagentType.PHRASE, String.class, 
			"Optional element name.  If provided, this task will create a root element with this NAME and return it.", null);

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {NAME};
		return new SimpleFormula(NewDocumentPhrase.class, reagents);
	}
	
	public void init(EntityConfig config) {

		// Instance Members.
		this.name = (Phrase) config.getValue(NAME);
		
	}

	public Object evaluate(TaskRequest req, TaskResponse res) {

		Branch rslt = new DocumentFactory().createDocument();
		if (name != null) {
			rslt = rslt.addElement((String) name.evaluate(req, res));
		}
		return rslt;
		
	}
	
}