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

public final class LdapAttributes {

	/**
	 * Use this name to create a request attribute containing an 
	 * <code>org.springframework.ldap.core.support.ContextSource</code> object 
	 * for use with Cernunnos LDAP features.
	 */
	public static final String CONTEXT_SOURCE = "LdapAttributes.CONTEXT_SOURCE";

	/**
	 * Use this name to create a request attribute containing a 
	 * <code>javax.naming.directory.Search</code> object 
	 * for use with Cernunnos LDAP features.
	 */
	public static final String SEARCH_CONTROLS = "LdapAttributes.SEARCH_CONTROLS";

}