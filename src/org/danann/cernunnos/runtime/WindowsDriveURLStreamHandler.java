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

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public final class WindowsDriveURLStreamHandler extends URLStreamHandler {

	/*
	 * Public API.
	 */

	public URLConnection openConnection(URL url) {
		return new URLConnectionImpl(url);
	}

	/*
	 * Nested Types.
	 */
	
	private static final class URLConnectionImpl extends URLConnection {
		
		/*
		 * Public API.
		 */
		
		public URLConnectionImpl(URL url) {
			super(url);			
		}
		
		public void connect() {}
		
		public InputStream getInputStream() {
						
			InputStream rslt = null;
			try {
				rslt = new URL("file:///" + url.toExternalForm()).openStream();
			} catch (Throwable t) {
				String msg = "Unable to read the specified file:  " + url.toExternalForm();
				throw new RuntimeException(msg, t);
			}

			return rslt;
			
		}
		
		public OutputStream getOutputStream() {
			throw new UnsupportedOperationException();
		}

	}

}