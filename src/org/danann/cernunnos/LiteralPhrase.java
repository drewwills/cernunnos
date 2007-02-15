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
 * Wraps the <code>Phrase</code> contract around a specific, literal 
 * <code>Object</code>.  <code>LiteralPhrase</code> does not provide a 
 * zero-argument constructor, and therefore cannot be used in grammar 
 * definitions.  Instances of <code>LiteralPhrase</code> are created directly by 
 * code.
 */
public final class LiteralPhrase implements Phrase {

	// Instance Members.
	private final Object value;
	
	/*
	 * Public API.
	 */

	/**
	 * Creates a new <code>LiteralPhrase</code> surrounding the specified 
	 * object.
	 * 
	 * @param value A valid, non-null <code>Object</code> reference.
	 */
	public LiteralPhrase(Object value) {

		// Assertions...
		if (value == null) {
			String msg = "Argument 'value' cannot be null.";
			throw new IllegalArgumentException(msg);
		}

		// Instance Members.
		this.value = value;
		
	}

	public Formula getFormula() {
		throw new UnsupportedOperationException();
	}

	public void init(EntityConfig config) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Always returns the object that was specified at creation.
	 * 
	 * @param req Representations the input to the current task.
	 * @param res Representations the output of the current task.
	 * @return The final, actual value of this <code>Phrase</code>. 
	 */
	public Object evaluate(TaskRequest req, TaskResponse res) {
		return value;
	}

}