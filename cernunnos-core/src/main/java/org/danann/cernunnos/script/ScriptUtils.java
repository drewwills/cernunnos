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

package org.danann.cernunnos.script;

import java.util.List;
import java.util.Map;

import javax.script.Bindings;
import javax.script.SimpleBindings;

import org.danann.cernunnos.Attributes;
import org.danann.cernunnos.BindingsHelper;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

/**
 * Utilities related the the script task and phrase.
 *
 * @author Eric Dalquist
 * @version $Revision$
 */
public class ScriptUtils {
    public static final Bindings generateBindings(TaskRequest req, TaskResponse res) {
        final Bindings bindings = new SimpleBindings();

        // Bind simple things (non-Attributes)...
        for (final Map.Entry<String, Object> attrEntry : req.getAttributes().entrySet()) {
            final String attrKey = attrEntry.getKey();
            if (attrKey.indexOf(".") == -1) {
                bindings.put(attrKey, attrEntry.getValue());
            }
        }
        
        // Bind Attributes based on BindingsHelper objects...
        final List<BindingsHelper> helpers = Attributes.prepareBindings(new TaskRequestDecorator(req, res));
        for (final BindingsHelper bindingsHelper : helpers) {
            bindings.put(bindingsHelper.getBindingName(), bindingsHelper);
        }
        
        return bindings;
    }
}
