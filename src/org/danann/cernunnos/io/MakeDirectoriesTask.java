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
import org.danann.cernunnos.Task;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public final class MakeDirectoriesTask implements Task {

	// Instance Members.
	private Phrase path;

	/*
	 * Public API.
	 */

	public static final Reagent PATH = new SimpleReagent("LOCATION", "@path", ReagentType.PHRASE, String.class, 
								"Path expression describing the directories to make on the "
								+ "filesystem.  May be absolute or relative.");

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {PATH};
		final Formula rslt = new SimpleFormula(MakeDirectoriesTask.class, reagents);
		return rslt;
	}
	
	public void init(EntityConfig config) {
		
		// Instance Members.
		this.path = (Phrase) config.getValue(PATH);

	}

	public void perform(TaskRequest req, TaskResponse res) {

		File dir = new File((String) path.evaluate(req, res));		
		dir.mkdirs();

	}

}