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

package org.danann.cernunnos.sql;

import java.sql.ResultSetMetaData;
import java.util.Map;
import javax.sql.DataSource;

import org.springframework.transaction.PlatformTransactionManager;

import org.danann.cernunnos.Attributes;
import org.danann.cernunnos.BindingsHelper;

public final class SqlAttributes {
    
	static {
		Attributes.registerBindings("SqlAttributes", BindingsHelperImpl.class);
	}

	/*
	 * Public API.
	 */

	/**
     * The default name under which a {@link org.springframework.transaction.PlatformTransactionManager} may be
     * registered as a request attribute.
     */
    public static final String TRANSACTION_MANAGER = SqlAttributes.class.getSimpleName() + ".TRANSACTION_MANAGER";

	/**
	 * The default name under which a <code>javax.sql.DataSource</code> may be
	 * registered as a request attribute.
	 */
	public static final String DATA_SOURCE = SqlAttributes.class.getSimpleName() + ".DATA_SOURCE";

	/**
	 * The default name under which the new connection will be registered as a
	 * request attribute.
	 * @deprecated Use <code>DATA_SOURCE</code> instead
	 */
	@Deprecated
	public static final String CONNECTION = SqlAttributes.class.getSimpleName() + ".CONNECTION";

	/**
	 * The name under which the ResultSetMetaData object will be registered as a
	 * request attribute while iterating the results of a query.
	 */
	public static final String RESULT_SET_METADATA = SqlAttributes.class.getSimpleName() + ".RESULT_SET_METADATA";

	/**
	 * The name of the current column when iterating over the columns in a
	 * result set.
	 */
	public static final String COLUMN_NAME = SqlAttributes.class.getSimpleName() + ".COLUMN_NAME";

	/*
	 * Nested Types.
	 */

	public static final class BindingsHelperImpl implements BindingsHelper {

		/*
		 * Public API.
		 */
		
		public final PlatformTransactionManager TRANSACTION_MANAGER;
		public final DataSource DATA_SOURCE;
		public final ResultSetMetaData RESULT_SET_METADATA;
		public final String COLUMN_NAME;
		
		public BindingsHelperImpl(Map<String,Object> bindings) {
			
			// Assertions.
			if (bindings == null) {
				String msg = "Argument 'bindings' cannot be null.";
				throw new IllegalArgumentException(msg);
			}
			
			// Instance Members.
			this.TRANSACTION_MANAGER = (PlatformTransactionManager) bindings.get(SqlAttributes.TRANSACTION_MANAGER);
			this.DATA_SOURCE = (DataSource) bindings.get(SqlAttributes.DATA_SOURCE);
			this.RESULT_SET_METADATA = (ResultSetMetaData) bindings.get(SqlAttributes.RESULT_SET_METADATA);
			this.COLUMN_NAME = (String) bindings.get(SqlAttributes.COLUMN_NAME);
			
		}
		
		public String getBindingName() {
			return "SqlAttributes";
		}
		
	}

}
