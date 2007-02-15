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

package org.danann.cernunnos;

import java.util.List;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Node;

/**
 * Provides a standard way to serialize common data types to and from XML.  The 
 * following types are currently supported:
 * <ul>
 *   <li>String</li>
 * </ul>
 * Arrays of any supported type may also be serialized.
 */
public final class CommonTypesSerializer {

	// Static Members.
	private static final DocumentFactory fac = new DocumentFactory();

	/*
	 * Public API.
	 */

	/**
	 * Serializes the specified <code>Object</code> into XML.
	 * 
	 * @param o The target of serialization.
	 * @return A <code>Node</code> representation of the specified object.
	 */
	public static Node toNode(Object o) {
		
		// Assertions...
		if (o == null) {
			String msg = "Argument 'o [Object]' cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		
		for (TypeHandlers h : TypeHandlers.values()) {
			if (h.appliesTo(o)) {
				return h.toNode(o);
			}
		}

		// If we're still here, there's no handler for the class...
		String msg = "Unable to serialize the specified Node.  No handler is defined for " 
															+ o.getClass().getName();
		throw new RuntimeException(msg);
		
	}
	
	/**
	 * 'Re-hydrates' or deserializes the specified <code>Node</code> into an 
	 * <code>Object</code>.
	 * 
	 * @param n An XML node.
	 * @return An <code>Object</code> created from the specified node.
	 */
	public static Object fromNode(Node n) {
		
		// Assertions...
		if (n == null) {
			String msg = "Argument 'n [Node]' cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		
		for (TypeHandlers h : TypeHandlers.values()) {
			if (h.appliesTo(n)) {
				return h.fromNode(n);
			}
		}

		// If we're still here, there's no handler for the class...
		String msg = "Unable to deserialize the specified Object.  No handler is defined for " 
																		+ n.getName();
		throw new RuntimeException(msg);
		
	}
	
	/*
	 * Implementation.
	 */

	private enum TypeHandlers {
				
		ARRAY {
			boolean appliesTo(Object o) { return o.getClass().isArray(); }
			boolean appliesTo(Node n) { return n.getName().equals("array"); }
			Node toNode(Object o) { 
				Element rslt = fac.createElement("array");
				Object[] elements = (Object[]) o;
				for (Object j : elements) {
					rslt.add(CommonTypesSerializer.toNode(j));
				}
				return rslt; }
			Object fromNode(Node n) {
				List list = n.selectNodes("*");
				Object[] rslt = new Object[list.size()];
				for (int i=0; i < list.size(); i++) {
					Node d = (Node) list.get(i);
					rslt[i] = CommonTypesSerializer.fromNode(d);
				}
				return rslt;
			}
		},

		STRING {
			boolean appliesTo(Object o) { return o instanceof String; }
			boolean appliesTo(Node n) { return n.getName().equals(String.class.getName()); }
			Node toNode(Object o) { return fac.createElement(String.class.getName()).addText(o.toString()); }
			Object fromNode(Node n) { return n.getText(); }
		};
		
		abstract boolean appliesTo(Object o);
		abstract boolean appliesTo(Node n);
		abstract Node toNode(Object o);
		abstract Object fromNode(Node n);
		
	}
	
}
