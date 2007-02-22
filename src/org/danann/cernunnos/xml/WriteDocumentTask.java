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

package org.danann.cernunnos.xml;

import java.io.File;
import java.io.FileOutputStream;

import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import org.danann.cernunnos.AttributePhrase;
import org.danann.cernunnos.Attributes;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Task;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

/**
 * <code>Task</code> implementation that writes the NODE node to the specified 
 * file.
 */
public final class WriteDocumentTask implements Task {

	// Instance Members.
	private Phrase file;
	private Phrase node;

	/*
	 * Public API.
	 */
	
	public static final Reagent FILE = new SimpleReagent("FILE", "@file", ReagentType.PHRASE, String.class, 
							"File system location to which the document should be written.");

	public static final Reagent NODE = new SimpleReagent("NODE", "@node", ReagentType.PHRASE, Node.class,
							"Optional source node to be written.  If not provided, the value of the "
							+ "'Attributes.NODE' request attribute will be used.", 
							new AttributePhrase(Attributes.NODE));

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {FILE, NODE};
		final Formula rslt = new SimpleFormula(WriteDocumentTask.class, reagents);
		return rslt;
	}

	public void init(EntityConfig config) {

		// Instance Members.
		this.file = (Phrase) config.getValue(FILE); 
		this.node = (Phrase) config.getValue(NODE); 
		
	}

	public void perform(TaskRequest req, TaskResponse res) {

		File f = new File((String) file.evaluate(req, res));
		
		try {
			XMLWriter writer = new XMLWriter(new FileOutputStream(f), new OutputFormat("  ", true));
			writer.write((Node) node.evaluate(req, res));
		} catch (Throwable t) {
			String msg = "Unable to write the specified file:  " + f.getPath();
			throw new RuntimeException(msg, t);
		}
				
	}
	
}