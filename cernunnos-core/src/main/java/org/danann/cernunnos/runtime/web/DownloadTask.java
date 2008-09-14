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

package org.danann.cernunnos.runtime.web;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.LiteralPhrase;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.Task;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public class DownloadTask implements Task {

    // Instance Members.
    private Phrase source;
    private Phrase content_type;
    private Phrase to_file;
    protected final Log log = LogFactory.getLog(this.getClass());

    /*
     * Public API.
     */

    public static final Reagent SOURCE = new SimpleReagent("SOURCE", "@source", ReagentType.PHRASE, Object.class,
                    "The content that will be written to the HttpServletResponse as a download.  Provide " +
                    "either a String, a byte array, or an InputStream.");

    public static final Reagent CONTENT_TYPE = new SimpleReagent("CONTENT_TYPE", "@content-type", ReagentType.PHRASE, 
                    String.class, "Optional content type to specify for the downloaded file.  The default is " +
                    "'application/x-download'", new LiteralPhrase("application/x-download"));

    public static final Reagent TO_FILE = new SimpleReagent("TO_FILE", "@to-file", ReagentType.PHRASE, String.class,
                    "Optional name of the downloaded file for saving, including extension (e.g. 'aLetter.doc').  " +
                    "If provided, this task will set the 'Content-Disposition' header to 'attachment,' which " +
                    "normally makes the browser prompt the user to save the file to the filesystem (instead of " +
                    "view it in the browser).", null);

    public Formula getFormula() {
        Reagent[] reagents = new Reagent[] {SOURCE, CONTENT_TYPE, TO_FILE};
        final Formula rslt = new SimpleFormula(getClass(), reagents);
        return rslt;
    }

    public void init(EntityConfig config) {

        // Instance Members.
        this.source = (Phrase) config.getValue(SOURCE);
        this.content_type = (Phrase) config.getValue(CONTENT_TYPE);
        this.to_file = (Phrase) config.getValue(TO_FILE);

    }

    public void perform(TaskRequest req, TaskResponse res) {

        String cType = (String) content_type.evaluate(req, res);
        
        String fName = to_file != null ? (String) to_file.evaluate(req, res) 
                                            : "(Not Specified)";

        OutputStream os = null;
        try {

            HttpServletResponse httpr = (HttpServletResponse) req.getAttribute(WebAttributes.RESPONSE);
            httpr.setContentType(cType);
            
            // Check for use of TO_FILE...
            if (fName != null) {
                httpr.setHeader("Content-Disposition", "attachment; filename=" + fName);
            }

            // Evaluate the source, act accordingly...
            Object src = source.evaluate(req, res);
            os = httpr.getOutputStream();
            if (src instanceof String) {
                String s = (String) src;
                os.write(s.getBytes());
            } else if (src instanceof byte[]) {
                byte[] bytes = (byte[]) src;
                os.write(bytes);
            } else if (src instanceof InputStream) {
                InputStream is = (InputStream) src;
                byte[] buffer = new byte[1024];
                for (int len = is.read(buffer); len > 0; len = is.read(buffer)) {
                    os.write(buffer, 0, len);
                }
            }
            
            os.flush();

        } catch (Throwable t) {
            
            String msg = "Error sending the specified download:  "
                            + "\n\t\tCONTENT_TYPE=" + cType
                            + "\n\t\tTO_FILE=" + fName;
            throw new RuntimeException(msg, t);
            
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException ioe) {
                    log.error("Failed to close the HTTP response stream.", ioe);
                }
            }
        }

    }

}
