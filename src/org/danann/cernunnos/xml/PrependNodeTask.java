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

import java.util.LinkedList;
import java.util.List;

import org.dom4j.Branch;
import org.dom4j.Node;

import org.danann.cernunnos.AttributePhrase;
import org.danann.cernunnos.Attributes;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Grammar;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.Task;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public final class PrependNodeTask implements Task {

	// Instance Members.
	private Phrase node;
	private Phrase parent;
	private Phrase sibling;
	private List content;
	private Grammar grammar;

	/*
	 * Public API.
	 */

	public static final Reagent NODE = new SimpleReagent("NODE", "@node", ReagentType.PHRASE, Node.class,
					"Optional node that will be prepended.  If not provided, the 'Attributes.NODE' "
					+ "request attribute will be used.", new AttributePhrase(Attributes.NODE));

	public static final Reagent PARENT = new SimpleReagent("PARENT", "@parent", ReagentType.PHRASE, Node.class,
					"Optional node under which the specified content will be added at the beginning.  Specify "
					+ "only PARENT or SIBLING, not both.  If neither is specified, the 'Attributes.NODE' "
					+ "request attribute will be used as a PARENT.", new AttributePhrase(Attributes.NODE));

	public static final Reagent SIBLING = new SimpleReagent("SIBLING", "@sibling", ReagentType.PHRASE, Node.class,
					"Optional node before which the specified content will be added.  Specify only PARENT or "
					+ "SIBLING, not both.", null);

	public static final Reagent CONTENT = new SimpleReagent("CONTENT", "*", ReagentType.NODE_LIST, List.class,
					"Optional XML nodes to prepend.  Use this reagent to specify content in-line.  If "
					+ "CONTENT is present, it will be prefered over NODE.", null);


	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {NODE, PARENT, SIBLING, CONTENT};
		final Formula rslt = new SimpleFormula(PrependNodeTask.class, reagents);
		return rslt;
	}

	public void init(EntityConfig config) {

		// Instance Members.
		this.node = (Phrase) config.getValue(NODE);
		this.parent = (Phrase) config.getValue(PARENT);
		this.sibling = (Phrase) config.getValue(SIBLING);
		this.content = (List) config.getValue(CONTENT);
		this.grammar = config.getGrammar();

	}

	@SuppressWarnings("unchecked")
	public void perform(TaskRequest req, TaskResponse res) {


		// Figure out where to put the content...
		Branch p = null;
		int index;
		if (sibling != null) {
			Node sib = (Node) sibling.evaluate(req, res);
			p = sib.getParent();
			index = p.indexOf(sib);
		} else {
			// Work from the PARENT...
			p = (Branch) parent.evaluate(req, res);
			index = 0;
		}

		// Figure out what content to add...
		List list = null;
		if (content != null && content.size() > 0) {
			list = content;
		} else {
			list = new LinkedList();
			list.add(node.evaluate(req, res));
		}

		// Evaluate phrases & add...
		for (Object o : list) {
			Node n = (Node) ((Node) o).clone();
			NodeProcessor.evaluatePhrases(n, grammar, req, res);
			p.content().add(index++, n);
		}

	}

}