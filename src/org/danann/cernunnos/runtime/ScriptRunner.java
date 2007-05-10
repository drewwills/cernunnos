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

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import org.danann.cernunnos.Grammar;
import org.danann.cernunnos.Task;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

/**
 * Simplifies the process of invoking Cernunnos tasks from within Java code.  
 * <code>ScriptRunner</code> allows you to run a script in the form of a 
 * <code>Task</code>, <code>Element</code>, or location (file system or URL), 
 * with or without providing a <code>TaskRequest</code>.
 */
public class ScriptRunner {

	static {
		URL.setURLStreamHandlerFactory(new URLStreamHandlerFactoryImpl());
	}

	// Instance Members.
	private final Grammar grammar;
	
	/*
	 * Public API.
	 */

	/**
	 * Creates a <code>ScriptRunner</code> based on the default 
	 * <code>Grammar</code>.
	 */
	public ScriptRunner() {
		this(XmlGrammar.getMainGrammar());
	}
	
	/**
	 * Creates a <code>ScriptRunner</code> based on the specified 
	 * <code>Grammar</code>.
	 */
	public ScriptRunner(Grammar g) {

		// Assertions.
		if (g == null) {
			String msg = "Argument 'g [Grammar]' cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		
		// Instance Members.
		this.grammar = g;

	}
	
	/**
	 * Invokes the script found at the specified location (file system or URL).
	 * 
	 * @param location A file on the file system or a URL.
	 * @return The <code>TaskResponse</code> that results from invoking the 
	 * specified script.
	 */
	public TaskResponse run(String location) {
		return run(location, new RuntimeRequestResponse());
	}
	
	/**
	 * Invokes the script found at the specified location (file system or URL).
	 * 
	 * @param location A file on the file system or a URL.
	 * @param req A <code>TaskRequest</code> prepared externally.
	 * @return The <code>TaskResponse</code> that results from invoking the 
	 * specified script.
	 */
	public TaskResponse run(String location, TaskRequest req) {

		// Assertions.
		if (location == null) {
			String msg = "Argument 'location' cannot be null.";
			throw new IllegalArgumentException(msg);
		}

		Document doc = null;
		try {
			doc = new SAXReader().read(new URL(new File(".").toURL(), location));
		} catch (Throwable t) {
			String msg = "Error reading a script from the specified location:  " + location;
			throw new RuntimeException(msg, t);
		}
		return run(doc.getRootElement(), req);

	}
	
	/**
	 * Invokes the script defined by the specified element.
	 * 
	 * @param m An <code>Element</code> that defines a task.
	 * @return The <code>TaskResponse</code> that results from invoking the 
	 * specified script.
	 */
	public TaskResponse run(Element m) {
		return run(m, new RuntimeRequestResponse());
	}
	
	/**
	 * Invokes the script defined by the specified element with the specified 
	 * <code>TaskRequest</code>.
	 * 
	 * @param m An <code>Element</code> that defines a Task.
	 * @param req A <code>TaskRequest</code> prepared externally.
	 * @return The <code>TaskResponse</code> that results from invoking the 
	 * specified script.
	 */
	public TaskResponse run(Element m, TaskRequest req) {

		// Assertions.
		if (m == null) {
			String msg = "Argument 'm [Element]' cannot be null.";
			throw new IllegalArgumentException(msg);
		}

		return run(grammar.newTask(m, null), req);

	}
	
	/**
	 * Invokes the specified <code>Task</code>.
	 * 
	 * @param k A bootstrapped <code>Task</code> object.
	 * @return The <code>TaskResponse</code> that results from invoking the 
	 * specified script.
	 */
	public TaskResponse run(Task k) {
		return run(k, new RuntimeRequestResponse());
	}
	
	/**
	 * Invokes the specified <code>Task</code> with the specified 
	 * <code>TaskRequest</code>.  Use this overload of the <code>run</code> 
	 * method if you need to pre-load information into the 
	 * <code>TaskRequest</code>.
	 * 
	 * @param k A bootstrapped <code>Task</code> object.
	 * @param req A <code>TaskRequest</code> prepared externally.
	 * @return The <code>TaskResponse</code> that results from invoking the 
	 * specified script.
	 */
	public TaskResponse run(Task k, TaskRequest req) {
		
		// Assertions.
		if (k == null) {
			String msg = "Argument 'k [Task]' cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		if (req == null) {
			String msg = "Argument 'req' cannot be null.";
			throw new IllegalArgumentException(msg);
		}

		TaskResponse res = new RuntimeRequestResponse();
		k.perform(req, res);
		
		return res;
		
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