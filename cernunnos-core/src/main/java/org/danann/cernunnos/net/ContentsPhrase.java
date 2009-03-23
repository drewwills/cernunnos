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

package org.danann.cernunnos.net;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ResourceHelper;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public final class ContentsPhrase implements Phrase {

	// Instance Members.
    private final ResourceHelper resource = new ResourceHelper();

	/*
	 * Public API.
	 */

	public Formula getFormula() {
        Reagent[] reagents = new Reagent[] {ResourceHelper.CONTEXT_TARGET, ResourceHelper.LOCATION_PHRASE};
		return new SimpleFormula(ContentsPhrase.class, reagents);
	}

	public void init(EntityConfig config) {

		// Instance Members.
	    this.resource.init(config);

	}

	public Object evaluate(TaskRequest req, TaskResponse res) {

		Object rslt = null;

		InputStream inpt = null;
		try {

	        URL u = resource.evaluate(req, res);
			inpt = u.openStream();


			StringBuffer buff = new StringBuffer();
			byte[] bytes = new byte[1024];
			for (int len = inpt.read(bytes); len > 0; len = inpt.read(bytes)) {
				buff.append(new String(bytes, 0, len));
			}

			rslt = buff.toString();

		} catch (Throwable t) {
			String msg = "ContentsPhrase terminated unexpectedly.";
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