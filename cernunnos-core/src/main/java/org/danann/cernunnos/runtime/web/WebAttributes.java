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

package org.danann.cernunnos.runtime.web;

import java.util.Map;

import org.danann.cernunnos.Attributes;
import org.danann.cernunnos.BindingsHelper;

public final class WebAttributes {

	static {
		Attributes.registerBindings("WebAttributes", BindingsHelperImpl.class);
	}

	/*
	 * Public API.
	 */

	/**
	 * The name under which the request object (<code>ActionRequest</code> for
	 * portlets or HttpServletRequest for servlets) will be registered as a
	 * request attribute within Cernunnos.
	 */
	public static final String REQUEST = WebAttributes.class.getSimpleName() + ".REQUEST";

	/**
	 * The name under which the response object (<code>ActionResponse</code> for
	 * portlets or HttpServletResponse for servlets) will be registered as a
	 * request attribute within Cernunnos.
	 */
	public static final String RESPONSE = WebAttributes.class.getSimpleName() + ".RESPONSE";

	/*
	 * Nested Types.
	 */

	public static final class BindingsHelperImpl implements BindingsHelper {

		/*
		 * Public API.
		 */
		
		// NB:  REQUEST and RESPONSE are type Object b/c they may be instances 
		// of either ServletRequest or PortletRequest, which don't share a common 
		// hierarchy...  
		public final Object REQUEST;
		public final Object RESPONSE;
		
		public BindingsHelperImpl(Map<String,Object> bindings) {
			
			// Assertions.
			if (bindings == null) {
				String msg = "Argument 'bindings' cannot be null.";
				throw new IllegalArgumentException(msg);
			}
			
			// Instance Members.
			this.REQUEST = bindings.get(WebAttributes.REQUEST);
			this.RESPONSE = bindings.get(WebAttributes.RESPONSE);
			
		}
		
		public String getBindingName() {
			return "WebAttributes";
		}
		
	}

}
