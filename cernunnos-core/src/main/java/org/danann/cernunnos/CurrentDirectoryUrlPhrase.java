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

import java.io.File;

/**
 * Constructs a <code>URL</code> representation of the filesystem directory from 
 * which Java is executing and returns a <code>String</code> representation of 
 * it.
 */
public final class CurrentDirectoryUrlPhrase implements Phrase {
	
	/*
	 * Public API.
	 */

	/**
	 * Constructs a new <code>CurrentDirectoryUrlPhrase</code>.
	 */
	public CurrentDirectoryUrlPhrase() {}
	
	public Formula getFormula() {
		return new SimpleFormula(CurrentDirectoryUrlPhrase.class, new Reagent[0]);
	}
	
	public void init(EntityConfig config) {}

	/**
	 * Always returns a <code>URL</code> representation of the filesystem 
	 * directory from which Java is executing.
	 * 
	 * @param req Representations the input to the current task.
	 * @param res Representations the output of the current task.
	 * @return The final, actual value of this <code>Phrase</code>. 
	 */
	public Object evaluate(TaskRequest req, TaskResponse res) {
		String rslt = null;
		try {
			rslt = new File(".").toURI().toURL().toExternalForm();
		} catch (Throwable t) {
			String msg = "Unable to represent the current directory as a URL.";
			throw new RuntimeException(msg, t);
		}
		return rslt;
	}
	
}