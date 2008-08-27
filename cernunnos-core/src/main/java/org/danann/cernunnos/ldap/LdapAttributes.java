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

package org.danann.cernunnos.ldap;

import java.util.Map;
import javax.naming.directory.SearchControls;

import org.springframework.ldap.core.ContextSource;

import org.danann.cernunnos.Attributes;
import org.danann.cernunnos.BindingsHelper;

public final class LdapAttributes {

	static {
		Attributes.registerBindings("LdapAttributes", BindingsHelperImpl.class);
	}

	/*
	 * Public API.
	 */

	/**
	 * Use this name to create a request attribute containing an 
	 * <code>org.springframework.ldap.core.ContextSource</code> object 
	 * for use with Cernunnos LDAP features.
	 */
	public static final String CONTEXT_SOURCE = LdapAttributes.class.getSimpleName() + ".CONTEXT_SOURCE";

	/**
	 * Use this name to create a request attribute containing a 
	 * <code>javax.naming.directory.SearchControls</code> object 
	 * for use with Cernunnos LDAP features.
	 */
	public static final String SEARCH_CONTROLS = LdapAttributes.class.getSimpleName() + ".SEARCH_CONTROLS";

	/*
	 * Nested Types.
	 */

	public static final class BindingsHelperImpl implements BindingsHelper {

		/*
		 * Public API.
		 */
		
		public final ContextSource CONTEXT_SOURCE;
		public final SearchControls SEARCH_CONTROLS;
		
		public BindingsHelperImpl(Map<String,Object> bindings) {
			
			// Assertions.
			if (bindings == null) {
				String msg = "Argument 'bindings' cannot be null.";
				throw new IllegalArgumentException(msg);
			}
			
			// Instance Members.
			this.CONTEXT_SOURCE = (ContextSource) bindings.get(LdapAttributes.CONTEXT_SOURCE);
			this.SEARCH_CONTROLS = (SearchControls) bindings.get(LdapAttributes.SEARCH_CONTROLS);
			
		}
		
		public String getBindingName() {
			return "LdapAttributes";
		}
		
	}

}
