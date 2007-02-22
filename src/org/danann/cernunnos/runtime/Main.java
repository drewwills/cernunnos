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
import java.io.InputStream;
import java.net.URL;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;

import org.danann.cernunnos.Grammar;
import org.danann.cernunnos.Task;

public final class Main {
	
	// Static Members.
	private static final String DEFAULT_GRAMMAR = "cernunnos.xml";
		
	public static void main(String[] args) {
		
		// Put some whitespace between the command and the output...
		System.out.println("");

		// Analyze the command-line arguments & make some decisions...
		URL url = null;
		RuntimeRequestResponse req = new RuntimeRequestResponse();
		switch (args.length) {
			case 0:
				// No file provided, can't continue...
				System.out.println("Usage:\n\n\t>crn [script_name] [arguments]");
				break;
			default:
				try {
					url = new URL(new File(".").toURL(), args[0]);
				} catch (Throwable t) {
					String msg = "Unable to read the specified script:  " + args[0];
					throw new RuntimeException(msg, t);
				}
				for (int i=1; i < args.length; i++) {
					req.setAttribute("$" + i, args[i]);
				}
				break;
		}

		SAXReader reader = new SAXReader();		

		// XmlGrammar.
		Grammar g = null;
		try {
			InputStream inpt = ClassLoader.getSystemResourceAsStream(DEFAULT_GRAMMAR);	// ToDo:  Make the grammar configurable...
			Document doc = reader.read(inpt);
			g = XmlGrammar.parse(doc.getRootElement());
		} catch (Throwable t) {
			System.out.println("");
			t.printStackTrace(System.out);
		}
		
		// Project.
		Task script = null;
		try {
			Document doc = reader.read(url);
			script = g.newTask(doc.getRootElement(), null);
		} catch (Throwable t) {
			System.out.println("");
			t.printStackTrace(System.out);
		}
		
		script.perform(req, new RuntimeRequestResponse());
		
	}

}