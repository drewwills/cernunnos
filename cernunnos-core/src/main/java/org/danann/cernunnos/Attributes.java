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

package org.danann.cernunnos;

import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Node;

/**
 * Provides static access to some very common attribute names that facilitate
 * elegant cooperation between <code>Task</code> and <code>Phrase</code> 
 * implementations.
 */
public final class Attributes {

	// Static Members.
	private static final Map<String,Class<? extends BindingsHelper>> BINDINGS_HELPERS = 
					new ConcurrentHashMap<String,Class<? extends BindingsHelper>>();
	static {
		Attributes.registerBindings("Attributes", BindingsHelperImpl.class);
	}
	private static final Log log = LogFactory.getLog(Attributes.class);
	
	/*
	 * Public API.
	 */

	/**
	 * Specifies the actual URL of the current script.  This attribute should be
	 * set either by the Cernunnos runtime (most commonly) or by the invoker of
	 * Cernunnos.  In either case, it should be available by default.
	 */
	public static final String ORIGIN = Attributes.class.getSimpleName() + ".ORIGIN";

	/**
	 * Use this name to create a request attribute containing a
	 * <code>java.lang.String</code> that describes a valid URL.  Many
	 * <code>Task</code> implementations will use this URL to resolve relative
	 * paths.
	 */
	public static final String CONTEXT = Attributes.class.getSimpleName() + ".CONTEXT";

	/**
	 * Use this name to create a request attribute containing a
	 * <code>java.lang.String</code> that represents the location of a resource.
	 */
	public static final String LOCATION = Attributes.class.getSimpleName() + ".LOCATION";

	/**
	 * Use this name to create a request attribute containing an
	 * <code>org.dom4j.Node</code>.
	 */
	public static final String NODE = Attributes.class.getSimpleName() + ".NODE";

	/**
	 * Use this name to create a request attribute containing a
	 * <code>java.io.PrintStream</code>.
	 */
	public static final String STREAM = Attributes.class.getSimpleName() + ".STREAM";

	/**
	 * Use this name to create a request attribute containing a
	 * <code>java.lang.String</code>.
	 */
	public static final String STRING = Attributes.class.getSimpleName() + ".STRING";

	/**
	 * Use this name to create a request attribute containing a
	 * <code>java.lang.Object</code>.
	 */
	public static final String OBJECT = Attributes.class.getSimpleName() + ".OBJECT";

	/**
	 * The default name under which a <code>ReturnValue</code> object may be
	 * registered as a request attribute.  The <code>ReturnValue</code> allows 
	 * Cernunnos operations to return a value where appropriate.
	 */
	public static final String RETURN_VALUE = "FlowAttributes.RETURN_VALUE";
	
	public static List<BindingsHelper> prepareBindings(TaskRequest req) {

		// Assertions.
		if (req == null) {
			String msg = "Argument 'bindings' cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		
		// Figure out which BindingsHelpers to prepare.
		Map<String,Map<String,Object>> bindings = new HashMap<String,Map<String,Object>>();
		for (Map.Entry<String,Object> y : req.getAttributes().entrySet()) {
			if (y.getKey().indexOf(".") == -1) {
				// We're only interested in request attributes that 
				// may be based off Attributes-series keys...
				continue;
			}			
			String token = y.getKey().split("\\.")[0];
			if (BINDINGS_HELPERS.containsKey(token)) {
				// We have a match...
				Map<String,Object> m = bindings.get(token);
				if (m == null) {
					// First time... we must create and add the Map...
					m = new HashMap<String,Object>();
					bindings.put(token, m);
				}
				m.put(y.getKey(), y.getValue());
			} else {
				StringBuilder msg = new StringBuilder();
				msg.append("Unable to bind request attribute '").append(y.getKey())
						.append("' because it contains a period ('.');  do not ")
						.append("create request attributes with periods in their ")
						.append("names EXCEPT for Attributes-series entries.");
				log.warn(msg.toString());
			}
		}
		
		// Prepare a BindingsHelper for each identified name...
		List<BindingsHelper> rslt = new LinkedList<BindingsHelper>();
		for (Map.Entry<String,Map<String,Object>> y : bindings.entrySet()) {
			Class<? extends BindingsHelper> clazz = BINDINGS_HELPERS.get(y.getKey());
			try {
				Constructor<? extends BindingsHelper> c = clazz.getConstructor(Map.class);
				rslt.add(c.newInstance(y.getValue()));
			} catch (NoSuchMethodException nsme) {
				String msg = "BindingsHelper class '" + clazz.getName() + 
								"' does not implement required " +
								"contructor (Map<String,Object>).";
				throw new RuntimeException(msg, nsme);
			} catch (Throwable t) {
				String msg = "Unable to create the specified BindingsHelper:  " + clazz.getName();
				throw new RuntimeException(msg, t);
			}
		}
		
		return rslt;

	}
	
	public static void registerBindings(String name, Class<? extends BindingsHelper> clazz) {
		
		// Assertions.
		if (name == null) {
			String msg = "Argument 'name' cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		if (clazz == null) {
			String msg = "Argument 'clazz' cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		
		// Ensure that the caller has provided a unique name...
		if (BINDINGS_HELPERS.containsKey(name)) {
			String msg = "Method registerBindings() was invoked with a binding " +
									"name that already exists:  " + name;
			throw new IllegalArgumentException(msg);
		}
		
		// Ensure that the BindingsHelper impl reports the proper bindingName...
		BindingsHelper h = null;
		try {
			Constructor<? extends BindingsHelper> c = clazz.getConstructor(Map.class);
			h = c.newInstance(new HashMap<String,Object>());
		} catch (Throwable t) {
			String msg = "Unable to create an instance of the specified " +
							"BindingsHelper class:  " + clazz.getName();
			throw new RuntimeException(msg, t);
		}
		if (!h.getBindingName().equals(name)) {
			String msg = "BindingsHelper class '" + clazz.getName() + 
							"' does not provide expected bindingName '" + 
							h.getBindingName() + "'.";
			throw new IllegalArgumentException(msg);
		}
				
		BINDINGS_HELPERS.put(name, clazz);

	}

	/*
	 * Nested Types.
	 */

	private static final class BindingsHelperImpl implements BindingsHelper {

		/*
		 * Public API.
		 */
		
		public final String ORIGIN;
		public final String CONTEXT;
		public final String LOCATION;
		public final Node NODE;
		public final PrintStream STREAM;
		public final String STRING;
		public final Object OBJECT;
		public final Object RETURN_VALUE;
		
		public BindingsHelperImpl(Map<String,Object> bindings) {
			
			// Assertions.
			if (bindings == null) {
				String msg = "Argument 'bindings' cannot be null.";
				throw new IllegalArgumentException(msg);
			}
			
			// Instance Members.
			this.ORIGIN = (String) bindings.get(Attributes.ORIGIN);
			this.CONTEXT = (String) bindings.get(Attributes.CONTEXT);
			this.LOCATION = (String) bindings.get(Attributes.LOCATION);
			this.NODE = (Node) bindings.get(Attributes.NODE);
			this.STREAM = (PrintStream) bindings.get(Attributes.STREAM);
			this.STRING = (String) bindings.get(Attributes.STRING);
			this.OBJECT = bindings.get(Attributes.OBJECT);
			this.RETURN_VALUE = bindings.get(Attributes.RETURN_VALUE);			
			
		}
		
		public String getBindingName() {
			return "Attributes";
		}
		
	}
	
}
