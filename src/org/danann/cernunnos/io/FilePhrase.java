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

package org.danann.cernunnos.io;

import java.io.File;

import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public class FilePhrase implements Phrase {

	// Instance Members.
	private Phrase location;

	/*
	 * Public API.
	 */

	public static final Reagent LOCATION = new SimpleReagent("LOCATION", "descendant-or-self::text()", 
					ReagentType.PHRASE, String.class, "A file system location from which a File "
					+ "object will be created and returned.");

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {LOCATION};
		return new SimpleFormula(FilePhrase.class, reagents);
	}
	
	public void init(EntityConfig config) {

		// Instance Members.
		this.location = (Phrase) config.getValue(LOCATION);

	}

	public Object evaluate(TaskRequest req, TaskResponse res) {
	
		return new File((String) location.evaluate(req, res));
		
	}
	
}