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

import java.net.URL;

import org.danann.cernunnos.AbstractCacheHelperFactory;
import org.danann.cernunnos.AttributePhrase;
import org.danann.cernunnos.CacheHelper;
import org.danann.cernunnos.DynamicCacheHelper;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.LiteralPhrase;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.ResourceHelper;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.EntityResolver;

public final class ParseXmlPhrase implements Phrase {
    
    //Hide factory mutex to avoid unforseen sync problems
    private static final Object FACTORY_MUTEX = new Object();

    // Instance Members.
    private final ResourceHelper resource = new ResourceHelper();
    private CacheHelper<String, Element> documentCache;
    private Phrase entityResolver;

    /*
     * Public API.
     */

    public static final Reagent ENTITY_RESOLVER = new SimpleReagent("ENTITY_RESOLVER", "@entity-resolver", ReagentType.PHRASE,
                    EntityResolver.class, "Optional org.xml.sax.EntityResolver to use in document parsing.  By default, " +
                    "this phrase looks for an EntityResolver instance under the request attribute 'XmlAttributes.ENTITY_RESOLVER' " +
                    "and will use it if present.", new AttributePhrase(XmlAttributes.ENTITY_RESOLVER, new LiteralPhrase(null)));

    public Formula getFormula() {
        Reagent[] reagents = new Reagent[] {ResourceHelper.CONTEXT_TARGET, 
                    ResourceHelper.LOCATION_PHRASE, CacheHelper.CACHE, 
                    CacheHelper.CACHE_MODEL, ENTITY_RESOLVER};
        return new SimpleFormula(getClass(), reagents);
    }

    public void init(EntityConfig config) {

        // Instance Members.
        this.resource.init(config);
        this.documentCache = new DynamicCacheHelper<String, Element>(config);
        this.entityResolver = (Phrase) config.getValue(ENTITY_RESOLVER);

    }

    public Object evaluate(TaskRequest req, TaskResponse res) {
        
        URL loc = resource.evaluate(req, res);
        final EntityResolver resolver = (EntityResolver) entityResolver.evaluate(req, res);

        final DocumentFactory documentFactory = new DocumentFactory(resolver);
        return this.documentCache.getCachedObject(req, res, loc.toExternalForm(), documentFactory);
        
    }
    
    protected static final class DocumentFactory extends AbstractCacheHelperFactory<String, Element> {
        private final EntityResolver resolver;
        
        public DocumentFactory(EntityResolver resolver) {
            this.resolver = resolver;
        }

        /* (non-Javadoc)
         * @see org.danann.cernunnos.cache.CacheHelper.Factory#createObject(java.lang.Object)
         */
        public Element createObject(String key) {
            try {
                // Use an EntityResolver if provided...
                SAXReader rdr = new SAXReader();
                if (resolver != null) {
                    rdr.setEntityResolver(resolver);
                }

                // Read by passing a URL -- don't manage the URLConnection yourself...
                final Document document = rdr.read(key);
                final Element rslt = document.getRootElement();
                rslt.normalize();
                return rslt;
            }
            catch (Throwable t) {
                throw new RuntimeException("Unable to read the specified document:  " + key, t);
            }
        }

        /* (non-Javadoc)
         * @see org.danann.cernunnos.CacheHelper.Factory#getMutex(java.lang.Object)
         */
        public Object getMutex(String key) {
            return FACTORY_MUTEX;
        }
    }

}
