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


/**
 * <code>PhraseComponent</code> implementation that evaluates to an attribute 
 * set on the <code>TaskRequest</code>.
 */
public final class AttributePhrase implements Phrase {

	// Instance Members.
	private Phrase name;
	private Phrase dflt;

	/*
	 * Public API.
	 */

	/**
	 * Constructor used by the Cernunnos runtime.  Members will be populated in 
	 * the <code>init</code> method.
	 */
	public AttributePhrase() {
		this((Phrase)null, (Phrase)null);
	}
	
	/**
	 * Constructs an <code>AttributePhrase</code> that looks for a request 
	 * attribute with the specified <code>name</code>.
	 * 
	 * @param name A phrase that will evaluate to the name of a request 
	 * attribute.
	 */
	public AttributePhrase(Phrase name) {
		this(name, null);
	}

	/**
	 * Constructs an <code>AttributePhrase</code> that looks for a request 
	 * attribute with the specified <code>name</code>.
	 * 
	 * @param name The name of a request attribute.
	 */
	public AttributePhrase(String name) {
		this(new LiteralPhrase(name), null);
	}

	/**
	 * Constructs an <code>AttributePhrase</code> that looks for a request 
	 * attribute with the specified <code>name</code> but returns the value of 
	 * <code>dflt</code> if that attribute is not present.
	 * 
	 * @param name The name of a request attribute.
	 * @param dflt Alternate value to use if the named attribute is not present.
	 */
	public AttributePhrase(String name, Phrase dflt) {
		this(new LiteralPhrase(name), dflt);
	}
	
	/**
	 * Constructs an <code>AttributePhrase</code> that looks for a request 
	 * attribute with the specified <code>name</code> but returns the value of 
	 * <code>dflt</code> if that attribute is not present.
	 * 
	 * @param name A phrase that will evaluate to the name of a request 
	 * attribute.
	 * @param dflt Alternate value to use if the named attribute is not present.
	 */
	public AttributePhrase(Phrase name, Phrase dflt) {
		// NB:  Either argument may be null... 
		this.name = name;
		this.dflt = dflt;
	}
	
	public static final Reagent NAME = new SimpleReagent("KEY", "descendant-or-self::text()", ReagentType.PHRASE, 
								String.class, "The name of an attribute defined in the TaskRequest.");

	public static final Reagent DEFAULT = new SimpleReagent("DEFAULT", "@default", ReagentType.PHRASE, String.class, 
								"If specified, this value will be used returned if the named attribute is not "
								+ "present on the request.  If a DEFAULT is not specified and the named attribute "
								+ "is not present, an error will occur.", null);

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {NAME, DEFAULT};
		return new SimpleFormula(AttributePhrase.class, reagents);
	}
	
	public void init(EntityConfig config) {

		// Instance Members.
		this.name = (Phrase) config.getValue(NAME);
		this.dflt = (Phrase) config.getValue(DEFAULT);

	}

	public Object evaluate(TaskRequest req, TaskResponse res) {

		String n = (String) name.evaluate(req, res);
		
		// Check to see if the default should be appliead instead...
		if (dflt != null && !req.hasAttribute(n)) {
			return dflt.evaluate(req, res);
		}
		
		return req.getAttribute(n);
		
	}
	
}