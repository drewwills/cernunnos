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

public final class Main {
				
	public static void main(String[] args) {
				
		// Put some whitespace between the command and the output...
		System.out.println("");

		// Analyze the command-line arguments...
		RuntimeRequestResponse req = new RuntimeRequestResponse();
		switch (args.length) {
			case 0:
				// No file provided, can't continue...
				System.out.println("Usage:\n\n\t>crn [script_name] [arguments]");
				System.exit(0);
				break;
			default:
				for (int i=1; i < args.length; i++) {
					req.setAttribute("$" + i, args[i]);
				}
				break;
		}
		
		ScriptRunner runner = new ScriptRunner();
		runner.run(args[0], req);
		
	}

}