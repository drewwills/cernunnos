/*
 * Copyright 2008 Andrew Wills
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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Node;

import org.danann.cernunnos.Bootstrappable;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.Task;


public final class Entry {

    // Static Members.
    private static final DocumentFactory fac = new DocumentFactory();

    // Instance Members.
    private final String name;
    private Type type;
    private final Element description;
    private Formula formula;
    private Map<Reagent,Object> mappings;
    private final List<Node> examples;

    // Instance Members for lazyLoad()...
    private final String clazz;
    private final Element contentModel;
    private final XmlGrammar grammar;

    /*
     * Public API.
     */

    public enum Type {
        TASK,
        PHRASE
    }
    
    public Entry(String name, Element description, String clazz, Element contentModel, XmlGrammar g, List<Node> examples) {
    	
        // Assertions...
        if (name == null) {
            String msg = "Argument 'name' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        // NB:  description may be null...
        if (clazz == null) {
            String msg = "Argument 'clazz' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        // NB:  contentModel may be null...
        if (g == null) {
            String msg = "Argument 'g [Grammar]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (examples == null) {
            String msg = "Argument 'examples' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.name = name;
        this.type = null;
        this.description = description;
        this.formula = null;
        this.mappings = null;
        this.examples = (List<Node>) Collections.unmodifiableList(examples);

        // Instance Members for lazyLoad()...
        this.clazz = clazz;
        this.contentModel = contentModel;
        this.grammar = g;

    }

    @Deprecated
    public Entry(String name, Type type, String description, Formula f, Map<Reagent,Object> mappings, List<Node> examples) {
        this(name, type, fac.createElement("description"), f, mappings, examples);
        Element p = fac.createElement("p");
        p.setText(description);
        this.description.add(p);
    }

    @Deprecated
    public Entry(String name, Type type, Element description, Formula f, Map<Reagent,Object> mappings, List<Node> examples) {

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

        // Instance Members for lazyLoad()...
        this.clazz = null;
        this.contentModel = null;
        this.grammar = null;
        
    }

    public String getName() {
        return name;
    }

    public Type getType() {
    	if (type == null) {
    		lazyLoad();
    	}
        return type;
    }

    public Element getDescription() {
        return description;
    }

    public Formula getFormula() {
    	if (formula == null) {
    		lazyLoad();
    	}
        return formula;
    }

    public Map<Reagent,Object> getMappings() {
    	if (mappings == null) {
    		lazyLoad();
    	}
        return mappings;
    }

    public List<Node> getExamples() {
        return examples;
    }
    
    public boolean equals(Object o) {
    	
    	boolean rslt = false;	// default...
    	
    	if (o != null && o instanceof Entry) {
    		Entry other = (Entry) o;
    		if (other.getName().equals(this.getName()) 
    					&& other.getType().equals(this.getType())) {
    			rslt = true;
    		}
    	}
    	
    	return rslt;
    	
    }
    
    public int hashCode() {
    	return this.getName().hashCode();
    }
    
    /*
     * Implementation.
     */

    private synchronized void lazyLoad() {
    	
    	if (formula != null) {
    		// We've already done this...
    		return;
    	}
    	
		try {

			// Obtain the Formula...
			Class<?> c = grammar.getClassLoader().loadClass(clazz);
			Bootstrappable b = (Bootstrappable) c.newInstance();
			Formula frm = b.getFormula();

			// Sanity check -- refuse the formula if the class doesn't match!
			if (!frm.getImplementationClass().equals(c)) {
				String msg = "Invalid Formula Provided by Task or Phrase Implementation:  class '"
							+ c.getName() + "' provided a formula specifying implementation class '"
							+ frm.getImplementationClass().getName() + "'.";
				throw new RuntimeException(msg);
			}

			// Evaluate the mappings...
			Map<Reagent,Object> mpg = new HashMap<Reagent,Object>();
			if (contentModel != null) {
				for (Reagent r : frm.getReagents()) {
					Object value = r.getReagentType().evaluate(grammar, contentModel, r.getXpath());
					if (value != null) {
						mpg.put(r, value);
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
												"known entry type:  " + clazz;
				throw new RuntimeException(msg);
			}

			// Set the member variables that were lazyLoaded...
			this.type = y;
			this.formula = frm;
			this.mappings = mpg;

		} catch (Throwable t) {
			String msg = "Unable to lazyLoad() the specified entry:" +
							"\n\t\tname=" + this.name +
							"\n\t\timpl=" + this.clazz;
			throw new RuntimeException(msg, t);
		}

    }
    
}
