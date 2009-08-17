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

package org.danann.cernunnos.runtime.web;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import org.danann.cernunnos.Attributes;
import org.danann.cernunnos.Grammar;
import org.danann.cernunnos.ReturnValueImpl;
import org.danann.cernunnos.Task;
import org.danann.cernunnos.runtime.RuntimeRequestResponse;
import org.danann.cernunnos.runtime.ScriptRunner;
import org.danann.cernunnos.runtime.XmlGrammar;
import org.danann.cernunnos.runtime.web.Settings.Entry;

/**
 * 
 * @author <A href="mailto:argherna@gmail.com>Andy Gherna</A>
 */
public class CernunnosServlet extends HttpServlet {

	// Static Members...
	private static final long serialVersionUID = 1L;
	private static final String CONFIG_LOCATION_PARAM = "contextConfigLocation";
	private static final String CURRENT_VIEW_SESSION_ATTRIBUTE =
				"Lo I the man, whose Muse whilome did maske,\n" +
				"As time her taught in lowly Shepheards weeds,\n" +
				"Am now enforst a far unfitter taske,\n" +
				"For trumpets sterne to chaunge mine Oaten reeds,";

	// Instance Members...
	private ScriptRunner runner = null;
    final Map<URL,Task> tasks = new HashMap<URL,Task>();
	private Settings settings = null;
	private ApplicationContext spring_context = null;
	
	private final Log log = LogFactory.getLog(getClass());	// Don't declare as static in general libraries

    @SuppressWarnings("unchecked")
    @Override
    public void init() throws ServletException {

        ServletConfig config = getServletConfig();

		// Load the context, if present...
		try {
	        
			// Bootstrap the servlet Grammar instance...
        	final Grammar root = XmlGrammar.getMainGrammar();
	        final InputStream inpt = CernunnosServlet.class.getResourceAsStream("servlet.grammar");	// Can't rely on classpath:// protocol handler...
            final Document doc = new SAXReader().read(inpt);
            final Task k = new ScriptRunner(root).compileTask(doc.getRootElement());
    		final RuntimeRequestResponse req = new RuntimeRequestResponse();
    		final ReturnValueImpl rslt = new ReturnValueImpl();
    		req.setAttribute(Attributes.RETURN_VALUE, rslt);
    		k.perform(req, new RuntimeRequestResponse());
    		Grammar g = (Grammar) rslt.getValue();
	        runner = new ScriptRunner(g);

			// Choose a context location...
			String contextConfigLocation = "/WEB-INF/" + config.getServletName() + "-servlet.xml";	// default...
			if (config.getInitParameter(CONFIG_LOCATION_PARAM) != null) {
				contextConfigLocation = config.getInitParameter(CONFIG_LOCATION_PARAM);
			}

			// Load it if it exists...
			URL u = getServletConfig().getServletContext().getResource(contextConfigLocation);
			if (u != null) {
				// There *is* a resource mapped to this path name...
				spring_context = new FileSystemXmlApplicationContext(u.toExternalForm());
			}

			if (log.isDebugEnabled()) {
				log.debug("Location of spring context (null means none):  " + u);
			}

			// Load the Settings...
			Map<String,String> settingsMap = new HashMap<String,String>();	// default...
			if (spring_context != null && spring_context.containsBean("settings")) {
				settingsMap = (Map<String,String>) spring_context.getBean("settings");
			}
			settings = Settings.load(settingsMap);

		} catch (Throwable t) {
			String msg = "Failure in CernunnosServlet.init()";
			throw new ServletException(msg, t);
		}

    }
	
	
	@Override
	public void service(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
	
		// Indicates we'll be executing not one, but (possibly) two 
		// scripts -- one for 'action' and one for 'view' -- in similar 
		// fashion to the CernunnosPortlet.
		boolean mvc = false;

		// Choose an action from 3 possibilities...
		String actionPath = null;
		String method = req.getMethod().toLowerCase();
		if (getInitParameter("scriptLocation") != null || 
				getInitParameter(method + "ScriptLocation") != null) {
			// (1) Script locations specified as init parameters trump all.  You 
			//     can provide either a single 'scriptLocation' param or one for 
			//     each HTTP method you support (e.g. 'postScriptLocation');  
			//     the method-based locations override the single location, so 
			//     you can provide a fall-back location to cover HTTP methods 
			//     you don't implement specifically.
			actionPath = getInitParameter(req.getMethod().toLowerCase() 
					+ "ScriptLocation");
			if (actionPath == null) {
				actionPath = getInitParameter("scriptLocation");
			}
		} else if (req.getPathInfo() != null && req.getPathInfo().split("/").length > 1) {
			// (2) Next in priority are script locations specified on the URL 
			//     preceding the querystring.  Check for the name of a script in 
			//     the PATH_INFO;  use the first component after the first '/' 
			//     character, which (since pathInfo starts with a slash ('/') 
			//     character) is pathInfo.split('/')[1].
			actionPath = req.getPathInfo().split("/")[1];
		} else {
			// (3) We know now we're operating in MVC mode...
			mvc = true;
			// We also need to know if an action is specified for this request...
			actionPath = req.getParameter(settings.getValue(Entry.ACTION_PARAMETER));
		}
		
		// Run an action if one is specified...
		if (actionPath != null && actionPath.length() > 0) {
			String scriptPath = settings.getValue(Settings.Entry.ACTION_PREFIX)
							+ actionPath
							+ settings.getValue(Settings.Entry.ACTION_SUFFIX);
			URL url = getServletConfig().getServletContext().getResource(scriptPath);
			if (url == null) {
			    String msg = "Cannot locate the specified script:  " + scriptPath;
			    throw new RuntimeException(msg);
			}
			runScript(url, req, res);
		}
		
		// Now run a view if we're in MVC mode...
		if (mvc) {
			
			// Choose a view from 4 possibilities...
			String viewPath = null;
			if (req.getParameter(settings.getValue(Settings.Entry.VIEW_PARAMETER)) != null) {
				// (1) A view has been specified on this request...
				viewPath = req.getParameter(settings.getValue(Settings.Entry.VIEW_PARAMETER));

				// We must also move the user to this screen for future renderings...
				req.getSession(true).setAttribute(CURRENT_VIEW_SESSION_ATTRIBUTE, viewPath);
			} else if (req.getSession(true).getAttribute(CURRENT_VIEW_SESSION_ATTRIBUTE) != null) {
				// (2) The user has previously navigated off the default page...
				viewPath = (String) req.getSession(true).getAttribute(CURRENT_VIEW_SESSION_ATTRIBUTE);
	        } else {
				// (4) Use the default page...
	        	viewPath = settings.getValue(Settings.Entry.DEFAULT_VIEW);
			}
			
			// Render the view...
			String scriptPath = settings.getValue(Settings.Entry.VIEW_PREFIX)
								+ viewPath
								+ settings.getValue(Settings.Entry.VIEW_SUFFIX);
			URL url = getServletConfig().getServletContext().getResource(scriptPath);
			runScript(url, req, res);

		}
		
	}
	
	
    private void runScript(URL u, HttpServletRequest req, HttpServletResponse res) throws ServletException {
    	runScript(u, req, res, new RuntimeRequestResponse());
    }

