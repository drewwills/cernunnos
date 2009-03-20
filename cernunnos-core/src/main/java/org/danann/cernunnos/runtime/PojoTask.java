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

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

import org.danann.cernunnos.Attributes;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.ResourceHelper;
import org.danann.cernunnos.ReturnValueImpl;
import org.danann.cernunnos.Task;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

/**
 * Task implementation that's easy to use with dependency-injection strategies 
 * like the Spring Framework.
 */
public class PojoTask implements Task, InitializingBean {

    // Static Members.
    private static final String DEFAULT_CONTEXT = PojoTask.class.getResource("PojoTask.class").toExternalForm();

    // Instance Members.
    private String context = DEFAULT_CONTEXT;
    private String location;
    private Map<String,Object> requestAttributes = Collections.emptyMap();
    private Task task;
    private final ScriptRunner runner = new ScriptRunner();
    private final Log log = LogFactory.getLog(getClass());  // Don't declare as static in general libraries
    
    /*
     * Public API.
     */

    /**
     * Sets the <code>context</code> URL from which a relative 
     * <code>location</code> URL should be evaluated.  If <code>location</code> 
     * is not relative then <code>context</code> has no effect.  The default is 
     * the location of the <code>PojoTask.class</code> file.  This method may be 
     * called at most once.
     * 
     * @param context A URL in <code>String</code> form
     */
    public void setContext(final String context) {
        
        // Assertions.
        if (context == null) {
            String msg = "Argument 'context' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (this.context != null) {
            String msg = "Property 'context' may only be set once.";
            throw new RuntimeException(msg);
        }
        
        this.context = context;

    }
    
    /**
     * Sets the <code>location</code> URL from which Cernunnos XML will be read 
     * to create this task.  A relative <code>location</code> will be evaluated 
     * from the <code>context</code> URL.  This method must be called exactly 
     * once.
     * 
     * @param location Absolute or relative location of the Cernunnos XML for 
     * this task
     */
    @Required
    public void setLocation(final String location) {

        // Assertions.
        if (location == null) {
            String msg = "Argument 'location' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (this.location != null) {
            String msg = "Property 'location' may only be set once.";
            throw new RuntimeException(msg);
        }

        this.location = location;

    }

    public void setRequestAttributes(final Map<String,Object> requestAttributes) {
        this.requestAttributes = new HashMap<String,Object>(requestAttributes);
    }
    
    /**
     * This method <em>must</em> be invoked after all POJO properties have been 
     * supplied.  If this <code>Task</code> has been defined using Spring 
     * Dependency Injection, this method will be called for you by the Spring 
     * context.  Additional calls to this method are no-ops.
     */
    public synchronized void afterPropertiesSet() {

        // Subsequent calls are no-ops...
        if (task == null) {
            
            // Be sure we have what we need...
            if (location == null) {
                String msg = "Property 'location' not set.  You must specify " +
                                                "a Cernunnos XML document.";
                throw new IllegalStateException(msg);
            }

            if (log.isDebugEnabled()) {
                log.debug("Bootstrapping Cernunnos XML [context=" 
                                + context + " location=" + location);
            }
            
            try {
                URL u = ResourceHelper.evaluate(context, location);
                task = runner.compileTask(u.toExternalForm());
            } catch (Throwable t) {
                String msg = "Unable to read the specified Cernunnos XML"
                                    + "\n\t\tcontext=" + context
                                    + "\n\t\tlocation=" + location;
                throw new RuntimeException(msg, t);
            }

        }

    }

    public Formula getFormula() {
        throw new UnsupportedOperationException();
    }

    public void init(EntityConfig config) {
        throw new UnsupportedOperationException();
    }
    
    public void perform(TaskRequest req, TaskResponse res) {
        
        // Assertions...
        if (task == null) {
            String msg = "Task not initialized;  you must invoke " +
            		    "PojoTask.afterPropertiesSet() before using this Task.";
            throw new IllegalStateException(msg);
        }
                
        RuntimeRequestResponse tr = new RuntimeRequestResponse();
        tr.enclose(req);
        for (Map.Entry<String,Object> y : requestAttributes.entrySet()) {
            tr.setAttribute(y.getKey(), y.getValue());
        }
        runner.run(task, tr, res);
        
    }
    
    public Object evaluate() {
        
        ReturnValueImpl rslt = new ReturnValueImpl();
        RuntimeRequestResponse tr = new RuntimeRequestResponse();
        tr.setAttribute(Attributes.RETURN_VALUE, rslt);
        
        perform(tr, new RuntimeRequestResponse());
        return rslt.getValue();

    }
    
}
