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

import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ResourceHelper;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public class ChecksumPhrase implements Phrase {

	// Instance Members.
	private final ResourceHelper resource = new ResourceHelper();

	/*
	 * Public API.
	 */

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {ResourceHelper.CONTEXT_SOURCE, ResourceHelper.LOCATION_PHRASE};
		return new SimpleFormula(getClass(), reagents);
	}

	public void init(EntityConfig config) {

		// Instance Members.
	    resource.init(config);

	}

	public Object evaluate(TaskRequest req, TaskResponse res) {
		
		Long rslt = null; 
		
        URL u = resource.evaluate(req, res);
		InputStream inpt = null;
		try {
			
			inpt = u.openStream();

			Checksum csum = new Adler32();
			byte[] bytes = new byte[1024];
			for (int len = inpt.read(bytes); len > 0; len = inpt.read(bytes)) {
				csum.update(bytes, 0, len);
			}
			
			rslt = csum.getValue();
			
		} catch (Throwable t) {
			String msg = "Unable to compute a checksum for the specified resource:  " + u.toExternalForm();
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
