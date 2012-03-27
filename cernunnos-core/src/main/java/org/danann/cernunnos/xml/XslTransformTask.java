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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.danann.cernunnos.AbstractCacheHelperFactory;
import org.danann.cernunnos.AbstractContainerTask;
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
import org.danann.cernunnos.CacheHelper.Factory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.DOMWriter;
import org.dom4j.io.DocumentResult;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.xml.sax.EntityResolver;

/**
 * <code>Task</code> implementation that performs an XSL Transformation over the
 * NODE node or LOCATION document.  Specify the location of the .xsl
 * stylesheet through the (mandatory) STYLESHEET reagent.
 */
public final class XslTransformTask extends AbstractContainerTask {
    public static final String STYLESHEET_LOCAL_CACHE_KEY = XslTransformTask.class.getSimpleName() + ".STYLESHEET_LOCAL";

	// Instance Members.
    private final Factory<Tuple<String, String>, Templates> transformerFactory = new CachedTransformerFactory();
	private CacheHelper<Tuple<String, String>, Templates> transformerCache;
	private Phrase entityResolver;
	private Phrase context;
	private Phrase stylesheet;
	private Phrase node;
	private Phrase location;
	private Phrase to_file;

	/*
	 * Public API.
	 */

	public static final Reagent ENTITY_RESOLVER = new SimpleReagent("ENTITY_RESOLVER", "@entity-resolver", ReagentType.PHRASE,
					EntityResolver.class, "Optional org.xml.sax.EntityResolver to use in parsing LOCATION.  By default, " +
					"this task looks for an EntityResolver instance under the request attribute 'XmlAttributes.ENTITY_RESOLVER' " +
					"and will use it if present.", new AttributePhrase(XmlAttributes.ENTITY_RESOLVER, new LiteralPhrase(null)));

	public static final Reagent CONTEXT = new SimpleReagent("CONTEXT", "@context", ReagentType.PHRASE, String.class,
					"The context from which missing elements of the STYLESHEET and LOCATION may be inferred in "
					+ "appropriate circumstances.  The default is a URL representing the filesystem location "
					+ "from which Java is executing.", new CurrentDirectoryUrlPhrase());

	public static final Reagent STYLESHEET = new SimpleReagent("STYLESHEET", "@stylesheet", ReagentType.PHRASE, String.class,
					"Location of the XSLT stylesheet to use in transformation.  May be a file system path (absolute "
					+ "or relative) or a URL.");

	public static final Reagent NODE = new SimpleReagent("NODE", "@node", ReagentType.PHRASE, Element.class,
					"Optional XML node to act as the source of the transformation.  If not explicitly "
					+ "specified, this task will attempt to use the 'Attributes.NODE' request attribute.  "
					+ "If that attribute is not present, the LOCATION reagent will be used.",
					new AttributePhrase(Attributes.NODE, new LiteralPhrase(null)));

	public static final Reagent LOCATION = new SimpleReagent("LOCATION", "@location", ReagentType.PHRASE, String.class,
					"Optional location of an XML resource that will be transformed (assuming the NODE reagent is not "
					+ "provided).  It may be a filesystem path or a URL, and may be absolute or relative.  If "
					+ "relative, the location will be evaluated from the CONTEXT.  If omitted, the value of the "
					+ "'Attributes.LOCATION' request attribute will be used.",
					new AttributePhrase(Attributes.LOCATION));

