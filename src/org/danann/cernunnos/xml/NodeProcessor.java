package org.danann.cernunnos.xml;

import java.util.Iterator;

import org.dom4j.Branch;
import org.dom4j.Node;

import org.danann.cernunnos.Grammar;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public final class NodeProcessor {

	/*
	 * Public API.
	 */

	public static void evaluatePhrases(Node n, Grammar g, TaskRequest req, TaskResponse res) {
		
		// Assertions...
		if (n == null) {
			String msg = "Argument 'n [Node]' cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		if (g == null) {
			String msg = "Argument 'g [Grammar]' cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		
		if (n instanceof Branch) {
			((Branch) n).normalize();
		}
		
		String xpath = "descendant-or-self::text() | descendant-or-self::*/@*";
		for (Iterator it = n.selectNodes(xpath).iterator(); it.hasNext();) {
			Node d =  (Node) it.next();
			if (d.getText().trim().length() != 0) {
				Phrase p = g.newPhrase(d.getText());
				Object o = p.evaluate(req, res);
				String value = o != null ? o.toString() : "null";
				d.setText(value.toString());
			}
		}
				
	}
	
	/*
	 * Implementation.
	 */

	private NodeProcessor() {}
	
}