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

import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.danann.cernunnos.AbstractContainerTask;
import org.danann.cernunnos.Attributes;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ResourceHelper;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public final class ArchiveIteratorTask extends AbstractContainerTask {

	// Instance Members.
    private final ResourceHelper resource = new ResourceHelper();

	/*
	 * Public API.
	 */

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {ResourceHelper.CONTEXT_TARGET, ResourceHelper.LOCATION_TASK, 
		                                                AbstractContainerTask.SUBTASKS};
		final Formula rslt = new SimpleFormula(ArchiveIteratorTask.class, reagents);
		return rslt;
	}

	public void init(EntityConfig config) {

		super.init(config);

		// Instance Members.
		resource.init(config);

	}

	public void perform(TaskRequest req, TaskResponse res) {

        URL url = resource.evaluate(req, res);
		InputStream inpt = null;
		ZipInputStream zip = null;
		try {

			inpt = url.openStream();
			zip = new ZipInputStream(inpt);

			// Set the default CONTEXT for subtasks...
			res.setAttribute(Attributes.CONTEXT, "jar:" + url.toExternalForm() + "!/");

			for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
				if (entry.isDirectory()) {
					// We need to skip these b/c many possible subtasks will
					// choke on them.  Hope that doesn't become a problem.
					continue;
				}
				res.setAttribute(Attributes.LOCATION, entry.getName());
				super.performSubtasks(req, res);
				zip.closeEntry();
			}

		} catch (Throwable t) {
			String msg = "Unable to read the specified archive:  " + url.toExternalForm();
			throw new RuntimeException(msg, t);
		} finally {
			if (zip != null) {
				try {
					zip.close();
				} catch (IOException ioe) {
					throw new RuntimeException(ioe);
				}
			}
			if (inpt != null) {
				try {
					inpt.close();
				} catch (IOException ioe) {
					throw new RuntimeException(ioe);
				}
			}
		}

	}

}