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
 * No-nonsense, immutable <code>Reagent</code> implementation for use by task 
 * authors who don't need anything outlandish.
 */
public final class SimpleReagent implements Reagent {

	// Static Members.
	private static final Object NO_DEFAULT_SPECIFIED = new Object();
		
	// Instance Members.
	private final String name;
	private final String xpath;
	private final ReagentType reagentType;
	private final Class<?> returnType;
	private final String description;
	private final Object dflt;
	
	/*
	 * Public API.
	 */
		
	/**
	 * Creates a new <code>SimpleReagent</code> that includes a return type but 
	 * not a specified default value.
	 * 
	 * @param name A descriptive name.
	 * @param xpath An expression to use in gathering the value of this reagent.
	 * @param type The type of this <code>Reagent</code> as defined by
	 * the <code>ReagentType</code> enumeration.
	 * @param returnType The runtime data type that instances of this 
	 * <code>Reagent</code> must return
	 * @param description An explanation of how this reagent will be used.
	 */
	public SimpleReagent(String name, String xpath, ReagentType type, Class<?> returnType, String description) {
		this(name, xpath, type, returnType, description, NO_DEFAULT_SPECIFIED);
	}
	
	/**
	 * Creates a new <code>SimpleReagent</code> that includes both a return type 
	 * and a default value.
	 * 
	 * @param name A descriptive name.
	 * @param xpath An expression to use in gathering the value of this reagent.
	 * @param reagentType The type of this <code>Reagent</code> as defined by
	 * the <code>ReagentType</code> enumeration.
	 * @param returnType The runtime data type that instances of this 
	 * <code>Reagent</code> must return
	 * @param description An explanation of how this reagent will be used.
	 * @param dflt The value this reagent should use if none is provided.
	 */
	public SimpleReagent(String name, String xpath, ReagentType reagentType, Class<?> returnType, String description, Object dflt) {

		// Assertions...
		if (name == null) {
			String msg = "Argument 'name' cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		if (xpath == null) {
			String msg = "Argument 'xpath' cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		if (reagentType == null) {
			String msg = "Argument 'reagentType' cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		// NB:  returnType may be null...
		if (description == null) {
			String msg = "Argument 'description' cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		// NB:  dflt may be null...
		
		// Instance Members.
		this.name = name;
		this.xpath = xpath;
		this.reagentType = reagentType;
		this.returnType = returnType;
		this.description = description;
		this.dflt = dflt;

	}
	
	public String getName() {
		return name;
	}
	
	public String getXpath() {
		return xpath;
	}
	
	public ReagentType getReagentType() {
		return reagentType;
	}
	
	public Class<?> getExpectedType() {
		return returnType;
	}
	
	public String getDescription() {
		return description;
	}

	public boolean hasDefault() {
		return !NO_DEFAULT_SPECIFIED.equals(dflt);
	}

	public Object getDefault() {
		
		// Assertions...
		if (NO_DEFAULT_SPECIFIED.equals(dflt)) {
			String msg = "The reagent has no specified default.";
			throw new RuntimeException(msg);
		}
		
		return dflt;
		
	}
	
	/**
	 * Evaluates to <code>true</code> if the specified object is an instance of 
	 * <code>Reagent</code> and its name is equal to this one's.
	 * 
	 * @param o An object to compare to this reagent.
	 * @return <code>true</code> if the specified object is a 
	 * <code>Reagent</code> with the same XPath expression, otherwise 
	 * <code>false</code>. 
	 */
	public boolean equals(Object o) {

		boolean rslt = false;	// default...
		if (o != null && o instanceof Reagent) {
			rslt = this.name.equals(((Reagent) o).getName());
		}
		
		return rslt;

	}
	
	/**
	 * Returns the value of <code>name.hashCode()</code>.
	 * 
	 * @return The value of <code>name.hashCode()</code>.
	 */
	public int hashCode() {
		return name.hashCode();
	}
	
}