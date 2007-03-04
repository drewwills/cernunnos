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

import org.dom4j.Branch;
import org.dom4j.Node;

import org.danann.cernunnos.AbstractContainerTask;
import org.danann.cernunnos.AttributePhrase;
import org.danann.cernunnos.Attributes;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public class AppendNodeTask extends AbstractContainerTask {

	// Instance Members.
	private Phrase node;
	private Phrase parent;
	private Phrase sibling;

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

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {NODE, PARENT, SIBLING, AbstractContainerTask.SUBTASKS};
		final Formula rslt = new SimpleFormula(AppendNodeTask.class, reagents);
		return rslt;
	}

	public void init(EntityConfig config) {

		super.init(config);		

		// Instance Members.
		this.node = (Phrase) config.getValue(NODE); 
		this.parent = (Phrase) config.getValue(PARENT); 
		this.sibling = (Phrase) config.getValue(SIBLING); 
		
	}

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
			index = 0;
		}
		
		p.content().add(index, node.evaluate(req, res));
		
		super.performSubtasks(req, res);
		
	}

}