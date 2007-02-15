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
import java.io.FileOutputStream;
import java.net.URL;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.DocumentResult;
import org.dom4j.io.DOMWriter;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import org.danann.cernunnos.AbstractContainerTask;
import org.danann.cernunnos.AttributePhrase;
import org.danann.cernunnos.Attributes;
import org.danann.cernunnos.CurrentDirectoryUrlPhrase;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

/**
 * <code>Task</code> implementation that performs an XSL Transformation over the 
 * SOURCE node or LOCATION document.  Specify the location of the .xsl 
 * stylesheet through the (mandatory) STYLESHEET reagent.  
 */
public class XslTransformTask extends AbstractContainerTask {

	// Instance Members.
	private TransformerFactory fac;
	private Phrase context;
	private Phrase stylesheet;
	private Phrase node;
	private Phrase location;
	private Phrase to_file;
	
	/*
	 * Public API.
	 */

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
					new AttributePhrase(Attributes.NODE, null));

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
		Reagent[] reagents = new Reagent[] {CONTEXT, STYLESHEET, NODE, LOCATION, 
									TO_FILE, AbstractContainerTask.SUBTASKS};
		final Formula rslt = new SimpleFormula(XslTransformTask.class, reagents);
		return rslt;
	}

	public void init(EntityConfig config) {

		super.init(config);

		// Instance Members.
		this.fac = TransformerFactory.newInstance();
		this.context = (Phrase) config.getValue(CONTEXT); 
		this.stylesheet = (Phrase) config.getValue(STYLESHEET); 
		this.node = (Phrase) config.getValue(NODE);
		this.location = (Phrase) config.getValue(LOCATION);
		this.to_file = (Phrase) config.getValue(TO_FILE); 
		
	}

	public void perform(TaskRequest req, TaskResponse res) {
		
		try {
			
			URL ctx = new URL((String) context.evaluate(req, res));
			URL transUrl = new URL(ctx, (String) stylesheet.evaluate(req, res));
			Transformer trans = fac.newTransformer(new StreamSource(transUrl.openStream()));

			Element srcElement = null;
			if (node != null) {
				// Reading from the NODE reagent is preferred...
				srcElement = (Element) node.evaluate(req, res);
			} else {
				// But read from LOCATION if NODE isn't set...
				URL loc = new URL(ctx, (String) location.evaluate(req, res));
				srcElement = new SAXReader().read(loc).getRootElement();
			}
			
			DocumentFactory dfac = new DocumentFactory();
			Document ddoc = dfac.createDocument((Element) srcElement.clone());
			DOMWriter dwriter = new DOMWriter();			
			
			DocumentResult rslt = new DocumentResult();
			trans.transform(new DOMSource(dwriter.write(ddoc)), rslt);

			if (to_file != null) {
				File f = new File((String) to_file.evaluate(req, res));
				f.getParentFile().mkdirs();
				XMLWriter writer = new XMLWriter(new FileOutputStream(f), 
										new OutputFormat("  ", true));
				writer.write(rslt.getDocument().getRootElement());
			} else {
				// default behavior...
				res.setAttribute(Attributes.NODE, rslt.getDocument().getRootElement());
			}

		} catch (Throwable t) {
			String msg = "Unable to perform the requested transformation.";
			throw new RuntimeException(msg, t);
		}
		
		super.performSubtasks(req, res);
		
	}

}