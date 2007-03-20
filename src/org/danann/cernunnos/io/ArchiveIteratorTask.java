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

import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.danann.cernunnos.AbstractContainerTask;
import org.danann.cernunnos.AttributePhrase;
import org.danann.cernunnos.Attributes;
import org.danann.cernunnos.CurrentDirectoryUrlPhrase;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public final class ArchiveIteratorTask extends AbstractContainerTask {

	// Instance Members.
	private Phrase context;
	private Phrase location;

	/*
	 * Public API.
	 */

	public static final Reagent CONTEXT = new SimpleReagent("CONTEXT", "@context", ReagentType.PHRASE, String.class, 
				"Optional context from which missing elements of the LOCATION will be inferred if it is "
				+ "relative.  If omitted, this task will use either: (1) the value of the 'Attributes.CONTEXT' "
				+ "request attribute if present; or (2) the directory within which Java is executing.", 
				new AttributePhrase(Attributes.CONTEXT, new CurrentDirectoryUrlPhrase()));

	public static final Reagent LOCATION = new SimpleReagent("LOCATION", "@location", ReagentType.PHRASE, String.class, 
				"Optional location of the archive that will be iterated over.  It may be a filesystem path or "
				+ "a URL, and may be absolute or relative.  If relative, the location will be evaluated "
				+ "from the CONTEXT.  If omitted, the value of the 'Attributes.LOCATION' request "
				+ "attribute will be used.", new AttributePhrase(Attributes.LOCATION));

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {CONTEXT, LOCATION, AbstractContainerTask.SUBTASKS};
		final Formula rslt = new SimpleFormula(ArchiveIteratorTask.class, reagents);
		return rslt;
	}

	public void init(EntityConfig config) {

		super.init(config);		

		// Instance Members.
		this.context = (Phrase) config.getValue(CONTEXT);
		this.location = (Phrase) config.getValue(LOCATION);
		
	}

	public void perform(TaskRequest req, TaskResponse res) {
		
		String loc = (String) location.evaluate(req, res);
		
		try {
			
			URL ctx = new URL((String) context.evaluate(req, res));
			ZipInputStream zip = new ZipInputStream(new URL(ctx, loc).openStream());
			
			for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
				res.setAttribute(Attributes.LOCATION, entry.getName());
				super.performSubtasks(req, res);
				zip.closeEntry();
			}
			
			zip.close();

		} catch (Throwable t) {
			String msg = "Unable to read the specified archive:  " + loc;
			throw new RuntimeException(msg, t);
		}

	}
	
}