/*
 * Copyright 2008 Andrew Wills
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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.Adler32;
import java.util.zip.Checksum;

import org.danann.cernunnos.AttributePhrase;
import org.danann.cernunnos.Attributes;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public class ChecksumPhrase implements Phrase {

	// Instance Members.
	private Phrase context;
	private Phrase location;

	/*
	 * Public API.
	 */

	public static final Reagent CONTEXT = new SimpleReagent("CONTEXT", "@context", ReagentType.PHRASE, String.class,
					"The context from which missing elements of the LOCATION can be inferred if it is relative.  " +
					"The default is the value of the 'Attributes.ORIGIN' request attribute.", 
					new AttributePhrase(Attributes.ORIGIN));

	public static final Reagent LOCATION = new SimpleReagent("LOCATION", "descendant-or-self::text()", ReagentType.PHRASE, 
					String.class, "Optional location of the resource for which checksum is needed.  May be a filesystem " +
					"path (absolute or relative), or a URL.  If relative, the location will be evaluated from the " +
					"CONTEXT.  If omitted, the value of the 'Attributes.LOCATION' request attribute will be used.", 
					new AttributePhrase(Attributes.LOCATION));

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {CONTEXT, LOCATION};
		return new SimpleFormula(getClass(), reagents);
	}

	public void init(EntityConfig config) {

		// Instance Members.
		this.context = (Phrase) config.getValue(CONTEXT);
		this.location = (Phrase) config.getValue(LOCATION);

	}

	public Object evaluate(TaskRequest req, TaskResponse res) {
		
		Long rslt = null; 
		
		String ctx_str = (String) context.evaluate(req, res);
		String loc_str = (String) location.evaluate(req, res);

		InputStream inpt = null;
		try {
			
			URL ctx = new URL(ctx_str);
			URL u = new URL(ctx, loc_str);
			inpt = u.openStream();

			Checksum csum = new Adler32();
			byte[] bytes = new byte[1024];
			for (int len = inpt.read(bytes); len > 0; len = inpt.read(bytes)) {
				csum.update(bytes, 0, len);
			}
			
			rslt = csum.getValue();
			
		} catch (Throwable t) {
			String msg = "Unable to compute a checksum for the specified resource:"
						+ "\n\tCONTEXT=" + ctx_str
						+ "\n\tLOCATION=" + loc_str;
			throw new RuntimeException(msg, t);
		} finally {
			if (inpt != null) {
				try {
					inpt.close();
				} catch (IOException ioe) {
					throw new RuntimeException(ioe);
				}
			}
		}
		
		return rslt;
		
	}
	
}
