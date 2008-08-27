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

/**
 * Exception class that contains enhanced debugging information from the 
 * Cernunnos runtime. 
 */
public class ManagedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private static final String PREAMBLE = "The Cernunnos Runtime encountered an error:";
	
	/*
	 * Public API.
	 */
	
	/**
	 * Creates a new <code>ManagedException</code> that originated from the 
	 * specified source.
	 * 
	 * @param source The origin of the problem within the Cernunnos XML.
	 * @param cause The cause of this exception.
	 */
	public ManagedException(EntityConfig config, Throwable cause) {
		super(prepareMessage(config), cause);
	}
	
	/*
	 * Implementation.
	 */

	private static String prepareMessage(EntityConfig config) {
		
		// Assertions...
		if (config == null) {
			String msg = "Argument 'config' cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		
		StringBuilder rslt = new StringBuilder();
		rslt.append(PREAMBLE)
				.append("\n\t\tEntity Name:  ").append(config.getEntryName())
				.append("\n\t\tSource:  ").append(config.getSource());

		return rslt.toString();
		
	}
	
}
