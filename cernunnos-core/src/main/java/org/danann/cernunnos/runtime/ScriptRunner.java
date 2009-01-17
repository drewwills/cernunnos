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

package org.danann.cernunnos.runtime;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.danann.cernunnos.Attributes;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Grammar;
import org.danann.cernunnos.ReturnValueImpl;
import org.danann.cernunnos.Task;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * Simplifies the process of invoking Cernunnos tasks from within Java code.
 * <code>ScriptRunner</code> allows you to run a script in the form of a
 * <code>Task</code>, <code>Element</code>, or location (file system or URL),
 * with or without providing a <code>TaskRequest</code> or <code>Grammar</code>.
 *
 * <p><strong>NOTE:<strong>  when a <code>Task</code> is expected to be used
 * more than once in the lifetime of the application, use
 * <code>compileTask</code> and reuse the same <code>Task</code> object to save
 * resources.
 */
public class ScriptRunner {

    // Instance Members.
    private final Grammar grammar;
    private final Log log;  // Don't declare as static in general libraries

    /*
     * Public API.
     */

    /**
     * Creates a <code>ScriptRunner</code> based on the default
     * <code>Grammar</code>.
     */
    public ScriptRunner() {
        this(XmlGrammar.getMainGrammar());
    }

    /**
     * Creates a <code>ScriptRunner</code> based on the specified
     * <code>Grammar</code>.
     */
    public ScriptRunner(Grammar g) {

        // Assertions.
        if (g == null) {
            String msg = "Argument 'g [Grammar]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.grammar = g;
        this.log = LogFactory.getLog(ScriptRunner.class);

    }

    /**
     * Prepares a <code>Task</code> for (subsequent) execution.
     *
     * @param location Absolute or relative location of a Cernunnos script file.
     */
    public Task compileTask(String location) {

        // Assertions.
        if (location == null) {
            String msg = "Argument 'location' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        Document doc = null;
        URL origin = null;
        try {
            origin = new URL(new File(".").toURL(), location);
            doc = new SAXReader().read(origin);
        } catch (Throwable t) {
            String msg = "Error reading a script from the specified location:  " + location;
            throw new RuntimeException(msg, t);
        }

        return new TaskDecorator(grammar.newTask(doc.getRootElement(), null),
                                                origin.toExternalForm());

    }

    /**
     * Prepares a <code>Task</code> for (subsequent) execution.
     *
     * @param m A pre-parsed Cernunnos script file.
     */
    public Task compileTask(Element m) {

        // Assertions.
        if (m == null) {
            String msg = "Argument 'm [Element]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        return grammar.newTask(m, null);

    }

    /**
     * Invokes the script found at the specified location (file system or URL).
     *
     * @param location A file on the file system or a URL.
     * @return The <code>TaskResponse</code> that results from invoking the
     * specified script.
     */
    public TaskResponse run(String location) {
        return run(location, new RuntimeRequestResponse());
    }

    /**
     * Invokes the script found at the specified location (file system or URL).
     *
     * @param location A file on the file system or a URL.
     * @param req A <code>TaskRequest</code> prepared externally.
     * @return The <code>TaskResponse</code> that results from invoking the
     * specified script.
     */
    public TaskResponse run(String location, TaskRequest req) {

        // Assertions.
        if (location == null) {
            String msg = "Argument 'location' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        return run(compileTask(location), req);

    }

    /**
     * Invokes the script defined by the specified element with the specified
     * <code>TaskRequest</code>.
     *
     * @param m An <code>Element</code> that defines a Task.
     * @param req A <code>TaskRequest</code> prepared externally.
     * @return The <code>TaskResponse</code> that results from invoking the
     * specified script.
     */
    public TaskResponse run(Element m, TaskRequest req) {

        // Assertions.
        if (m == null) {
            String msg = "Argument 'm [Element]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        return run(compileTask(m), req);

    }

    /**
     * Invokes the specified <code>Task</code>.
     *
     * @param k A fully-bootstrapped <code>Task</code> object.
     * @return The <code>TaskResponse</code> that results from invoking the
     * specified task.
     */
    public TaskResponse run(Task k) {
        return run(k, new RuntimeRequestResponse());
    }

    /**
     * Invokes the specified <code>Task</code> with the specified
     * request attributes.  Use this overload of the <code>run</code>
     * method if you need to pre-load information into the
     * <code>TaskRequest</code>.
     *
     * @param k A fully-bootstrapped <code>Task</code> object.
     * @param req A <code>Map</code> of request attributes.
     * @return The <code>TaskResponse</code> that results from invoking the
     * specified task.
     */
    public TaskResponse run(Task k, Map<String,Object> requestAttributes) {
        RuntimeRequestResponse req = new RuntimeRequestResponse();
        for (Map.Entry<String,Object> y : requestAttributes.entrySet()) {
            req.setAttribute(y.getKey(), y.getValue());
        }
        return run(k, req, new RuntimeRequestResponse());
    }

    /**
     * Invokes the specified <code>Task</code> with the specified
     * <code>TaskRequest</code>.  Use this overload of the <code>run</code>
     * method if you need to pre-load information into the
     * <code>TaskRequest</code>.
     *
     * @param k A fully-bootstrapped <code>Task</code> object.
     * @param req A <code>TaskRequest</code> prepared externally.
     * @return The <code>TaskResponse</code> that results from invoking the
     * specified task.
     */
    public TaskResponse run(Task k, TaskRequest req) {
        return run(k, req, new RuntimeRequestResponse());
    }
    
    /**
     * Invokes the specified <code>Task</code> with the specified
     * <code>TaskRequest</code> and <code>TaskResponse</code>.  Use this 
     * overload of the <code>run</code> method when you may need to 
     * pre-load information into both the <code>TaskRequest</code> and 
     * the <code>TaskResponse</code>.
     *
     * @param k A fully-bootstrapped <code>Task</code> object.
     * @param req A <code>TaskRequest</code> prepared externally.
     * @param req A <code>TaskResponse</code> prepared externally.
     * @return The <code>TaskResponse</code> that results from invoking the
     * specified task.
     */
    public TaskResponse run(Task k, TaskRequest req, TaskResponse res) {

        // Assertions.
        if (k == null) {
            String msg = "Argument 'k [Task]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (req == null) {
            String msg = "Argument 'req' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Set up Attributes.ORIGIN if possible...
        RuntimeRequestResponse tr = new RuntimeRequestResponse();
        tr.enclose(req);
        if (k instanceof TaskDecorator) {
            String origin = ((TaskDecorator) k).getOrigin();
            tr.setAttribute(Attributes.ORIGIN, origin);
        }

        // Provide a warning if there's no Attributes.ORIGIN at this point...
        if (!tr.hasAttribute(Attributes.ORIGIN)) {
            log.warn("Request attribute 'Attributes.ORIGIN' is not present.  " +
                    "Cernunnos may not be able to access resources relative " +
                    "to the script.");
        }
        
        // Set up Attributes.CACHE if not already provided...
        if (!tr.hasAttribute(Attributes.CACHE)) {
            tr.setAttribute(Attributes.CACHE, new HashMap<Object, Object>());
        }

        // Write the initial contents of the TaskRequest to the logs...
        if (log.isInfoEnabled()) {
            StringBuffer msg = new StringBuffer();
            msg.append("\n");
            msg.append("**************************************************\n");
            msg.append("** Invoking ScriptRunner.run(Task, TaskRequest)\n");
            msg.append("** TaskRequest contains ").append(tr.getAttributeNames().size()).append(" elements\n");
            for (String name : tr.getSortedAttributeNames()) {
                msg.append("**   - ").append(name).append("=").append(String.valueOf(tr.getAttribute(name))).append("\n");
            }
            msg.append("**************************************************\n");
            log.info(msg.toString());
        }

        // Invoke the task...
        k.perform(tr, res);
        return res;

    }
    
    /**
     * Invokes the script found at the specified location (file system or URL) 
     * and returns the <code>RETURN_VALUE</code>.
     *
     * @param location A file on the file system or a URL.
     * @return The <code>RETURN_VALUE</code> of the specified task.
     */
    public Object evaluate(String location) {
        return evaluate(location, new RuntimeRequestResponse());
    }

    /**
     * Invokes the script found at the specified location (file system or URL) 
     * and returns the <code>RETURN_VALUE</code>.
     *
     * @param location A file on the file system or a URL.
     * @param req A <code>TaskRequest</code> prepared externally.
     * @return The <code>RETURN_VALUE</code> of the specified task.
     */
    public Object evaluate(String location, TaskRequest req) {

        // Assertions.
        if (location == null) {
            String msg = "Argument 'location' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        return evaluate(compileTask(location), req);

    }

    /**
     * Invokes the script defined by the specified element with the specified
     * <code>TaskRequest</code> and returns the <code>RETURN_VALUE</code>.
     *
     * @param m An <code>Element</code> that defines a Task.
     * @param req A <code>TaskRequest</code> prepared externally.
     * @return The <code>RETURN_VALUE</code> of the specified task.
     */
    public Object evaluate(Element m, TaskRequest req) {

        // Assertions.
        if (m == null) {
            String msg = "Argument 'm [Element]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        return evaluate(compileTask(m), req);

    }

    /**
     * Invokes the specified <code>Task</code> and returns the 
     * <code>RETURN_VALUE</code>.
     *
     * @param k A fully-bootstrapped <code>Task</code> object.
     * @return The <code>RETURN_VALUE</code> of the specified task.
     */
    public Object evaluate(Task k) {
        return evaluate(k, new RuntimeRequestResponse());
    }

    /**
     * Invokes the specified <code>Task</code> with the specified
     * <code>TaskRequest</code> and returns the <code>RETURN_VALUE</code>.  
     * Use this overload of the <code>evaluate</code> method if you need to 
     * pre-load information into the <code>TaskRequest</code>.
     *
     * @param k A fully-bootstrapped <code>Task</code> object.
     * @param req A <code>TaskRequest</code> prepared externally.
     * @return The <code>RETURN_VALUE</code> of the specified task.
     */
    public Object evaluate(Task k, TaskRequest req) {
        return evaluate(k, req, new RuntimeRequestResponse());
    }

    /**
     * Invokes the specified <code>Task</code> with the specified
     * <code>TaskRequest</code> and <code>TaskResponse</code> and returns the 
     * <code>RETURN_VALUE</code>.  Use this 
     * overload of the <code>evaluate</code> method when you may need to 
     * pre-load information into both the <code>TaskRequest</code> and 
     * the <code>TaskResponse</code>.
     *
     * @param k A fully-bootstrapped <code>Task</code> object.
     * @param req A <code>TaskRequest</code> prepared externally.
     * @param req A <code>TaskResponse</code> prepared externally.
     * @return The <code>TaskResponse</code> that results from invoking the
     * specified task.
     */
    public Object evaluate(Task k, TaskRequest req, TaskResponse res) {
        
        ReturnValueImpl rslt = new ReturnValueImpl();
        RuntimeRequestResponse tr = new RuntimeRequestResponse();
        tr.enclose(req);
        tr.setAttribute(Attributes.RETURN_VALUE, rslt);
        
        run(k, tr, res);
        return rslt.getValue();

    }

    /*
     * Nested Types.
     */

    private static final class TaskDecorator implements Task {

        // Instance Members.
        private final Task enclosed;
        private final String origin;

        /*
         * Public API.
         */

        public TaskDecorator(Task enclosed, String origin) {

            // Assertions.
            if (enclosed == null) {
                String msg = "Argument 'enclosed' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (origin == null) {
                String msg = "Argument 'origin' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            // Instance Members.
            this.enclosed = enclosed;
            this.origin = origin;

        }

        public Formula getFormula() {
            throw new UnsupportedOperationException();
        }

        public void init(EntityConfig config) {
            throw new UnsupportedOperationException();
        }

        public void perform(TaskRequest req, TaskResponse res) {
            enclosed.perform(req, res);
        }

        public String getOrigin() {
            return origin;
        }

    }

}
