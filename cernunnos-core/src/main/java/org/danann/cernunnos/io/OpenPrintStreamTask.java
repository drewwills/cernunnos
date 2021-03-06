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
import java.io.FileNotFoundException;
import java.io.PrintStream;

import org.apache.commons.io.IOUtils;
import org.danann.cernunnos.AbstractContainerTask;
import org.danann.cernunnos.Attributes;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.LiteralPhrase;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public final class OpenPrintStreamTask extends AbstractContainerTask {

	// Instance Members.
	private Phrase attribute_name;
	private Phrase file;

	/*
	 * Public API.
	 */

	public static final Reagent ATTRIBUTE_NAME = new SimpleReagent("ATTRIBUTE_NAME", "@attribute-name", ReagentType.PHRASE, String.class,
			"Optional name under which the new PrintStream will be registered as a request attribute.  If omitted, the name "
			+ "'Attributes.STREAM' will be used.", new LiteralPhrase(Attributes.STREAM));

	public static final Reagent FILE = new SimpleReagent("FILE", "@file", ReagentType.PHRASE, String.class, 
			"File system path to which the PrintStream will be written.  It may be absolute or relative.  "
			+ "If relative, it will be evaluated from the directory in which Java is executing.  ");

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {ATTRIBUTE_NAME, FILE, AbstractContainerTask.SUBTASKS};
		final Formula rslt = new SimpleFormula(OpenPrintStreamTask.class, reagents);
		return rslt;
	}

	public void init(EntityConfig config) {
		
		super.init(config);		

		// Instance Members.
		this.attribute_name = (Phrase) config.getValue(ATTRIBUTE_NAME);
		this.file = (Phrase) config.getValue(FILE);

	}

	public void perform(TaskRequest req, TaskResponse res) {
		
		String loc = (String) file.evaluate(req, res);

		File f = new File(loc);
		if (f.getParentFile() != null) {
			// Make sure the necessary directories are in place...
			f.getParentFile().mkdirs();
		}
		
		PrintStream stream;
        try {
            stream = new PrintStream(f);
        }
        catch (FileNotFoundException fnfe) {
            throw new RuntimeException("Could not open file '" + f + "' for writing", fnfe);
        }
        
		try {
			// Invoke subtasks...
			res.setAttribute((String) attribute_name.evaluate(req, res), stream);
			super.performSubtasks(req, res);
		}
		finally {
		    IOUtils.closeQuietly(stream);
		}
	}

}