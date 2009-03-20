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

import org.dom4j.Element;
import org.dom4j.Node;

/**
 * Represents a lexicon or system of tasks.
 */
public interface Grammar {

	/**
	 * All <code>Grammar</code> implementations must use the
	 * <code>AttributePhrase</code> in all cases where a phrase implementation
	 * is not indicated either by name or by class name.
	 */
	static Class<?> DEFAULT_PHRASE_IMPL = AttributePhrase.class;
	
	/**
	 * Provides a short name for this <code>Grammar</code> that describes it and 
	 * its entries.  One-word names are preferred.
	 * 
	 * @return A name for this grammar
	 */
	String getName();

	/**
	 * Creates a new <code>Task</code> based on the information found in the
	 * specified <code>Element</code>.  The name of the element must be
	 * associated with class that implements <code>Task</code>.  The content of
	 * the element (attributes and child elements) may be anything that is
	 * understood by the task implementation.
	 *
	 * @param e An <code>Element</code> that defines a task.
	 * @param parent The task will contain the new task.
	 * @return A new, ready-to-use <code>Task</code> instance.
	 */
	Task newTask(Element e, Task parent);

	/**
	 * Creates a new <code>Phrase</code> based on the specified
	 * <code>String</code>.  The <code>Grammar</code> implementation is
	 * responsible for converting this <code>String</code> to a text node to
	 * work properly within the Cernunnos bootstrapping system.  This text node
	 * will match the XPath expression <code>descendant-or-self::text()</code>.
	 *
	 * @param inpt Text that will match the expression
	 * <code>descendant-or-self::text()</code> in boostrapping.
	 * @return A new, ready-to-use <code>Phrase</code> instance.
	 * @deprecated Use newPhrase(Node) for better error information
	 */
	@Deprecated
	Phrase newPhrase(String inpt);

	/**
	 * Creates a new <code>Phrase</code> based on the specified
	 * <code>String</code>.  The <code>Grammar</code> implementation is
	 * responsible for converting this <code>String</code> to a text node to
	 * work properly within the Cernunnos bootstrapping system.  This text node
	 * will match the XPath expression <code>descendant-or-self::text()</code>.
	 *
	 * @param inpt Text that will match the expression
	 * <code>descendant-or-self::text()</code> in boostrapping.
	 * @return A new, ready-to-use <code>Phrase</code> instance.
	 */
	Phrase newPhrase(Node n);

}
