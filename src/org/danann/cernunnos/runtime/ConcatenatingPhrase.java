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

package org.danann.cernunnos.runtime;

import java.util.LinkedList;
import java.util.List;

import org.dom4j.Node;

import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public final class ConcatenatingPhrase implements Phrase {
	
	// Instance Members.
	private final List<Phrase> children;

	/*
	 * Public API.
	 */

	public ConcatenatingPhrase(List<Phrase> children) {

		// Assertions...
		if (children == null) {
			String msg = "Argument 'children' cannot be null.";
			throw new IllegalArgumentException(msg);
		}

		// Instance Members.
		this.children = new LinkedList<Phrase>(children);
		
	}
	
	public Formula getFormula() {
		throw new UnsupportedOperationException();
	}

	public void init(EntityConfig config) {
		throw new UnsupportedOperationException();
	}

	public Object evaluate(TaskRequest req, TaskResponse res) {

		// Assertions...
		if (req == null) {
			String msg = "Argument 'req' cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		if (res == null) {
			String msg = "Argument 'res' cannot be null.";
			throw new IllegalArgumentException(msg);
		}

		Object rslt = null;
		
		// NB:  If there's more than one component, the return type *must* be String.
		switch (children.size()) {
			case 1:
				rslt = children.get(0).evaluate(req, res);
				break;
			default:
				StringBuffer buffer = new StringBuffer();
				for (Phrase p : children) {
					Object value = p.evaluate(req, res);
					if (value instanceof Node) {
						// Concatenate the text value...
						String text = ((Node) value).getText();
						buffer.append(text);
					} else if (value == null) {
						// If null just write null...
						buffer.append("null");
					} else {
						// Try toString()...
						buffer.append(value.toString());
					}
				}
				rslt = buffer.toString();
				break;
		}		
		
		return rslt;
		
	}

}