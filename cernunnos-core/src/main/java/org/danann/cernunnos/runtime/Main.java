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

package org.danann.cernunnos.runtime;

import java.io.File;
import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class Main {

	public static void main(String[] args) {

        Log log = LogFactory.getLog(ScriptRunner.class);

        // Put some whitespace between the command and the output...
		System.out.println("");

		// Register custom protocol handlers...
		try {
			URL.setURLStreamHandlerFactory(new URLStreamHandlerFactoryImpl());
		} catch (Throwable t) {
			log.warn("Cernunnos was unable to register a URLStreamHandlerFactory.  " +
					"Custom URL protocols may not work properly (e.g. classpath://, c:/).  " +
					"See stack trace below.", t);
		}
		
		// Establish CRN_HOME...
		String crnHome = System.getenv("CRN_HOME");
		if (crnHome == null) {
		    if (log.isDebugEnabled()) {
	            log.debug("The CRN_HOME environment variable is not defined;  " +
	            		"this is completely normal for embedded applications " +
	            		"of Cernunnos.");
		    }
		}
		
		// Look at the specified script:  might it be in the bin/ directory?
		String location = args[0];
		try {
		    // This is how ScriptRunner will attempt to locate the script file...
	        URL u = new URL(new File(".").toURI().toURL(), location);
	        if (u.getProtocol().equals("file")) {
	            // We know the specified resource is a local file;  is it either 
	            // (1) absolute or (2) relative to the directory where Java is 
	            // executing?
	            if (!new File(u.toURI()).exists() && crnHome != null) {
	                // No.  And what's more, 'CRN_HOME' is defined.  In this case 
	                // let's see if the specified script *is* present in the bin/ 
	                // directory.
	                StringBuilder path = new StringBuilder();
	                path.append(crnHome).append(File.separator).append("bin")
	                            .append(File.separator).append(location);
	                File f = new File(path.toString());
	                if (f.exists()) {
	                    // The user is specifying a Cernunnos script in the bin/ directory...
	                    location = f.toURI().toURL().toExternalForm();
	                    if (log.isInfoEnabled()) {
	                        log.info("Resolving the specified Cernunnos document " +
	                                "to a file in the CRN_HOME/bin directory:  " + 
	                                location);
	                    }
	                }
	            }
	        }
        } catch (Throwable t) {
            // Just let this pass -- genuine issues will be caught & reported shortly...
        }
        
		// Analyze the command-line arguments...
		RuntimeRequestResponse req = new RuntimeRequestResponse();
		switch (args.length) {
			case 0:
				// No file provided, can't continue...
				System.out.println("Usage:\n\n\t>crn [script_name] [arguments]");
				System.exit(0);
				break;
			default:
				for (int i=1; i < args.length; i++) {
					req.setAttribute("$" + i, args[i]);
				}
				break;
		}

		ScriptRunner runner = new ScriptRunner();
		runner.run(location, req);

	}

	/*
	 * Nested Types.
	 */

    private static final class URLStreamHandlerFactoryImpl implements URLStreamHandlerFactory {

		public URLStreamHandler createURLStreamHandler(String protocol) {

			// Assertions.
			if (protocol == null) {
				String msg = "Argument 'protocol' cannot be null.";
				throw new IllegalArgumentException(msg);
			}

			URLStreamHandler rslt = null;

			if (protocol.equals("classpath")) {
				rslt = new ClasspathURLStreamHandler();
			} else if (protocol.matches("\\A[a-zA-Z]\\z")) {
				rslt = new WindowsDriveURLStreamHandler();
			}

			return rslt;

		}

	}

}