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

package org.danann.cernunnos.sql;

public final class SqlAttributes {

	/**
	 * The default name under which a <code>javax.sql.DataSource</code> may be
	 * registered as a request attribute.
	 */
	public static final String DATA_SOURCE = "SqlAttributes.DATA_SOURCE";

	/**
	 * The default name under which the new connection will be registered as a
	 * request attribute.
	 */
	public static final String CONNECTION = "SqlAttributes.CONNECTION";

	/**
	 * The name under which the ResultSetMetadata object will be registered as a
	 * request attribute while iterating the results of a query.
	 */
	public static final String RESULT_SET_METADATA = "SqlAttributes.RESULT_SET_METADATA";

	/**
	 * The name of the current column when iterating over the columns in a
	 * result set.
	 */
	public static final String COLUMN_NAME = "SqlAttributes.COLUMN_NAME";

}