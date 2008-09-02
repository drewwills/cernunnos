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

import org.danann.cernunnos.TaskRequest;
import org.jaxen.UnresolvableException;
import org.jaxen.VariableContext;

/**
 * Provides ability to use Cernunnos request attributes as variables in Jaxen XPath expressions
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class RequestVariableContext implements VariableContext {
    private final TaskRequest request;
    
    public RequestVariableContext(TaskRequest request) {
        this.request = request;
    }

    /* (non-Javadoc)
     * @see org.jaxen.VariableContext#getVariableValue(java.lang.String, java.lang.String, java.lang.String)
     */
    public Object getVariableValue(String namespaceURI, String prefix, String localName) throws UnresolvableException {
        if (this.request.hasAttribute(localName)) {
            return String.valueOf(this.request.getAttribute(localName));
        }
        
        return null;
    }

}
