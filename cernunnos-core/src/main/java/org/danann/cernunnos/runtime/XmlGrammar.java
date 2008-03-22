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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import org.danann.cernunnos.AbstractContainerTask;
import org.danann.cernunnos.Attributes;
import org.danann.cernunnos.Bootstrappable;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Grammar;
import org.danann.cernunnos.LiteralPhrase;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.Task;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

/**
 * Represents a "task language" or syntax in Cernunnos.  <code>XmlGrammar</code>
 * instances are responsible for bootsrapping <code>Task</code> objects from
 * XML.
 */
public final class XmlGrammar implements Grammar {

	// Static Members.
	private static final DocumentFactory fac = new DocumentFactory();
	private static final String MAIN_GRAMMAR_LOCATION = "main.grammar";
	private static Grammar mainGrammar = null;

	// Instance Members.
	private final Grammar parent;
	private final ClassLoader loader;
	private Map<String,Entry> taskEntries;
	private Map<String,Entry> phraseEntries;

	/*
	 * Public API.
	 */

	public static synchronized Grammar getMainGrammar() {

		try {
			InputStream inpt = XmlGrammar.class.getClassLoader().getResourceAsStream(MAIN_GRAMMAR_LOCATION);
			Document doc = new SAXReader().read(inpt);
			mainGrammar = XmlGrammar.parse(doc.getRootElement());
		} catch (Throwable t) {
			String msg = "Error parsing Main Grammar.";
			throw new RuntimeException(msg, t);
		}

		return mainGrammar;

	}

	public static Grammar parse(Element e) {
		return parse(e, null, XmlGrammar.class.getClassLoader());
	}

	public static Grammar parse(Element e, Grammar parent) {
		return parse(e, parent, XmlGrammar.class.getClassLoader());
	}

	public static Grammar parse(Element e, ClassLoader loader) {
		return parse(e, null, loader);
	}

	public static Grammar parse(Element e, Grammar parent, ClassLoader loader) {

		// Assertions...
		if (e == null) {
			String msg = "Argument 'e [Element]' cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		if (!e.getName().equals("grammar")) {
			String msg = "Argument 'e [Element]' must be a <grammar> element.";
			throw new IllegalArgumentException(msg);
		}
		// NB:  parent may be null...
		if (loader == null) {
			String msg = "Argument 'loader' cannot be null.";
			throw new IllegalArgumentException(msg);
		}

		XmlGrammar rslt = new XmlGrammar(parent, loader);	// Chicken-egg problem...

		// Create the entries collections...
		Map<String,Entry> phraseEntries = new HashMap<String,Entry>();
		phraseEntries.putAll(parseEntries(e.selectNodes("phrase"), rslt));
		rslt.setPhraseEntries(phraseEntries);

		Map<String,Entry> taskEntries = new HashMap<String,Entry>();
		taskEntries.putAll(parseEntries(e.selectNodes("task"), rslt));
		rslt.setTaskEntries(taskEntries);

		return rslt;

	}

