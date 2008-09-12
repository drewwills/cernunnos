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

package org.danann.cernunnos.runtime.web;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.portlet.PortletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.danann.cernunnos.Attributes;
import org.danann.cernunnos.Grammar;
import org.danann.cernunnos.ReturnValueImpl;
import org.danann.cernunnos.Task;
import org.danann.cernunnos.runtime.RuntimeRequestResponse;
import org.danann.cernunnos.runtime.ScriptRunner;
import org.danann.cernunnos.runtime.XmlGrammar;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class CernunnosPortlet extends GenericPortlet {

    // Static Members...
    private static final String CONFIG_LOCATION_PARAM = "contextConfigLocation";

    // Instance Members...
    private ScriptRunner runner = null;
    final Map<URL,Task> tasks = new HashMap<URL,Task>();
    private Settings settings = null;
    private ApplicationContext spring_context = null;
    private final Log log = LogFactory.getLog(CernunnosPortlet.class);  // Don't declare as static in general libraries

    /*
     * Public API.
     */

    /**
     * Use this constant to get a reference to the calling portlet instance from
     * the <code>PortletRequest</code> in your JSP.  Thereafter, you'll be able
     * to leverage the context settings by invoking <code>runScript</code>.
     */
    public static final String PORTLET_REQUEST_PARAM = "CernunnosPortlet.PORTLET_REQUEST_PARAM";

    @SuppressWarnings("unchecked")
    public void init() throws PortletException {

        PortletConfig config = getPortletConfig();

        // Load the context, if present...
        try {
            
            // Bootstrap the portlet Grammar instance...
            final Grammar root = XmlGrammar.getMainGrammar();
            final InputStream inpt = CernunnosPortlet.class.getResourceAsStream("portlet.grammar"); // Can't rely on classpath:// protocol handler...
            final Document doc = new SAXReader().read(inpt);
            final Task k = new ScriptRunner(root).compileTask(doc.getRootElement());
            final RuntimeRequestResponse req = new RuntimeRequestResponse();
            final ReturnValueImpl rslt = new ReturnValueImpl();
            req.setAttribute(Attributes.RETURN_VALUE, rslt);
            k.perform(req, new RuntimeRequestResponse());
            Grammar g = (Grammar) rslt.getValue();
            runner = new ScriptRunner(g);

            // Choose a context location...
            String contextConfigLocation = "/WEB-INF/" + config.getPortletName() + "-portlet.xml";  // default...
            if (config.getInitParameter(CONFIG_LOCATION_PARAM) != null) {
                contextConfigLocation = config.getInitParameter(CONFIG_LOCATION_PARAM);
            }

            // Load it if it exists...
            URL u = getPortletConfig().getPortletContext().getResource(contextConfigLocation);
            if (u != null) {
                // There *is* a resource mapped to this path name...
                spring_context = new FileSystemXmlApplicationContext(u.toExternalForm());
            }

            if (log.isTraceEnabled()) {
                log.trace("Loaction of spring context (null means none):  " + u);
            }

            // Load the Settings...
            Map<String,String> settingsMap = new HashMap<String,String>();  // default...
            if (spring_context != null && spring_context.containsBean("settings")) {
                settingsMap = (Map<String,String>) spring_context.getBean("settings");
            }
            settings = Settings.load(settingsMap);

        } catch (Throwable t) {
            String msg = "Failure in CernunnosPortlet.init()";
            throw new PortletException(msg, t);
        }

    }

    public void processAction(ActionRequest req, ActionResponse res) throws PortletException {

        // If a 'view' parameter is provided, we need to forward it to the doView() method appropriately...
        if (req.getParameter(settings.getValue(Settings.Entry.VIEW_PARAMETER)) != null) {
            String view = req.getParameter(settings.getValue(Settings.Entry.VIEW_PARAMETER));
            res.setRenderParameter(settings.getValue(Settings.Entry.VIEW_PARAMETER), view);
        }
        
        // Perform specified action(s)...
        String[] actions = req.getParameterValues(settings.getValue(Settings.Entry.ACTION_PARAMETER));
        if (actions != null && actions.length > 0) {

            // Invoke each action...
            String scriptPath = null;
            try {

                for (String act : actions) {
                    if (act != null && act.length() > 0) {
                        scriptPath = settings.getValue(Settings.Entry.ACTION_PREFIX)
                                        + act
                                        + settings.getValue(Settings.Entry.ACTION_SUFFIX);
                        URL url = getPortletConfig().getPortletContext().getResource(scriptPath);
                        runScript(url, req, res);
                    }
                }

            } catch (Throwable t) {
                String msg = "CernunnosPortlet.processAction() failed to run the specified script:  " + scriptPath;
                throw new PortletException(msg, t);
            }

        }

    }

    public void doView(RenderRequest req, RenderResponse res) throws PortletException {

        // Choose a view from 4 possibilities...
        String view = null;
        if (req.getParameter(settings.getValue(Settings.Entry.VIEW_PARAMETER)) != null) {
            // (1) A view has been specified on this request...
            view = req.getParameter(settings.getValue(Settings.Entry.VIEW_PARAMETER));

            // We must also move the user to this screen for future renderings...
            req.getPortletSession(true).setAttribute(CURRENT_VIEW_SESSION_ATTRIBUTE, view);
        } else if (req.getPortletSession(true).getAttribute(CURRENT_VIEW_SESSION_ATTRIBUTE) != null) {
            // (2) The user has previously navigated off the default page...
            view = (String) req.getPortletSession(true).getAttribute(CURRENT_VIEW_SESSION_ATTRIBUTE);
        } else if (req.getPreferences().getValue(settings.getValue(Settings.Entry.VIEW_PARAMETER), "").length() > 0) {
            // (3) The view preference was set, maybe at publish time, maybe by an
            // admin or maybe by the user.
            view = req.getPreferences().getValue(settings.getValue(Settings.Entry.VIEW_PARAMETER), "");
        } else {
            // (4) Use the default page...
            view = settings.getValue(Settings.Entry.DEFAULT_VIEW);
        }

        // Render...
        try {
            res.setContentType(req.getResponseContentType());
            String viewPath = settings.getValue(Settings.Entry.VIEW_PREFIX)
                                + view
                                + settings.getValue(Settings.Entry.VIEW_SUFFIX);
            URL url = getPortletConfig().getPortletContext().getResource(viewPath);
            runScript(url, req, res);
        } catch (Throwable t) {
            String msg = "Rendering failure in CernunnosPortlet.doView()";
            throw new PortletException(msg, t);
        }

    }

    private void runScript(URL u, PortletRequest req, PortletResponse res) {
        runScript(u, req, res, new RuntimeRequestResponse());
    }

    @SuppressWarnings("unchecked")
    private void runScript(URL u, PortletRequest req, PortletResponse res, RuntimeRequestResponse rrr) {

        // Choose the right Task...
        Task k = getTask(u);

        // Basic, guaranteed request attributes...
        rrr.setAttribute(WebAttributes.REQUEST, req);
        rrr.setAttribute(WebAttributes.RESPONSE, res);
        
        // Also let's check the request for multi-part form 
        // data & convert to request attributes if we find any...
        List<InputStream> streams = new LinkedList<InputStream>();
        if (req instanceof ActionRequest && PortletFileUpload.isMultipartContent((ActionRequest) req)) {
            
            log.debug("Miltipart form data detected (preparing to process).");
            
            try {
                final DiskFileItemFactory fac = new DiskFileItemFactory();
                final PortletFileUpload pfu = new PortletFileUpload(fac);
                final long maxSize = pfu.getFileSizeMax();  // FixMe!!
                pfu.setFileSizeMax(maxSize);
                pfu.setSizeMax(maxSize);
                fac.setSizeThreshold((int) (maxSize + 1L));
                List<FileItem> items = pfu.parseRequest((ActionRequest) req);
                for (FileItem f : items) {
                    if (log.isDebugEnabled()) {
                        log.debug("Processing file upload:  name='" + f.getName() 
                                    + "',fieldName='" + f.getFieldName() + "'");
                    }
                    InputStream inpt = f.getInputStream();
                    rrr.setAttribute(f.getFieldName(), inpt);
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

    }

    public void doEdit(RenderRequest req, RenderResponse res) throws PortletException {
        // This is probably not a permanent approach, but atm all we need is...
        doView(req, res);
    }

    /*
     * Private Stuff.
     */

    private static final String CURRENT_VIEW_SESSION_ATTRIBUTE =
                        "Death closes all: but something ere the end,\n" +
                        "Some work of noble note, may yet be done,\n" +
                        "Not unbecoming men that strove with Gods.";

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
