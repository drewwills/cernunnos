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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.ResourceHelper;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.Task;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public final class ExtractArchiveTask implements Task {

    // Instance Members.
    private final ResourceHelper resource = new ResourceHelper();
    private Phrase to_dir;

    /*
     * Public API.
     */

    public static final Reagent TO_DIR = new SimpleReagent("TO_DIR", "@to-dir", ReagentType.PHRASE, String.class,
                "Optional file system directory to which the specified archive will be extracted.  It may be "
                + "absolute or relative.  If relative, it will be evaluated from the directory in "
                + "which Java is executing.", null);

    public Formula getFormula() {
        Reagent[] reagents = new Reagent[] {ResourceHelper.CONTEXT_TARGET, 
                        ResourceHelper.LOCATION_TASK, TO_DIR};
        final Formula rslt = new SimpleFormula(ExtractArchiveTask.class, reagents);
        return rslt;
    }

    public void init(EntityConfig config) {

        // Instance Members.
        this.resource.init(config);
        this.to_dir = (Phrase) config.getValue(TO_DIR);

    }

    public void perform(TaskRequest req, TaskResponse res) {

        String dir = to_dir != null ? (String) to_dir.evaluate(req, res) : null;

        URL loc = resource.evaluate(req, res);
        InputStream inpt = null;
        JarInputStream zip = null;
        try {

            inpt = loc.openStream();
            zip = new JarInputStream(inpt);

            byte[] buffer = new byte[1024];
            for (JarEntry entry = zip.getNextJarEntry(); entry != null; entry = zip.getNextJarEntry()) {

                if (entry.isDirectory()) {
                    // We need to skip directories...
                    continue;
                }

                File f = new File(dir, entry.getName());
                if (f.getParentFile() != null) {
                    // Make sure the necessary directories are in place...
                    f.getParentFile().mkdirs();
                }

                int count;
                OutputStream os = new FileOutputStream(f);
                while ((count = zip.read(buffer)) > 0) {
                    os.write(buffer, 0, count);
                };
                os.close();

                zip.closeEntry();

            }

            Manifest m = zip.getManifest();
            if (m != null) {
                File f = new File(new File(dir, "META-INF"), "MANIFEST.MF");

                if (f.getParentFile() != null) {
                    // Make sure the necessary directories are in place...
                    f.getParentFile().mkdirs();
                }

                OutputStream os = new FileOutputStream(f);
                m.write(os);
                os.close();
            }

        } catch (Throwable t) {
            String msg = "Unable to extract the specified archive:  " 
                                        + loc.toExternalForm();
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
