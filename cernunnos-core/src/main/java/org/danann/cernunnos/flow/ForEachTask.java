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

package org.danann.cernunnos.flow;

import java.util.Enumeration;
import java.util.Iterator;

import org.danann.cernunnos.AbstractContainerTask;
import org.danann.cernunnos.Attributes;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.LiteralPhrase;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public class ForEachTask extends AbstractContainerTask {

	// Instance Members.
	private Phrase attribute_name;
	private Phrase items;

	/*
	 * Public API.
	 */

	public static final Reagent ATTRIBUTE_NAME = new SimpleReagent("ATTRIBUTE_NAME", "@attribute-name", ReagentType.PHRASE, String.class,
					"Optional name under which each item will be registered as a request attribute.  If omitted, " +
					"the name 'Attributes.OBJECT' will be used.", new LiteralPhrase(Attributes.OBJECT));

	public static final Reagent ITEMS = new SimpleReagent("ITEMS", "@items", ReagentType.PHRASE, Object.class,
					"Items that will be iterated over;  specify either an instance of java.lang.Iterable, " +
					"java.util.Enumeration, java.util.Iterator, or an array.  If omitted, this task will use the " +
					"value of the 'Attributes.OBJECT' request attribute.", new LiteralPhrase(Attributes.OBJECT));

	public Formula getFormula() {
		final Reagent[] reagents = new Reagent[] {ATTRIBUTE_NAME, ITEMS, AbstractContainerTask.SUBTASKS};
		final Formula rslt = new SimpleFormula(ForEachTask.class, reagents);
		return rslt;
	}

	public void init(EntityConfig config) {

		super.init(config);

		// Instance Members.
		this.attribute_name = (Phrase) config.getValue(ATTRIBUTE_NAME);
		this.items = (Phrase) config.getValue(ITEMS);

	}

	public void perform(TaskRequest req, TaskResponse res) {

		final String attr = (String) attribute_name.evaluate(req, res);

		// Figure out what ITEMS is and how to traverse it...
		Object itm = items.evaluate(req, res);
		if (itm instanceof Iterable) {
			final Iterable<?> it = (Iterable<?>) itm;
			for (Object o : it) {
				res.setAttribute(attr, o);
				super.performSubtasks(req, res);
			}
		} else if (itm instanceof Object[]) {
			final Object[] it = (Object[]) itm;
			for (Object o : it) {
				res.setAttribute(attr, o);
				super.performSubtasks(req, res);
			}
		} else if (itm instanceof Iterator) {
			final Iterator<?> it = (Iterator<?>) itm;
			while (it.hasNext()) {
				res.setAttribute(attr, it.next());
				super.performSubtasks(req, res);
			}
		} else if (itm instanceof Enumeration) {
			final Enumeration<?> it = (Enumeration<?>) itm;
			while (it.hasMoreElements()) {
				res.setAttribute(attr, it.nextElement());
				super.performSubtasks(req, res);
			}
		} else {
			final String msg = "Unsupported type for ITEMS reagent:  " 
									+ itm.getClass().getName();
			throw new RuntimeException(msg);
		}

	}

}
