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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import org.danann.cernunnos.Attributes;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Grammar;
import org.danann.cernunnos.LiteralPhrase;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReturnValue;
import org.danann.cernunnos.Task;
import org.danann.cernunnos.EntityConfig;

/**
 * Represents a "task language" or syntax in Cernunnos.  <code>XmlGrammar</code>
 * instances are responsible for bootstrapping <code>Task</code> objects from
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
    private Map<String,List<Entry>> entries;
	private final Log log = LogFactory.getLog(XmlGrammar.class);	// Don't declare as static in general libraries

    /*
     * Public API.
     */

    public static synchronized Grammar getMainGrammar() {

    	if (mainGrammar == null) {
    		// Create it...
    		try {
            	final Grammar root = new XmlGrammar(null, XmlGrammar.class.getClassLoader());
            	final InputStream inpt = XmlGrammar.class.getResourceAsStream(MAIN_GRAMMAR_LOCATION);
                final Document doc = new SAXReader().read(inpt);
                final Task k = new ScriptRunner(root).compileTask(doc.getRootElement());
        		final RuntimeRequestResponse req = new RuntimeRequestResponse();
        		final ReturnValueImpl rslt = new ReturnValueImpl();
        		req.setAttribute(Attributes.RETURN_VALUE, rslt);
        		k.perform(req, new RuntimeRequestResponse());
        		mainGrammar = (Grammar) rslt.getValue();
            } catch (Throwable t) {
                String msg = "Error parsing Main Grammar.";
                throw new RuntimeException(msg, t);
            }
    	}

        return mainGrammar;

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
        Entry n = getEntry(name, Entry.Type.TASK);

        Task rslt = null;
        EntityConfig config = null;
        try {

            // Create & bootstrap the result...
        	config = prepareEntryConfig(n, e);
            rslt = (Task) n.getFormula().getImplementationClass().newInstance();
            rslt.init(config);

        } catch (Throwable t) {
            String msg = "Unable to create the specified task:  " + name;
            throw new RuntimeException(msg, t);
        }

        return new RuntimeTaskDecorator(rslt, config);

    }

    public Phrase newPhrase(String inpt) {
    	return newPhrase(fac.createText(inpt));
    }
    
	public Phrase newPhrase(Node n) {
		
        // Assertions...
        if (n == null) {
            String msg = "Argument 'n [Node]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        List<String> chunks = new LinkedList<String>();
        String chunkMe = n.getText();
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
                            String name = null;     // Name of the phrase to use...
                            String nested = null;   // Content passed to the phrase

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

                            Entry y = getEntry(name, Entry.Type.PHRASE);
                            Phrase p = null;
                            try {

                                // Create & bootstrap the phrase...
                            	EntityConfig config = prepareEntryConfig(y, fac.createText(nested), n.getUniquePath());
                                Phrase enclosed = (Phrase) y.getFormula().getImplementationClass().newInstance();
                                enclosed.init(config);
                                p = new RuntimePhraseDecorator(enclosed, config);

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
     * Package API.
     */

    XmlGrammar(Grammar parent) {
        this(parent, ((XmlGrammar) parent).getClassLoader());
    }

    ClassLoader getClassLoader() {
        return loader;
    }

    void addEntry(Entry e) {
        
        List<Entry> list = entries.get(e.getName());
        if (list == null) {
        	list = new LinkedList<Entry>();
        	entries.put(e.getName(), list);
        }
        list.add(e);

    }
    
    Set<Entry> getEntries() {

    	Set<Entry> rslt = null;
    	if (parent != null && parent instanceof XmlGrammar) {
    		rslt = ((XmlGrammar) parent).getEntries();
    	} else {
    		rslt = new HashSet<Entry>();
    	}
    	
    	for (List<Entry> list : entries.values()) {
    		for (Entry y : list) {
        		rslt.add(y);
    		}
    	}
    	
    	return rslt;

    }

    /*
     * Implementation.
     */

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

        // NB:  Tasks & phrases are added after creation...
        this.entries = Collections.synchronizedMap(new HashMap<String,List<Entry>>());

    }

    private Entry getEntry(String name, Entry.Type type) {
    	
        Entry rslt = null;

        // If there's a matching entry w/in this Grammar it trumps all...
        if (entries.containsKey(name)) {
        	List<Entry> list = entries.get(name);
        	for (Entry y : list) {
        		// Be sure we have an entry of the correct type...
        		if (y.getType().equals(type)) {
        			rslt = y;
        			break;
        		}
        	}
        }
        
        if (rslt == null) {
            if (parent != null && parent instanceof XmlGrammar) {
                // This is a little hokey... perhaps Entry should be a first-class type?
                rslt = ((XmlGrammar) parent).getEntry(name, type);
            } else {
                // Assume the name is a class that implements Bootstrappable...
            	rslt = new Entry(name, (Element) null, name, (Element) null, this, new LinkedList<Node>());
            }
        }
        
        return rslt;

    }

    private EntityConfig prepareEntryConfig(Entry n, Node d) {
    	return prepareEntryConfig(n, d, d.getUniquePath());
    }
    
    private EntityConfig prepareEntryConfig(Entry n, Node d, String source) {

        // Assertions...
        if (n == null) {
            String msg = "Argument 'n [Entry]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (d == null) {
            String msg = "Argument 'd [Element]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (source == null) {
            String msg = "Argument 'source' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Report any use of deprecated entries...
        if (n.isDeprecated()) {
			StringBuilder msg = new StringBuilder();
			msg.append("USE OF DEPRECATED API:  A deprecated ENTRY was referenced.")
					.append("\n\t\tEntry Name:  ").append(n.getName())
					.append("\n\t\tDeprecated Since:  ").append(n.getDeprecation().getVersion())
					.append("\n\t\tEntry Type:  ").append(n.getType())
					.append("\n\t\tSource:  ").append(source)
					.append("\n");
			log.warn(msg.toString());
        }        
        
        try {
        	
            Formula f = n.getFormula();

            Map<Reagent,Object> mappings = new HashMap<Reagent,Object>();
            List<Reagent> needed = new ArrayList<Reagent>(f.getReagents());
            needed.removeAll(n.getMappings().keySet());
            for (Reagent r : needed) {
                Object value = r.getReagentType().evaluate(this, d, r.getXpath());
                
                // Report any use of deprecated reagents...
                if (r.isDeprecated() && value != null) {
        			StringBuilder msg = new StringBuilder();
        			msg.append("USE OF DEPRECATED API:  A value was specified for a deprecated REAGENT.")
        					.append("\n\t\tReagent Name:  ").append(r.getName())
        					.append("\n\t\tDeprecated Since:  ").append(r.getDeprecation().getVersion())
        					.append("\n\t\tEntry Name:  ").append(n.getName())
        					.append("\n\t\tEntry Type:  ").append(n.getType())
        					.append("\n\t\tSource:  ").append(d.getUniquePath() + "/" + r.getXpath())
        					.append("\n");
        			log.warn(msg.toString());
                }
                
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
            
            String entryName = null;
            if (n.getType().equals(Entry.Type.TASK)) {
            		entryName = "<" + n.getName() + ">";
            } else if (n.getType().equals(Entry.Type.PHRASE)) {
            		entryName = "${" + n.getName() + "}";
            } else {
            	throw new RuntimeException("Unsupported Entry Type:  " + n.getType());
            }

            return new SimpleEntityConfig(this, entryName, source, n.getFormula(), mappings);

        } catch (Throwable t) {
			StringBuilder msg = new StringBuilder();
			msg.append("Unable to prepare an EntityConfig based on the specified information:")
					.append("\n\t\tEntity Name:  ").append(n.getName())
					.append("\n\t\tSource:  ").append(source);
			throw new RuntimeException(msg.toString(), t);
		}

    }

    /*
     * Nested Types.
     */
    
    private static final class ReturnValueImpl implements ReturnValue {
		
		// Instance Members.
		private Object value;
		
		
		/*
		 * Public API.
		 */
		
		public Object getValue() {
			return value;
		}
		
		public void setValue(Object value) {
			this.value = value;
		}

	}

}
