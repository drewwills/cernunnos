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
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.danann.cernunnos.AttributePhrase;
import org.danann.cernunnos.Attributes;
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

public final class WriteFileTask implements Task {

	// Instance Members.
	private Phrase string;
	private Phrase file;

	public static final Reagent STRING = new SimpleReagent("STRING", "@string", ReagentType.PHRASE, String.class, 
			"Contents of the new file as a String.  The default is the value of the 'Attributes.STRING' "
			+ "request attribute.", new AttributePhrase(Attributes.STRING));

	public static final Reagent FILE = new SimpleReagent("FILE", "@file", ReagentType.PHRASE, String.class, 
			"Optional file system path to which the contents of STRING will be written.  It may be "
			+ "absolute or relative, in which case it will be evaluated from the directory in which Java "
			+ "is executing.  The default is the value of the 'Attributes.LOCATION' request attribute.",
			new AttributePhrase(Attributes.LOCATION));

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {STRING, FILE};
		final Formula rslt = new SimpleFormula(WriteFileTask.class, reagents);
		return rslt;
	}

	public void init(EntityConfig config) {
		
		// Instance Members.
		this.string = (Phrase) config.getValue(STRING); 
		this.file = (Phrase) config.getValue(FILE);

	}

	public void perform(TaskRequest req, TaskResponse res) {

		String path = (String) file.evaluate(req, res);

		try {
			
			String s = (String) string.evaluate(req, res);
			
			File f = new File(path);
			if (f.getParentFile() != null) {
				// Make sure the necessary directories are in place...
				f.getParentFile().mkdirs();
			}
			
			OutputStream os = new FileOutputStream(f);
			os.write(s.getBytes());
			os.close();
			
		} catch (Throwable t) {
			String msg = "Unable to write to the specified file:  " + path;
			throw new RuntimeException(msg, t);
		}
		
	}

}