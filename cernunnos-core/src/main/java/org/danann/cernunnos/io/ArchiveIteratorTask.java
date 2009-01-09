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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
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
		ZipInputStream zip = null;
		try {
			final String urlExternalForm = url.toExternalForm();
            
			try {
                zip = new ZipInputStream(new BufferedInputStream(url.openStream()));
            }
            catch (IOException ioe) {
                throw new RuntimeException("Failed to open input stream for URL: " + urlExternalForm, ioe);
            }

			// Set the default CONTEXT for subtasks...
			res.setAttribute(Attributes.CONTEXT, "jar:" + urlExternalForm + "!/");

			try {
    			for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
    				if (entry.isDirectory()) {
    					// We need to skip these b/c many possible subtasks will
    					// choke on them.  Hope that doesn't become a problem.
    					continue;
    				}
    				
    				res.setAttribute(Attributes.LOCATION, entry.getName());
    				super.performSubtasks(req, res); //Outer try/catch should never see an IOException from performSubtasks
    				
    				zip.closeEntry();
    			}
			}
			catch (IOException ioe) {
			    throw new RuntimeException("Failed to read specified archive:  " + urlExternalForm, ioe);
			}
		} finally {
		    //Problems closing an InputStream can be ignored
		    IOUtils.closeQuietly(zip);
		}

	}

}