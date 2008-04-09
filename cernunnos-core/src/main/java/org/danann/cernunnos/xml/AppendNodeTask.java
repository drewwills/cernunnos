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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Attribute;
import org.dom4j.Branch;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.Node;

import org.danann.cernunnos.AttributePhrase;
import org.danann.cernunnos.Attributes;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Grammar;
import org.danann.cernunnos.LiteralPhrase;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.Task;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public final class AppendNodeTask implements Task {

	// Instance Members.
	private Phrase node;
	private Phrase parent;
	private Phrase sibling;
	private List<?> content;
	private Phrase apply_namespace;
	private Grammar grammar;
	private final Log log = LogFactory.getLog(AppendNodeTask.class);	// Don't declare as static in general libraries

	/*
	 * Public API.
	 */

	public static final Reagent NODE = new SimpleReagent("NODE", "@node", ReagentType.PHRASE, Node.class,
					"Optional node that will be appended.  If not provided, the 'Attributes.NODE' "
					+ "request attribute will be used.", new AttributePhrase(Attributes.NODE));

	public static final Reagent PARENT = new SimpleReagent("PARENT", "@parent", ReagentType.PHRASE, Node.class,
					"Optional node under which the specified content will be added.  Specify only PARENT or "
					+ "SIBLING, not both.  If neither is specified, the 'Attributes.NODE' request attribute "
					+ "will be used as a PARENT.", new AttributePhrase(Attributes.NODE));

	public static final Reagent SIBLING = new SimpleReagent("SIBLING", "@sibling", ReagentType.PHRASE, Node.class,
					"Optional node after which the specified content will be added.  Specify only PARENT or "
					+ "SIBLING, not both.", null);

	public static final Reagent CONTENT = new SimpleReagent("CONTENT", "*", ReagentType.NODE_LIST, List.class,
					"Optional XML nodes to append.  Use this reagent to specify content in-line.  If "
					+ "CONTENT is present, it will be prefered over NODE.", null);

	public static final Reagent APPLY_NAMESPACE = new SimpleReagent("APPLY_NAMESPACE", "@apply-namespace", ReagentType.PHRASE, 
					Boolean.class, "Tells this task whether to reconstruct the QNames of added elements to include the " +
					"namespace of the parent element if:  (1) the parent contains a namespace;  and (2) the intended child " +
					"does not.  The default is Boolean.TRUE.", new LiteralPhrase(Boolean.TRUE));

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {NODE, PARENT, SIBLING, CONTENT, APPLY_NAMESPACE};
		final Formula rslt = new SimpleFormula(AppendNodeTask.class, reagents);
		return rslt;
	}

	public void init(EntityConfig config) {

		// Instance Members.
		this.node = (Phrase) config.getValue(NODE);
		this.parent = (Phrase) config.getValue(PARENT);
		this.sibling = (Phrase) config.getValue(SIBLING);
		this.content = (List<?>) config.getValue(CONTENT);
		this.apply_namespace = (Phrase) config.getValue(APPLY_NAMESPACE);
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
			index = p.indexOf(sib) + 1;
		} else {
			// Work from the PARENT...
			p = (Branch) parent.evaluate(req, res);
			index = p.content().size();
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

			// If the parent is an element, check if we should
			// carry the parent namespace over to the child...
			if ((Boolean) apply_namespace.evaluate(req, res) &&
					p.getNodeType() == Node.ELEMENT_NODE &&
					!((Element) p).getNamespace().equals(Namespace.NO_NAMESPACE)) {
				// We know the parent is an Element w/ a namespace,
				// is the child (also) an Element w/ none?
				if (n.getNodeType() == Node.ELEMENT_NODE && ((Element) n).getNamespace().equals(Namespace.NO_NAMESPACE)) {
					// Yes -- we need to port the namespace forward...
					Namespace nsp = ((Element) p).getNamespace();
					if (log.isTraceEnabled()) {
						StringBuffer msg = new StringBuffer();
						msg.append("Adding the following namespace to <").append(n.getName())
															.append(">:  ").append(nsp);
						log.trace(msg.toString());
					}
					NodeProcessor.applyNamespace(nsp, (Element) n);
				}
			}

			// Although they *are* nodes, attributes are not technically
			// content, and therefore they must have special treatment...
			if (p.getNodeType() == Node.ELEMENT_NODE && n.getNodeType() == Node.ATTRIBUTE_NODE) {
				// Add attributes by calling addAttribute on the Element contract...
				((Element) p).add((Attribute) n);
			} else {
				// Add everything else as 'content'...
				p.content().add(index++, n);
			}

		}

	}

}