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

package org.danann.cernunnos;

/**
 * Provides static access to some very common attribute names that facilitate
 * elegant cooperation between <code>Task</code> implementations.
 */
public final class Attributes {

	/**
	 * Specifies the actual URL of the current script.  This attribute should be
	 * set either by the Cernunnos runtime (most commonly) or by the invoker of
	 * Cernunnos.  In either case, it should be available by default.
	 */
	public static final String ORIGIN = "Attributes.ORIGIN";

	/**
	 * Use this name to create a request attribute containing a
	 * <code>java.lang.String</code> that describes a valid URL.  Many
	 * <code>Task</code> implementations will use this URL to resolve relative
	 * paths.
	 */
	public static final String CONTEXT = "Attributes.CONTEXT";

	/**
	 * Use this name to create a request attribute containing a
	 * <code>java.lang.String</code> that represents the location of a resource.
	 */
	public static final String LOCATION = "Attributes.LOCATION";

	/**
	 * Use this name to create a request attribute containing an
	 * <code>org.dom4j.Node</code>.
	 */
	public static final String NODE = "Attributes.NODE";

	/**
	 * Use this name to create a request attribute containing a
	 * <code>java.io.PrintStream</code>.
	 */
	public static final String STREAM = "Attributes.STREAM";

	/**
	 * Use this name to create a request attribute containing a
	 * <code>java.lang.String</code>.
	 */
	public static final String STRING = "Attributes.STRING";

	/**
	 * Use this name to create a request attribute containing a
	 * <code>java.lang.Object</code>.
	 */
	public static final String OBJECT = "Attributes.OBJECT";

	/**
	 * The default name under which a <code>ReturnValue</code> object may be
	 * registered as a request attribute.  The <code>ReturnValue</code> allows 
	 * Cernunnos operations to return a value where appropriate.
	 */
	public static final String RETURN_VALUE = "FlowAttributes.RETURN_VALUE";

}
