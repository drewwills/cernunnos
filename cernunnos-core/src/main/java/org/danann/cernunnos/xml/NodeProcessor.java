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

import java.util.Iterator;

import org.dom4j.Branch;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.QName;

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
		for (Iterator<?> it = n.selectNodes(xpath).iterator(); it.hasNext();) {
			Node d =  (Node) it.next();
			if (d.getText().trim().length() != 0) {
				Phrase p = g.newPhrase(d);
				Object o = p.evaluate(req, res);
				String value = o != null ? o.toString() : "null";
				d.setText(value);
			}
		}

	}

	/**
	 * Recursively applies the specified <code>Namespace</code> to the specified
	 * <code>Element</code>, unless the <code>Element</code> (or any child
	 * <code>Element</code>) already specifies a <code>Namespace</code>.
	 *
	 * @param nsp Namespace to apply.
	 * @param e XML structure upon which to apply the Namespace.
	 */
	public static void applyNamespace(Namespace nsp, Element e) {

		// Assertions...
		if (nsp == null) {
			String msg = "Argument 'nsp' cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		if (e == null) {
			String msg = "Argument 'e [Element]' cannot be null.";
			throw new IllegalArgumentException(msg);
		}

		if (e.getNamespace().equals(Namespace.NO_NAMESPACE)) {
			e.setQName(new QName(e.getName(), nsp));
			for (Object n : e.elements()) {
				applyNamespace(nsp, (Element) n);
			}
		}

	}

	/*
	 * Implementation.
	 */

	private NodeProcessor() {}

}