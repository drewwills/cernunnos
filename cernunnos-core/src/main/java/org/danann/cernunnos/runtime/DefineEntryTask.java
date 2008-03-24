/**
 * 
 */
package org.danann.cernunnos.runtime;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;
import org.dom4j.Node;

import org.danann.cernunnos.Bootstrappable;
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

public final class DefineEntryTask implements Task {
	
	// Instance Members.
	private Phrase entry_name;
	private Phrase impl;
	private Element content;
	private Element description;
	private List<Node> examples;
	
	/*
	 * Public API.
	 */

	public static final Reagent NAME = new SimpleReagent("NAME", "@name", ReagentType.PHRASE, String.class,
				"Name of this grammar entry.  Cernunnos XML typically references tasks and phrases by name.");

	public static final Reagent IMPL = new SimpleReagent("IMPL", "@impl", ReagentType.PHRASE, String.class,
				"Fully-qualified Java class name for this Task or Phrase.");

	public static final Reagent CONTENT_MODEL = new SimpleReagent("CONTENT_MODEL", "content-model", 
				ReagentType.NODE_LIST, List.class, "Grammar entries may optionally be defined with " +
				"some or all of the reagents supported by the underlying class pre-defined.  Use a " +
				"single child 'content-model' element for this purpose:  reagents of the implementing " +
				"class will have their XPath expressions evaluated against this element.  Note that " +
				"phrases, which normally can accept only one input ('descendant-or-self::text()') can " +
				"support multiple reagents using this mechanism.", Collections.emptyList());

	public static final Reagent DESCRIPTION = new SimpleReagent("DESCRIPTION", "description", ReagentType.NODE_LIST, 
				List.class, "XHTML description of this entry.", Collections.emptyList());

	public static final Reagent EXAMPLES = new SimpleReagent("EXAMPLES", "example", ReagentType.NODE_LIST, 
				List.class, "Examples of this entry in use.  Each example may optionally include a " +
				"'caption' attribute that describes what the example does.", Collections.emptyList());

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {NAME, IMPL, CONTENT_MODEL, DESCRIPTION, EXAMPLES};
		final Formula rslt = new SimpleFormula(this.getClass(), reagents);
		return rslt;
	}

	public void init(EntityConfig config) {
		
		List<?> list = null;
		
		// Instance Members.
		this.entry_name = (Phrase) config.getValue(NAME);
		this.impl = (Phrase) config.getValue(IMPL);
		list = (List<?>) config.getValue(CONTENT_MODEL);
		this.content = list.size() > 0 ? (Element) list.get(0) : null;
		list = (List<?>) config.getValue(DESCRIPTION);
		this.description = list.size() > 0 ? (Element) list.get(0) : null;
		this.examples = new LinkedList<Node>();
		list = (List<?>) config.getValue(EXAMPLES);
		for (Object o : list) {
			examples.add((Node) o);
		}			
		
	}

	public void perform(TaskRequest req, TaskResponse res) {

		XmlGrammar grammar = (XmlGrammar) req.getAttribute(EncloseGrammarTask.SECRET_GRAMMAR_KEY);
		
		String n = (String) entry_name.evaluate(req, res);
		String m = (String) impl.evaluate(req, res);
		
		try {

			// Obtain the Formula...
			Class<?> c = grammar.getClassLoader().loadClass(m);
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
			if (content != null) {
				for (Reagent r : f.getReagents()) {
					Object value = r.getReagentType().evaluate(grammar, content, r.getXpath());
					if (value != null) {
						mappings.put(r, value);
					}
				}
			}
			
			// Evaluate the type...
			Entry.Type y = null;
			if (b instanceof Phrase) {
				// NB:  For the moment it seems perilous to allow a 
				// single class to define both a Phrase and a Task;  
				// if it implements Phrase, it's a Phrase. 
				y = Entry.Type.PHRASE;
			} else if (b instanceof Task) {
				y = Entry.Type.TASK;
			} else {
				String msg = "The specified IMPL class does not implement a " +
												"known entry type:  " + m;
				throw new RuntimeException(msg);
			}

			grammar.addEntry(new Entry(n, y, description, 
										f, mappings, examples));

		} catch (Throwable t) {
			String msg = "Unable to parse the specified entry:" +
							"\n\t\tname=" + n +
							"\n\t\timpl=" + m;
			throw new RuntimeException(msg, t);
		}
		
	}

}