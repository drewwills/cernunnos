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

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import org.danann.cernunnos.AbstractContainerTask;
import org.danann.cernunnos.Bootstrappable;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Grammar;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

@Deprecated
public final class AddGrammarTask extends AbstractContainerTask {

	// Instance Members.
	private String context;
	private String location;

	/*
	 * Public API.
	 */

	public static final Reagent CONTEXT = new SimpleReagent("CONTEXT", "@context", ReagentType.STRING, String.class,
				"The context from which missing elements of the LOCATION can be inferred if it is relative.  "
				+ "The default is the filesystem location from which Java is executing.  WARNING:  This reagent "
				+ "must be a String (not a Phrase) because it gets used at boostrap time.  A URL will be "
				+ "constructed from the value via new URL(String spec).", createDefaultUrl());

	public static final Reagent LOCATION = new SimpleReagent("LOCATION", "@location", ReagentType.STRING, String.class,
					"The location of the grammar file that defines the grammar to add.  WARNING:  This reagent "
					+ "must be a String (not a Phrase) because it gets used at boostrap time (not runtime).");

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {CONTEXT, LOCATION, AbstractContainerTask.SUBTASKS};
		final Formula rslt = new SimpleFormula(AddGrammarTask.class, reagents);
		return rslt;
	}

	public void init(EntityConfig config) {

		// Instance Members.
		this.context = (String) config.getValue(CONTEXT);
		this.location = (String) config.getValue(LOCATION);

		Grammar g = null;
		try {
			URL ctx = new URL(this.context);
			URL loc = new URL(ctx, location);

			// Read by passing a URL -- don't manage the URLConnection yourself...
			g = AddGrammarTask.parse(new SAXReader().read(loc).getRootElement(), 
													config.getGrammar());
		} catch (Throwable t) {
			String msg = "Unable to parse a grammar from the specified location:  " + this.location;
			throw new RuntimeException(msg, t);
		}

		super.init(new SimpleEntityConfig(g, config.getEntryName(), 
						config.getSource(), config.getFormula(), 
						config.getValues()));

	}

	public void perform(TaskRequest req, TaskResponse res) {
		super.performSubtasks(req, res);
	}

	/*
	 * Implementation.
	 */

	private static String createDefaultUrl() {

		String rslt = null;

		try {
			rslt = new File(".").toURL().toString();
		} catch (Throwable t) {
			String msg = "Unable to create a URL representation of the current directory.";
			throw new RuntimeException(msg, t);
		}

		return rslt;

	}

	private static Grammar parse(Element e, Grammar parent) {
		
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
	
	    XmlGrammar rslt = new XmlGrammar(parent);   // Chicken-egg problem...
	
	    // Create the entries collections...
	    Map<String,Entry> phraseEntries = new HashMap<String,Entry>();
	    phraseEntries.putAll(AddGrammarTask.parseEntries(e.selectNodes("phrase"), rslt));
	    for (Entry y : phraseEntries.values()) {
	    	rslt.addEntry(y);
	    }
	
	    Map<String,Entry> taskEntries = new HashMap<String,Entry>();
	    taskEntries.putAll(AddGrammarTask.parseEntries(e.selectNodes("task"), rslt));
	    for (Entry y : taskEntries.values()) {
	    	rslt.addEntry(y);
	    }
	
	    return rslt;
	
	}

	static Map<String,Entry> parseEntries(List<?> nodes, XmlGrammar g) {
	
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
	    for (Iterator<?> it = nodes.iterator(); it.hasNext();) {
	
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
	            String description = null;  // default...
	            Element doc = (Element) e.selectSingleNode("following-sibling::doc[@entry = '" + name + "']");
	            if (doc != null) {
	                description = doc.selectSingleNode("description").getText();
	            }
	
	            // Obtain the Formula...
	            Class<?> c = g.getClassLoader().loadClass(impl);
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
	            for (Iterator<?> xItr = e.selectNodes("//doc[@entry = '" + name + "']/example").iterator(); xItr.hasNext();) {
	                examples.add((Node) xItr.next());
	            }
	
	            // Create the associated Entry...
	            rslt.put(name, new Entry(name, Entry.Type.valueOf(e.getName().toUpperCase()), description, f, mappings, examples));
	
	        } catch (Throwable t) {
	            String msg = "Unable to parse the specified entry.";
	            throw new RuntimeException(msg, t);
	        }
	
	    }
	
	    return rslt;
	
	}

}