    @SuppressWarnings("unchecked")
	private void runScript(URL u, HttpServletRequest req, HttpServletResponse res, RuntimeRequestResponse rrr) throws ServletException {

		try {
			// Choose the right Task...
			Task k = getTask(u);

			// Basic, guaranteed request attributes...
			rrr.setAttribute(WebAttributes.REQUEST, req);
			rrr.setAttribute(WebAttributes.RESPONSE, res);

	        // Also let's check the request for multi-part form 
	        // data & convert to request attributes if we find any...
	        List<InputStream> streams = new LinkedList<InputStream>();
	        if (ServletFileUpload.isMultipartContent(req)) {
	            
	            log.debug("Miltipart form data detected (preparing to process).");
	            
	            try {
	                final DiskFileItemFactory fac = new DiskFileItemFactory();
	                final ServletFileUpload sfu = new ServletFileUpload(fac);
	                final long maxSize = sfu.getFileSizeMax();  // FixMe!!
	                sfu.setFileSizeMax(maxSize);
	                sfu.setSizeMax(maxSize);
	                fac.setSizeThreshold((int) (maxSize + 1L));
	                List<FileItem> items = sfu.parseRequest(req);
	                for (FileItem f : items) {
	                    if (log.isDebugEnabled()) {
	                        log.debug("Processing file upload:  name='" + f.getName() 
	                                    + "',fieldName='" + f.getFieldName() + "'");
	                    }
	                    InputStream inpt = f.getInputStream();
	                    rrr.setAttribute(f.getFieldName(), inpt);
	                    rrr.setAttribute(f.getFieldName() + "_FileItem", f);
	                    streams.add(inpt);
	                }
	            } catch (Throwable t) {
	                String msg = "Cernunnos servlet failed to process multipart " +
	                                            "form data from the request.";
	                throw new RuntimeException(msg, t);
	            }
	            
	        } else {
	            log.debug("Miltipart form data was not detected.");
	        }

	        // Anything that should be included from the spring_context?
			if (spring_context != null && spring_context.containsBean("requestAttributes")) {
				Map<String,Object> requestAttributes = (Map<String,Object>) spring_context.getBean("requestAttributes");
				for (Map.Entry entry : requestAttributes.entrySet()) {
					rrr.setAttribute((String) entry.getKey(), entry.getValue());
				}
			}
		
			runner.run(k, rrr);

	        // Clean up resources...
	        if (streams.size() > 0) {
	            try {
	                for (InputStream inpt : streams) {
	                    inpt.close();
	                }
	            } catch (Throwable t) {
	                String msg = "Cernunnos servlet failed to release resources.";
	                throw new RuntimeException(msg, t);
	            }
	        }

		} catch(Exception ex) {
			
			// Something went wrong in the Cernunnos script.  
			
			if (log.isFatalEnabled()) {
				log.fatal("An error occurred during the run", ex);
			}
			
			throw new ServletException("An error occurred during the run", ex);
		}

    }

	// ---------------------------------------------------------- Private stuff

	
	private synchronized Task getTask(URL u) {

		// Assertions.
		if (u == null) {
			String msg = "Argument 'u [URL]' cannot be null.";
			throw new IllegalArgumentException(msg);
		}

        Task rslt = null;
        if (tasks.containsKey(u)) {
        	rslt = tasks.get(u);
        } else {

        	rslt = runner.compileTask(u.toString());
        	tasks.put(u, rslt);

        	if (log.isTraceEnabled()) {
            	log.trace("Compiling a Task for the following URL:  " + u.toString());
        	}

        }

        return rslt;

	}
}
