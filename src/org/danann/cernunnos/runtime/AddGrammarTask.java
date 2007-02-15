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
import java.io.FileInputStream;
import java.net.URL;

import org.dom4j.io.SAXReader;

import org.danann.cernunnos.AbstractContainerTask;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Grammar;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public class AddGrammarTask extends AbstractContainerTask {
	
	// Instance Members.
	private String context;
	private String location;
	private String classpath;
	
	/*
	 * Public API.
	 */
	
	public static final Reagent CONTEXT = new SimpleReagent("CONTEXT", "@context", ReagentType.STRING, String.class, 
				"The context from which missing elements of the LOCATION can be inferred if it is relative.  "
				+ "The default is the filesystem location from which Java is executing.  WARNING:  This reagent "
				+ "must be a String (not a Phrase) because it gets used at boostrap time.  A URL will be "
				+ "constructed from the value via new URL(String spec).", createDefaultUrl());

	public static final Reagent LOCATION = new SimpleReagent("LOCATION", "@location", ReagentType.STRING, String.class,
					"The location of the grammar file that defines the grammar to add.  WARNING:  This reagent "
					+ "must be a String (not a Phrase) because it gets used at boostrap time (not runtime).");

	public static final Reagent CLASSPATH = new SimpleReagent("CLASSPATH", "@classpath", ReagentType.STRING, String.class,
					"Optional filesystem location from which task implementations should be loaded.  By "
					+ "default, the system classpath is used.  WARNING:  This reagent must be a String (not "
					+ "a Phrase) because it gets used at boostrap time (not runtime).", null);
	
	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {CONTEXT, LOCATION, CLASSPATH, AbstractContainerTask.SUBTASKS};
		final Formula rslt = new SimpleFormula(AddGrammarTask.class, reagents);
		return rslt;
	}
	
	public void init(EntityConfig config) {

		// Instance Members.
		this.context = (String) config.getValue(CONTEXT);
		this.location = (String) config.getValue(LOCATION);
		this.classpath = (String) config.getValue(CLASSPATH);
		
		ClassLoader loader = ClassLoader.getSystemClassLoader();	// default...
		if (this.classpath != null) {
			loader = new ClassLoaderImpl(this.classpath);
		}
		
		Grammar g = null;
		try {
			URL ctx = new URL(this.context); 
			URL loc = new URL(ctx, location);
			g = XmlGrammar.parse(new SAXReader().read(loc.openStream()).getRootElement(), 
													config.getGrammar(), loader);
		} catch (Throwable t) {
			String msg = "Unable to parse a grammar from the specified location:  " + this.location; 
			throw new RuntimeException(msg, t);
		}

		super.init(new SimpleEntityConfig(g, config.getFormula(), config.getValues()));
		
	}

	public void perform(TaskRequest req, TaskResponse res) {
		super.performSubtasks(req, res);
	}

	/*
	 * Implementation.
	 */
	
	private static String createDefaultUrl() {

		String rslt = null;
		
		try {
			rslt = new File(".").toURL().toString();
		} catch (Throwable t) {
			String msg = "Unable to create a URL representation of the current directory.";
			throw new RuntimeException(msg, t);
		}

		return rslt;
		
	}
	
	/*
	 * Nested Types.
	 */

	private static final class ClassLoaderImpl extends ClassLoader {
		
		// Instance Members.
		private final String classpath;
		
		/*
		 * Public API.
		 */

		public ClassLoaderImpl(String classpath) {

			// Assertions...
			if (classpath == null) {
				String msg = "Argument 'classpath' cannot be null.";
				throw new IllegalArgumentException(msg);
			}
			if (!(new File(classpath).exists())) {
				String msg = "The specified classpath location does not exist:  " + classpath;
				throw new IllegalArgumentException(msg);
			}
			if (classpath.endsWith("/")) {
				// This isn't an exception, but we must clean it up...
				classpath = classpath.substring(0, classpath.length() - 1);
			}
			
			// Instance Members.
			this.classpath = classpath;
			
		}
		
		protected Class<?> findClass(String name) {
			
			// Assertions...
			if (name == null) {
				String msg = "Argument 'name' cannot be null.";
				throw new IllegalArgumentException(msg);
			}

			String path = classpath + "/" + name.replace('.', '/') + ".class";
			
			Class rslt = null;
			try {
				FileInputStream inpt = new FileInputStream(path);
				byte[] bytes = new byte[inpt.available()];
				inpt.read(bytes, 0, inpt.available());
				rslt = super.defineClass(name, bytes, 0, bytes.length);				
			} catch (Throwable t) {
				String msg = "Unable to load the specified class file:  " + path;
				throw new RuntimeException(msg, t);
			}
			
			return rslt;

		}
		
	}
	
}