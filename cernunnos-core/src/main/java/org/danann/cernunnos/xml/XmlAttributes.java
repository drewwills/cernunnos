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

package org.danann.cernunnos.xml;

import java.util.Map;

import org.xml.sax.EntityResolver;

import org.danann.cernunnos.Attributes;
import org.danann.cernunnos.BindingsHelper;

public final class XmlAttributes {

	static {
		Attributes.registerBindings("XmlAttributes", BindingsHelperImpl.class);
	}

	/*
	 * Public API.
	 */

	/**
	 * The default name under which a <code>org.xml.sax.EntityResolver</code>
	 * may be registered as a request attribute.
	 */
	public static final String ENTITY_RESOLVER = XmlAttributes.class.getSimpleName() + ".ENTITY_RESOLVER";

	/*
	 * Nested Types.
	 */

	public static final class BindingsHelperImpl implements BindingsHelper {

		/*
		 * Public API.
		 */
		
		public final EntityResolver ENTITY_RESOLVER;
		
		public BindingsHelperImpl(Map<String,Object> bindings) {
			
			// Assertions.
			if (bindings == null) {
				String msg = "Argument 'bindings' cannot be null.";
				throw new IllegalArgumentException(msg);
			}
			
			// Instance Members.
			this.ENTITY_RESOLVER = (EntityResolver) bindings.get(XmlAttributes.ENTITY_RESOLVER);
			
		}
		
		public String getBindingName() {
			return "XmlAttributes";
		}
		
	}

}