	public static final Reagent TO_FILE = new SimpleReagent("TO_FILE", "@to-file", ReagentType.PHRASE, String.class,
					"Optional file system path to which the result of the transformation will be written.  It may "
					+ "be absolute or relative, in which case it will be evaluated from the directory in which "
					+ "Java is executing.  If not provided, the result will be set to the value of "
					+ "'Attributes.NODE' on the task request for subtasks.", null);

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {CacheHelper.CACHE, CacheHelper.CACHE_MODEL, ENTITY_RESOLVER, CONTEXT, STYLESHEET,
					NODE, LOCATION, TO_FILE, AbstractContainerTask.SUBTASKS};
		final Formula rslt = new SimpleFormula(XslTransformTask.class, reagents);
		return rslt;
	}

	@Override
    public void init(EntityConfig config) {
		super.init(config);

		// Instance Members.
		this.transformerCache = new DynamicCacheHelper<Tuple<String, String>, Templates>(config);
		this.entityResolver = (Phrase) config.getValue(ENTITY_RESOLVER);
		this.context = (Phrase) config.getValue(CONTEXT);
		this.stylesheet = (Phrase) config.getValue(STYLESHEET);
		this.node = (Phrase) config.getValue(NODE);
		this.location = (Phrase) config.getValue(LOCATION);
		this.to_file = (Phrase) config.getValue(TO_FILE);

	}

	public void perform(TaskRequest req, TaskResponse res) {
        final String contextLocation = (String) context.evaluate(req, res);
        final String stylesheetLocation = (String) stylesheet.evaluate(req, res);
        final Tuple<String, String> transformerKey = new Tuple<String, String>(contextLocation, stylesheetLocation);
        final Templates templates = this.transformerCache.getCachedObject(req, res, transformerKey, this.transformerFactory);

		Element srcElement = null;
		Node nodeReagentEvaluated = node != null ? (Node) node.evaluate(req, res) : null;
		if (nodeReagentEvaluated != null) {
			// Reading from the NODE reagent is preferred...
			srcElement = (Element) nodeReagentEvaluated;
		} else {
			// But read from LOCATION if NODE isn't set...
		    final String locationStr = (String) location.evaluate(req, res);
            final URL loc;
                try {
                    final URL ctx;
                    try {
                        ctx = new URL(contextLocation);
                    }
                    catch (MalformedURLException mue) {
                        throw new RuntimeException("Failed to parse context '" + contextLocation + "' into URL", mue);
                }
                
                loc = new URL(ctx, locationStr);
            }
            catch (MalformedURLException mue) {
                throw new RuntimeException("Failed to parse location '" + locationStr + "' with context '" + contextLocation + "' into URL", mue);
            }

			// Use an EntityResolver if provided...
			SAXReader rdr = new SAXReader();
			EntityResolver resolver = (EntityResolver) entityResolver.evaluate(req, res);
			if (resolver != null) {
				rdr.setEntityResolver(resolver);
			}

			final Document document;
            try {
                document = rdr.read(loc);
            }
            catch (DocumentException de) {
                throw new RuntimeException("Failed to read XML Document for XSLT from " + loc.toExternalForm(), de);
            }
            srcElement = document.getRootElement();
		}

		DocumentFactory dfac = new DocumentFactory();
		Document ddoc = dfac.createDocument((Element) srcElement.clone());
		DOMWriter dwriter = new DOMWriter();

		DocumentResult rslt = new DocumentResult();
		
		final Transformer trans;
        try {
            trans = templates.newTransformer();
        }
        catch (TransformerConfigurationException tce) {
            throw new RuntimeException("Failed to retrieve Transformer for XSLT", tce);
        }
        
		try {
            trans.transform(new DOMSource(dwriter.write(ddoc)), rslt);
        }
        catch (TransformerException te) {
            throw new RuntimeException("Failed to perform XSL transformation", te);
        }
        catch (DocumentException de) {
            throw new RuntimeException("Failed to translate JDOM Document to W3C Document", de);
        }

		final Element rootElement = rslt.getDocument().getRootElement();
		
        if (to_file != null) {
			File f = new File((String) to_file.evaluate(req, res));
			if (f.getParentFile() != null) {
				// Make sure the necessary directories are in place...
				f.getParentFile().mkdirs();
			}
			
			final XMLWriter writer;
            try {
                writer = new XMLWriter(new FileOutputStream(f),
                						new OutputFormat("  ", true));
            }
            catch (UnsupportedEncodingException uee) {
                throw new RuntimeException("Failed to create XML writer", uee);
            }
            catch (FileNotFoundException fnfe) {
                throw new RuntimeException("Could not create file for XML output: " + f, fnfe);
            }
            
			try {
                writer.write(rootElement);
            }
            catch (IOException ioe) {
                throw new RuntimeException("Failed to write transformed XML document to: " + f, ioe);
            }
		} else {
			// default behavior...
			res.setAttribute(Attributes.NODE, rootElement);
		}

		super.performSubtasks(req, res);

	}

    /**
     * Factory to create new Transformer instances
     */
    protected static class CachedTransformerFactory extends AbstractCacheHelperFactory<Tuple<String, String>, Templates> {
        private final CachedTransformerFactoryMutex MUTEX = new CachedTransformerFactoryMutex();
        private final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        
        /* (non-Javadoc)
         * @see org.danann.cernunnos.cache.CacheHelper.Factory#createObject(java.lang.Object)
         */
        public Templates createObject(Tuple<String, String> key) {
            final URL xslUrl;
            try {
                final URL contextUrl = new URL(key.first);
                xslUrl = new URL(contextUrl, key.second);
            }
            catch (MalformedURLException mue) {
                throw new RuntimeException("Failed to create URL to XSL from scriptEngine='" + key.first + "' and script='" + key.second + "'", mue);
            }
            
            try {
                return this.transformerFactory.newTemplates(new StreamSource(xslUrl.toExternalForm()));
            }
            catch (TransformerConfigurationException tce) {
                throw new RuntimeException("Failed to create Transformer for XSL='" + xslUrl.toExternalForm() + "'", tce);
            }
        }

        /* (non-Javadoc)
         * @see org.danann.cernunnos.cache.CacheHelper.Factory#isThreadSafe(java.lang.Object, java.lang.Object)
         */
        @Override
        public boolean isThreadSafe(Tuple<String, String> key, Templates instance) {
            return true;
        }

        /* (non-Javadoc)
         * @see org.danann.cernunnos.CacheHelper.Factory#getMutex(java.lang.Object)
         */
        public Object getMutex(Tuple<String, String> key) {
            return MUTEX;
        }
        
        private static class CachedTransformerFactoryMutex {
        }
    }
}