	public Task newTask(Element e, Task parent) {

		// Assertions...
		if (e == null) {
			String msg = "Argument 'e [Element]' cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		// NB:  parent may be null...

		// Elements that define tasks *must* be normalized...
		e.normalize();

		String name = e.getName();
		Entry n = getEntry(name, EntryType.TASK);

		Task rslt = null;
		try {

			// Create & bootstrap the result...
			rslt = (Task) n.getFormula().getImplementationClass().newInstance();
			rslt.init(prepareEntryConfig(n, e));

		} catch (Throwable t) {
			String msg = "Unable to create the specified task:  " + name;
			throw new RuntimeException(msg, t);
		}

		return new RuntimeTaskDecorator(rslt);

	}

	public Phrase newPhrase(String inpt) {

		// Assertions...
		if (inpt == null) {
			String msg = "Argument 'inpt' cannot be null.";
			throw new IllegalArgumentException(msg);
		}

		List<String> chunks = new LinkedList<String>();
		String chunkMe = inpt;
		while (chunkMe.length() != 0) {
			if (chunkMe.startsWith(Phrase.OPEN_PHRASE_DELIMITER)) {
				chunks.add(Phrase.OPEN_PHRASE_DELIMITER);
				chunkMe = chunkMe.substring(2);
			} else {
				chunks.add(chunkMe.substring(0, 1));
				chunkMe = chunkMe.substring(1);
			}
		}

		List<Phrase> children = new LinkedList<Phrase>();
		StringBuffer buffer = new StringBuffer();
		int openCount = 0;
		for (String chunk : chunks) {
			switch (openCount) {
				case 0:
					if (chunk.equals(Phrase.OPEN_PHRASE_DELIMITER)) {
						if (buffer.length() > 0) {
							children.add(new LiteralPhrase(buffer.toString()));
							buffer.setLength(0);
						}
						++openCount;
					} else {
						buffer.append(chunk);
					}
					break;
				default:
					if (chunk.equals(Phrase.OPEN_PHRASE_DELIMITER)) {
						++openCount;
						buffer.append(chunk);
					} else if (chunk.equals(Phrase.CLOSE_PHRASE_DELIMITER)) {
						--openCount;
						if (openCount == 0) {

							// Time to create a dynamic component...
							String expression = buffer.toString();
							String name = null;		// Name of the phrase to use...
							String nested = null;	// Content passed to the phrase

							// Determine if a Phrase impl was specified or if we should use the default...
							int openParenIndex = expression.indexOf("(");
							if (openParenIndex != -1 && expression.endsWith(")")) {
								// A phrase impl was specified -- use it!
								try {
									name = expression.substring(0, openParenIndex);
									nested = expression.substring(expression.indexOf("(") + 1, expression.length() - 1);
								} catch (Throwable t) {
									String msg = "The specified expression is not well formed:  " + expression;
									throw new RuntimeException(msg, t);
								}
							} else {
								// Use the default phrase impl...
								name = Grammar.DEFAULT_PHRASE_IMPL.getName();
								nested = expression;
							}

							Entry n = getEntry(name, EntryType.PHRASE);
							Phrase p = null;
							try {

								// Create & bootstrap the phrase...
								p = (Phrase) n.getFormula().getImplementationClass().newInstance();
								p.init(prepareEntryConfig(n, fac.createText(nested)));

							} catch (Throwable t) {
								String msg = "Unable to create the specified phrase:  " + name;
								throw new RuntimeException(msg, t);
							}

							children.add(p);
							buffer.setLength(0);
						} else {
							buffer.append(chunk);
						}
					} else {
						buffer.append(chunk);
					}
					break;
			}
		}
		if (buffer.length() > 0) {
			// Add anything that's left...
			children.add(new LiteralPhrase(buffer.toString()));
		}

		return new ConcatenatingPhrase(children);

	}

	/*
	 * Implementation.
	 */

	private static Map<String,Entry> parseEntries(List nodes, XmlGrammar g) {

		// Assertions...
		if (nodes == null) {
			String msg = "Argument 'nodes' cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		if (g == null) {
			String msg = "Argument 'g [Grammar]' cannot be null.";
			throw new IllegalArgumentException(msg);
		}

		Map<String,Entry> rslt = new HashMap<String,Entry>();
		for (Iterator it = nodes.iterator(); it.hasNext();) {

			Element e = (Element) it.next();

			// Type Name.
			String name = e.valueOf("@name");
			if (name.trim().length() == 0) {
				String msg = "The following element is missing required "
									+ "attribute '@name':  " + e.asXML();
				throw new IllegalArgumentException(msg);
			}

			// Formula.
			String impl = e.valueOf("@impl");
			if (impl.trim().length() == 0) {
				String msg = "The following element is missing required "
									+ "attribute '@impl':  " + e.asXML();
				throw new IllegalArgumentException(msg);
			}
			try {

				// Get the description, if present...
				String description = null;	// default...
				Element doc = (Element) e.selectSingleNode("following-sibling::doc[@entry = '" + name + "']");
				if (doc != null) {
					description = doc.selectSingleNode("description").getText();
				}

				// Obtain the Formula...
				Class c = g.loader.loadClass(impl);
				Bootstrappable b = (Bootstrappable) c.newInstance();
				Formula f = b.getFormula();

				// Sanity check -- refuse the formula if the class doesn't match!
				if (!f.getImplementationClass().equals(c)) {
					String msg = "Invalid Formula Provided by Task Implementation:  class '"
								+ c.getName() + "' provided a formula specifying implementation class '"
								+ f.getImplementationClass().getName() + "'.";
					throw new RuntimeException(msg);
				}

				// Evaluate the mappings...
				Map<Reagent,Object> mappings = new HashMap<Reagent,Object>();
				for (Reagent r : f.getReagents()) {
					Object value = r.getReagentType().evaluate(g, e, r.getXpath());
					if (value != null) {
						mappings.put(r, value);
					}
				}

				// Pull the examples as well...
				List<Node> examples = new LinkedList<Node>();
				for (Iterator xItr = e.selectNodes("//doc[@entry = '" + name + "']/example").iterator(); xItr.hasNext();) {
					examples.add((Node) xItr.next());
				}

				// Create the associated Entry...
				rslt.put(name, new Entry(name, EntryType.valueOf(e.getName().toUpperCase()), description, f, mappings, examples));

			} catch (Throwable t) {
				String msg = "Unable to parse the specified entry.";
				throw new RuntimeException(msg, t);
			}

		}

		return rslt;

	}

	private XmlGrammar(Grammar parent, ClassLoader loader) {

		// Assertions...
		// NB:  Parent may be null.
		if (loader == null) {
			String msg = "Argument 'loader' cannot be null.";
			throw new IllegalArgumentException(msg);
		}

		// Instance Members.
		this.parent = parent;
		this.loader = loader;

		// NB:  Tasks & phrases are set after creation
		// (viz. in parse() method) due to chicken-egg scenario...

	}

	private Entry getEntry(String name, EntryType type) {
		Entry rslt = null;

		// Choose which entry Map...
		Map<String,Entry> m = null;
		if (type.equals(EntryType.TASK)) {
			m = taskEntries;
		} else if (type.equals(EntryType.PHRASE)) {
			m = phraseEntries;
		}
		
		// Might be a null Map if we're currently bootstrapping a Grammar...
		if (m != null && m.containsKey(name)) {
			// If there's a matching entry w/in this Grammar it trumps all...
			return m.get(name);
		} else if (parent != null && parent instanceof XmlGrammar) {
			// This is a little hokey... perhaps Entry should be a first-class type?
			rslt = ((XmlGrammar) parent).getEntry(name, type);
		} else {
			// See if the name is a class that implements...
			try {
				Class c = Class.forName(name);
				Bootstrappable b = (Bootstrappable) c.newInstance();
				EntryType y = null;
				if (type.equals(EntryType.PHRASE) && b instanceof Phrase) {
					y = EntryType.PHRASE;
				} else if (type.equals(EntryType.TASK) && b instanceof Task) {
					y = EntryType.TASK;
				} else {
					String msg = "The specified class is either not a Phrase or " +
							"a Task, or it doesn't match the specified type:  " + name;
					throw new RuntimeException(msg);
				}
				rslt = new Entry(name, y, null, b.getFormula(), new HashMap<Reagent,Object>(), new LinkedList<Node>());
			} catch (ClassNotFoundException cnfe) {
				String msg = "The specified entry name does not match a known entry or an available class:  " + name;
				throw new IllegalArgumentException(msg);
			} catch (Throwable t) {
				String msg = "Error preparing the specified entry:  " + name;
				throw new RuntimeException(msg, t);
			}
		}
		return rslt;
	}

	private void setTaskEntries(Map<String,Entry> m) {

		// Assertions...
		if (m == null) {
			String msg = "Argument 'm [Map<String,Entry>]' cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		if (this.taskEntries != null) {
			String msg = "The task entries collection has already been assigned.";
			throw new IllegalStateException(msg);
		}

		this.taskEntries = (Map<String,Entry>) Collections.unmodifiableMap(m);

	}

	private void setPhraseEntries(Map<String,Entry> m) {

		// Assertions...
		if (m == null) {
			String msg = "Argument 'm [Map<String,Entry>]' cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		if (this.phraseEntries != null) {
			String msg = "The phrase entries collection has already been assigned.";
			throw new IllegalStateException(msg);
		}

		this.phraseEntries = (Map<String,Entry>) Collections.unmodifiableMap(m);

	}

	private EntityConfig prepareEntryConfig(Entry n, Node d) {

		// Assertions...
		if (n == null) {
			String msg = "Argument 'n [Entry]' cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		if (d == null) {
			String msg = "Argument 'd [Element]' cannot be null.";
			throw new IllegalArgumentException(msg);
		}

		Formula f = n.getFormula();

		Map<Reagent,Object> mappings = new HashMap<Reagent,Object>();
		List<Reagent> needed = new ArrayList<Reagent>(f.getReagents());
		needed.removeAll(n.getMappings().keySet());
		for (Reagent r : needed) {
			Object value = r.getReagentType().evaluate(this, d, r.getXpath());
			if (value == null) {
				// First see if there's a default...
				if (r.hasDefault()) {
					value = r.getDefault();
				} else {
					String msg = "The required expression '" + r.getXpath()
						+ "' is missing from the following node:  " + d.asXML();
					throw new RuntimeException(msg);
				}
			}
			mappings.put(r, value);
		}
		mappings.putAll(n.getMappings());

		return new SimpleEntityConfig(this, n.getFormula(), mappings);

	}

	/*
	 * Nested Types.
	 */

	private enum EntryType {
		TASK,
		PHRASE
	}

	private static final class Entry {

		// Instance Members.
		private final String name;
		private final EntryType type;
		private final String description;
		private final Formula formula;
		private final Map<Reagent,Object> mappings;
		private final List<Node> examples;

		/*
		 * Public API.
		 */

		public Entry(String name, EntryType type, String description, Formula f, Map<Reagent,Object> mappings, List<Node> examples) {

			// Assertions...
			if (name == null) {
				String msg = "Argument 'name' cannot be null.";
				throw new IllegalArgumentException(msg);
			}
			if (type == null) {
				String msg = "Argument 'type' cannot be null.";
				throw new IllegalArgumentException(msg);
			}
			// NB:  description may be null...
			if (f == null) {
				String msg = "Argument 'f [Formula]' cannot be null.";
				throw new IllegalArgumentException(msg);
			}
			if (mappings == null) {
				String msg = "Argument 'mappings' cannot be null.";
				throw new IllegalArgumentException(msg);
			}
			if (examples == null) {
				String msg = "Argument 'examples' cannot be null.";
				throw new IllegalArgumentException(msg);
			}

			// Instance Members.
			this.name = name;
			this.type = type;
			this.description = description;
			this.formula = f;
			this.mappings = (Map<Reagent,Object>) Collections.unmodifiableMap(mappings);
			this.examples = (List<Node>) Collections.unmodifiableList(examples);

		}

		public String getName() {
			return name;
		}

		public EntryType getType() {
			return type;
		}

		public String getDescription() {
			return description;
		}

		public Formula getFormula() {
			return formula;
		}

		public Map<Reagent,Object> getMappings() {
			return mappings;
		}

		public List<Node> getExamples() {
			return examples;
		}

	}

	public static final class SerializeGrammarTask extends AbstractContainerTask {

		// Injstance Members.
		private XmlGrammar grammar;

		/*
		 * Public API.
		 */

		public void init(EntityConfig config) {

			super.init(config);

			// Instance Members.
			this.grammar = (XmlGrammar) config.getGrammar();

		}

		public Formula getFormula() {
			Reagent[] reagents = new Reagent[] {AbstractContainerTask.SUBTASKS};
			final Formula rslt = new SimpleFormula(SerializeGrammarTask.class, reagents);
			return rslt;
		}

		public void perform(TaskRequest req, TaskResponse res) {

			Element rslt = fac.createElement("grammar");

			for (Entry e : grammar.taskEntries.values()) {
				rslt.add(serializeEntry(e, fac));
			}
			for (Entry e : grammar.phraseEntries.values()) {
				rslt.add(serializeEntry(e, fac));
			}

			res.setAttribute(Attributes.NODE, rslt);

			super.performSubtasks(req, res);

		}

		/*
		 * Implementation.
		 */

		private static Node serializeEntry(Entry e, DocumentFactory fac) {

			// Assertions...
			if (e == null) {
				String msg = "Argument 'e [Entry]' cannot be null.";
				throw new IllegalArgumentException(msg);
			}
			if (fac == null) {
				String msg = "Argument 'fac' cannot be null.";
				throw new IllegalArgumentException(msg);
			}

			Element rslt = fac.createElement("entry");
			rslt.addAttribute("type", e.getType().name());

			// Name.
			Element name = fac.createElement("name");
			name.setText(e.getName());
			rslt.add(name);

			// Description.
			Element desc = fac.createElement("description");
			if (e.getDescription() != null) {
				desc.setText(e.getDescription());
			}
			rslt.add(desc);

			// Formula.
			rslt.add(serializeFormula(e.getFormula(), e.getMappings(), fac));

			// Examples.
			for (Node x : e.getExamples()) {
				rslt.add((Node) x.clone());
			}

			return rslt;

		}

		private static Node serializeFormula(Formula f, Map<Reagent,Object> mappings, DocumentFactory fac) {

			// Assertions...
			if (f == null) {
				String msg = "Argument 'f [Formula]' cannot be null.";
				throw new IllegalArgumentException(msg);
			}
			if (fac == null) {
				String msg = "Argument 'fac' cannot be null.";
				throw new IllegalArgumentException(msg);
			}

			Element rslt = fac.createElement("formula");

			// Impl.
			rslt.addAttribute("impl", f.getImplementationClass().getName());

			// Reagents.
			Element reagents = fac.createElement("reagents");
			for (Reagent r : f.getReagents()) {
				if (mappings.containsKey(r)) {
					// We don't list it, since there's a final value...
					continue;
				}
				Element reagent = fac.createElement("reagent");
				reagent.addAttribute("name", r.getName());
				reagent.addAttribute("xpath", r.getXpath());
				reagent.addAttribute("reagent-type", r.getReagentType().name());
				reagent.addAttribute("expected-type", r.getExpectedType().getName());
				boolean req = !r.hasDefault();
				reagent.addAttribute("required", req ? "Yes" : "No");
				Element desc = fac.createElement("description");
				if (r.getDescription() != null) {
					desc.setText(r.getDescription());
				}
				reagent.add(desc);
				reagents.add(reagent);
			}
			rslt.add(reagents);

			return rslt;

		}

	}

}