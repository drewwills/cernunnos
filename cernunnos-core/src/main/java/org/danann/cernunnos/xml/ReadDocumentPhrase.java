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

package org.danann.cernunnos.xml;

import java.net.URL;

import org.danann.cernunnos.AbstractCacheHelperFactory;
import org.danann.cernunnos.AttributePhrase;
import org.danann.cernunnos.Attributes;
import org.danann.cernunnos.CacheHelper;
import org.danann.cernunnos.CurrentDirectoryUrlPhrase;
import org.danann.cernunnos.DynamicCacheHelper;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.LiteralPhrase;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;
import org.danann.cernunnos.Tuple;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.EntityResolver;

public final class ReadDocumentPhrase implements Phrase {
	// Instance Members.
    private CacheHelper<Tuple<String, String>, Element> documentCache;
    private Phrase entityResolver;
	private Phrase context;
	private Phrase location;

	/*
	 * Public API.
	 */

	public static final Reagent ENTITY_RESOLVER = new SimpleReagent("ENTITY_RESOLVER", "@entity-resolver", ReagentType.PHRASE,
					EntityResolver.class, "Optional org.xml.sax.EntityResolver to use in document parsing.  By default, " +
					"this phrase looks for an EntityResolver instance under the request attribute 'XmlAttributes.ENTITY_RESOLVER' " +
					"and will use it if present.", new AttributePhrase(XmlAttributes.ENTITY_RESOLVER, new LiteralPhrase(null)));

	public static final Reagent CONTEXT = new SimpleReagent("CONTEXT", "@context", ReagentType.PHRASE, String.class,
					"The context from which missing elements of the LOCATION can be inferred if it "
					+ "is relative.  The default is a URL representing the filesystem location from which "
						+ "Java is executing.", new CurrentDirectoryUrlPhrase());

	public static final Reagent LOCATION = new SimpleReagent("LOCATION", "descendant-or-self::text()", ReagentType.PHRASE, String.class,
					"Location of a resource from which a document may be read.  May be a filesystem path (absolute or relative), "
					+ "or a URL.  If relative, the location will be evaluated from the CONTEXT.  If omitted, the value of the "
					+ "'Attributes.LOCATION' request attribute will be used.", new AttributePhrase(Attributes.LOCATION));

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {CacheHelper.CACHE, CacheHelper.CACHE_MODEL, ENTITY_RESOLVER, CONTEXT, LOCATION};
		return new SimpleFormula(ReadDocumentPhrase.class, reagents);
	}

	public void init(EntityConfig config) {

		// Instance Members.
        this.documentCache = new DynamicCacheHelper<Tuple<String, String>, Element>(config);
		this.entityResolver = (Phrase) config.getValue(ENTITY_RESOLVER);
		this.context = (Phrase) config.getValue(CONTEXT);
		this.location = (Phrase) config.getValue(LOCATION);

	}

	public Object evaluate(TaskRequest req, TaskResponse res) {
		final String contextLocation = (String) context.evaluate(req, res);
		final String documentLocation = (String) location.evaluate(req, res);
        final EntityResolver resolver = (EntityResolver) entityResolver.evaluate(req, res);

        final Tuple<String, String> documentKey = new Tuple<String, String>(contextLocation, documentLocation);
        final DocumentFactory documentFactory = new DocumentFactory(resolver);
        return this.documentCache.getCachedObject(req, res, documentKey, documentFactory);
	}
	
    
    /**
     * Loads a DOM4J document from the specified contact and location and returns the root Element
     */
    protected Element loadDocument(String ctx_str, String loc_str, EntityResolver resolver) {
        try {
            final URL ctx = new URL(ctx_str);
            final URL doc = new URL(ctx, loc_str);
            
            // Use an EntityResolver if provided...
            final SAXReader rdr = new SAXReader();
            if (resolver != null) {
                rdr.setEntityResolver(resolver);
            }

            // Read by passing a URL -- don't manage the URLConnection yourself...
            final Element rslt = rdr.read(doc).getRootElement();
            rslt.normalize();
            return rslt;

        } catch (Throwable t) {
            String msg = "Unable to read the specified document:"
                        + "\n\tCONTEXT=" + ctx_str
                        + "\n\tLOCATION=" + loc_str;
            throw new RuntimeException(msg, t);
        }
    }
    
    protected static final class DocumentFactory extends AbstractCacheHelperFactory<Tuple<String, String>, Element> {
        private final EntityResolver resolver;
        
        public DocumentFactory(EntityResolver resolver) {
            this.resolver = resolver;
        }

        /* (non-Javadoc)
         * @see org.danann.cernunnos.cache.CacheHelper.Factory#createObject(java.lang.Object)
         */
        public Element createObject(Tuple<String, String> key) {
            try {
                URL ctx = new URL(key.first);
                URL doc = new URL(ctx, key.second);

                // Use an EntityResolver if provided...
                SAXReader rdr = new SAXReader();
                if (resolver != null) {
                    rdr.setEntityResolver(resolver);
                }

                // Read by passing a URL -- don't manage the URLConnection yourself...
                final Document document = rdr.read(doc);
                final Element rslt = document.getRootElement();
                rslt.normalize();
                return rslt;
            }
            catch (Throwable t) {
                throw new RuntimeException("Unable to read the specified document:" + 
                        "\n\tCONTEXT=" + key.first + 
                        "\n\tLOCATION=" + key.second, t);
            }
        }

        /* (non-Javadoc)
         * @see org.danann.cernunnos.CacheHelper.Factory#getMutex(java.lang.Object)
         */
        public Object getMutex(Tuple<String, String> key) {
            return key;
        }
    }
}