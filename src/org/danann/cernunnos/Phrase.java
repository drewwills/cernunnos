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
 * Represents an expression that can be evaluated to a specific value at 
 * runtime.  Tasks use phrases for configuration settings that might not be 
 * knowable when the project file is authored.  Below are some example task 
 * attributes based on phrases as they might appear in project files:
 * 
 * <ul>
 *   <li><code>path="${ctx(path)}"</code></li>
 *   <li><code>output-file="customers/${xpath(customer/@name)}.xml"</code></li>
 *   <li><code>message="${xpath(string(@date))}"</code></li>
 * </ul>
 * 
 * A phrase may be composed of zero or more literal parts together with zero or 
 * more dynamic parts, and dynamic parts may be nested within other dynamic 
 * parts.  Each part, literal or dynamic, is itself a phrase.
 */
public interface Phrase extends Bootstrappable {

	/**
	 * Used to mark the beginning of a dynamic phrase.
	 */
	static final String OPEN_PHRASE_DELIMITER = "${";

	/**
	 * Used to mark the end of a dynamic phrase.
	 */
	static final String CLOSE_PHRASE_DELIMITER = "}";

	/**
	 * Called durring task execution to obtain a value for the parameter 
	 * represented by this <code>Phrase</code>.
	 * 
	 * @param req Representations the input to the current task.
	 * @param res Representations the output of the current task.
	 * @return The final, actual value of this <code>Phrase</code>. 
	 */
	Object evaluate(TaskRequest req, TaskResponse res);
		
}