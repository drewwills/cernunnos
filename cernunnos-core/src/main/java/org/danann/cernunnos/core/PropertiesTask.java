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

package org.danann.cernunnos.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.danann.cernunnos.AbstractContainerTask;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ResourceHelper;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public final class PropertiesTask extends AbstractContainerTask {

    // Instance Members.
    private final ResourceHelper resource = new ResourceHelper();

    /*
     * Public API.
     */

    public Formula getFormula() {
        Reagent[] reagents = new Reagent[] {ResourceHelper.CONTEXT_SOURCE, ResourceHelper.LOCATION_TASK, 
                                                            AbstractContainerTask.SUBTASKS};
        final Formula rslt = new SimpleFormula(getClass(), reagents);
        return rslt;
    }

    public void init(EntityConfig config) {

        super.init(config);

        // Instance Members.
        this.resource.init(config);

    }

    public void perform(TaskRequest req, TaskResponse res) {

        URL loc = resource.evaluate(req, res);

        
        Properties p = new Properties();
        
        InputStream inpt = null;
        try {
            try {
                inpt = loc.openStream();
                p.load(inpt);
            }
            catch (IOException ioe) {
                throw new RuntimeException("Unable to read the specified properties file: " + loc.toExternalForm(), ioe);
            }
        } finally {
            IOUtils.closeQuietly(inpt);
        }

        for (Entry<?,?> e : p.entrySet()) {
            res.setAttribute((String) e.getKey(), e.getValue());
        }

        super.performSubtasks(req, res);
    }

}
