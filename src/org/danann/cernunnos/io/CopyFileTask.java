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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

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
import org.danann.cernunnos.Task;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public class CopyFileTask implements Task {

	// Instance Members.
	private Phrase context;
	private Phrase location;
	private Phrase to_dir;
	private Phrase to_file;

	/*
	 * Public API.
	 */

	public static final Reagent CONTEXT = new SimpleReagent("CONTEXT", "@context", ReagentType.PHRASE, String.class, 
				"Optional context location which missing elements of the LOCATION location can be inferred if it is "
				+ "relative.  If omitted, this task will use either: (1) the value of the 'Attributes.CONTEXT' "
				+ "request attribute if present; or (2) the directory within which Java is executing.", 
				new AttributePhrase(Attributes.CONTEXT, new CurrentDirectoryUrlPhrase()));

	public static final Reagent LOCATION = new SimpleReagent("LOCATION", "@location", ReagentType.PHRASE, String.class, 
				"Optional location of the resource that will be copied.  It may be a filesystem path or "
				+ "a URL, and may be absolute or relative.  If relative, the location will be evaluated "
				+ "from the CONTEXT.  If omitted, the value of the 'Attributes.LOCATION' request "
				+ "attribute will be used.", new AttributePhrase(Attributes.LOCATION));

	public static final Reagent TO_DIR = new SimpleReagent("TO_DIR", "@to-dir", ReagentType.PHRASE, String.class, 
				"Optional file system directory to which the specified resource will be copied.  It may be "
				+ "absolute or relative.  If relative, it will be evaluated relative to the directory in "
				+ "which Java is executing.", null);

	public static final Reagent TO_FILE = new SimpleReagent("TO_FILE", "@to-file", ReagentType.PHRASE, String.class, 
				"Optional file system path to which the specified resource will be copied.  It may be absolute "
				+ "or relative (in which case the location will be evaluated relative to the CONTEXT).  "
				+ "If omitted, the the value of the 'Attributes.LOCATION' request attribute will be used.",
				new AttributePhrase(Attributes.LOCATION));

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {CONTEXT, LOCATION, TO_DIR, TO_FILE};
		final Formula rslt = new SimpleFormula(CopyFileTask.class, reagents);
		return rslt;
	}
	
	public void init(EntityConfig config) {
		
		// Instance Members.
		this.context = (Phrase) config.getValue(CONTEXT); 
		this.location = (Phrase) config.getValue(LOCATION);
		this.to_dir = (Phrase) config.getValue(TO_DIR);
		this.to_file = (Phrase) config.getValue(TO_FILE);

	}

	public void perform(TaskRequest req, TaskResponse res) {

		String origin = (String) location.evaluate(req, res);
		String dir = to_dir != null ? (String) to_dir.evaluate(req, res) : null;
		String destination = (String) to_file.evaluate(req, res);

		try {

			URL ctx = new URL((String) context.evaluate(req, res));
			URL loc = new URL(ctx, origin);
			URLConnection conn = loc.openConnection();
			InputStream is = conn.getInputStream();

			
			File f = new File(dir, destination);
			if (f.getParentFile() != null) {
				// Make sure the necessary directories are in place...
				f.getParentFile().mkdirs();
			}
			OutputStream os = new FileOutputStream(f);
			
			int bytesRead = 0;
			for (int avail = is.available(); bytesRead < conn.getContentLength(); avail = is.available()) {
				byte[] b = new byte[avail];
				is.read(b);
				os.write(b);
				bytesRead = bytesRead + avail;
			}
			is.close();
			os.close();
			
		} catch (Throwable t) {
			String msg = "Unable to copy the specified file [" + origin 
								+ "] to_file the specified location [" 
								+ destination + "].";
			throw new RuntimeException(msg, t);
		}
		
	}

}