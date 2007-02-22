package org.danann.cernunnos.xml;

import org.dom4j.Node;

import org.danann.cernunnos.AttributePhrase;
import org.danann.cernunnos.Attributes;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.Task;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public class DeleteNodeTask implements Task {

	// Instance Members.
	private Phrase node;

	/*
	 * Public API.
	 */
	
	public static final Reagent NODE = new SimpleReagent("NODE", "@node", ReagentType.PHRASE, Node.class,
							"Optional node to be deleted.  If not provided, the value of the "
							+ "'Attributes.NODE' request attribute will be used.", 
							new AttributePhrase(Attributes.NODE));

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {NODE};
		final Formula rslt = new SimpleFormula(DeleteNodeTask.class, reagents);
		return rslt;
	}

	public void init(EntityConfig config) {

		// Instance Members.
		this.node = (Phrase) config.getValue(NODE); 
		
	}

	public void perform(TaskRequest req, TaskResponse res) {
		
		try {
			Node n = (Node) node.evaluate(req, res);
			n.getParent().remove(n);
		} catch (Throwable t) {
			String msg = "Unable to delete the specified node.";
			throw new RuntimeException(msg, t);
		}
				
	}
	
}