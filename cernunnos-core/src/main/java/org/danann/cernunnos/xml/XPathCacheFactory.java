/*
 * Copyright 2008 Eric Dalquist
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

import org.danann.cernunnos.AbstractCacheHelperFactory;
import org.dom4j.XPath;
import org.dom4j.xpath.DefaultXPath;

/**
 * Shared factory for XPath objects
 *
 * @author Eric Dalquist
 * @version $Revision$
 */
public final class XPathCacheFactory extends AbstractCacheHelperFactory<String, XPath> {
    //Hide factory mutex to avoid unforseen sync problems
    private static final Object FACTORY_MUTEX = new Object();
    
    public static final XPathCacheFactory INSTANCE = new XPathCacheFactory();
    
    private XPathCacheFactory() {
    }
    

    /* (non-Javadoc)
     * @see org.danann.cernunnos.CacheHelper.Factory#createObject(java.lang.Object)
     */
    public XPath createObject(String key) {
        return new DefaultXPath(key);
    }

    /* (non-Javadoc)
     * @see org.danann.cernunnos.CacheHelper.Factory#getMutex(java.lang.Object)
     */
    public Object getMutex(String key) {
        return FACTORY_MUTEX;
    }
}
