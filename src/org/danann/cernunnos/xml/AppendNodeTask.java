/*
 * Copyright 2007 Andrew Wills
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.danann.cernunnos.xml;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Branch;
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

public class AppendNodeTask implements Task {

	// Instance Members.
	private Phrase node;
	private List content;
	private Phrase to_node;

	/*
	 * Public API.
	 */

	public static final Reagent NODE = new SimpleReagent("NODE", "@node", ReagentType.PHRASE, Node.class,
					"Optional node that will be added to TO_NODE.  If not provided, CONTENT "
					+ "will be added instead.", null);

	public static final Reagent CONTENT = new SimpleReagent("CONTENT", "*", ReagentType.NODE_LIST, List.class,
					"Content that will be added to TO_NODE if NODES is not specified.  The default "
					+ "is an empty list.", Collections.emptyList());


	public static final Reagent TO_NODE = new SimpleReagent("TO_NODE", "@to-node", ReagentType.PHRASE, Node.class,
					"Optional node to which the CONTENT collection will be added.  If not provided, the value of "
					+ "the 'Attributes.NODE' request attribute will be used.", 
					new AttributePhrase(Attributes.NODE));

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {NODE, CONTENT, TO_NODE};
		final Formula rslt = new SimpleFormula(AppendNodeTask.class, reagents);
		return rslt;
	}

	public void init(EntityConfig config) {

		// Instance Members.
		this.node = (Phrase) config.getValue(NODE); 
		this.content = (List) config.getValue(CONTENT); 
		this.to_node = (Phrase) config.getValue(TO_NODE); 
		
	}

	public void perform(TaskRequest req, TaskResponse res) {
		
		Node n = (Node) to_node.evaluate(req, res);
		if (!(n instanceof Branch)) {
			String msg = "Unable to append.  The specified TO_NODE is not an instance of org.dom4j.Branch.";
			throw new RuntimeException(msg);
		}

		// Choose NODES or CONTENT...
		List list = null;
		if (node != null) {
			Node child = (Node) node.evaluate(req, res);
			list = Arrays.asList(new Object[] { child });
		} else {
			list = content;
		}
		
		// Add the new children...
		Branch b = (Branch) n;
		for (Iterator it = list.iterator(); it.hasNext();) {
			Node child = (Node) it.next();
			Node clone = (Node) child.clone();
			b.add((Node) clone.detach());
		}
		
	}

}