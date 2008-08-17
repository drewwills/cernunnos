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

package org.danann.cernunnos.cvs;

import java.util.Map;

import org.netbeans.lib.cvsclient.Client;

import org.danann.cernunnos.Attributes;
import org.danann.cernunnos.BindingsHelper;

public final class CvsAttributes {

	static {
		Attributes.registerBindings("CvsAttributes", BindingsHelperImpl.class);
	}
	
	/**
	 * Use this name to register an instance of 
	 * <code>org.netbeans.lib.cvsclient.Client</code> for use with CVS command 
	 * tasks.
	 */
	public static final String CLIENT = CvsAttributes.class.getSimpleName() + ".CLIENT";

	/**
	 * Use this name to register the CVSRoot <code>String</code> used to 
	 * prepare the <code>org.netbeans.lib.cvsclient.command.GlobalOptions</code>
	 * object required by CVS command classes.
	 */
	public static final String CVSROOT = CvsAttributes.class.getSimpleName() + ".CVSROOT";

	/*
	 * Nested Types.
	 */

	public static final class BindingsHelperImpl implements BindingsHelper {

		/*
		 * Public API.
		 */
		
		public final Client CLIENT;
		
		public BindingsHelperImpl(Map<String,Object> bindings) {
			
			// Assertions.
			if (bindings == null) {
				String msg = "Argument 'bindings' cannot be null.";
				throw new IllegalArgumentException(msg);
			}
			
			// Instance Members.
			this.CLIENT = (Client) bindings.get(CvsAttributes.CLIENT);
			
		}
		
		public String getBindingName() {
			return "CvsAttributes";
		}
		
	}

}
