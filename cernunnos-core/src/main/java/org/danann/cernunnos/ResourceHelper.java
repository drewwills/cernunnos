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

package org.danann.cernunnos;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

/**
 * Simplifies the process of identifying a resource by <code>CONTEXT</code> and 
 * <code>LOCATION</code>, and provides support for important features like 
 * custom <code>URLStreamHandler</code> implementations.
 */
public final class ResourceHelper {
    
    // Static Members.
    private static final List<ProtocolTranslator> translators = new LinkedList<ProtocolTranslator>();
    static {
        translators.add(
                new ProtocolTranslator("\\Aclasspath:/.*") {
                    @Override
                    public URL translate(String spec) {
                        
                        // Assertions.
                        if (spec == null) {
                            String msg = "Argument 'spec' cannot be null.";
                            throw new IllegalArgumentException(msg);
                        }
                        
                        // Replace twice b/c both 'classpath:/' 
                        // and 'classpath://' hare supported...
                        String s = spec.replaceAll("\\Aclasspath://", "/")
                                        .replaceAll("\\Aclasspath:/", "/");
                        return this.getClass().getResource(s);

                    }
                });
        translators.add(
                new ProtocolTranslator("\\A[a-zA-Z]:/.*") {
                    @Override
                    public URL translate(String spec) {
                        
                        // Assertions.
                        if (spec == null) {
                            String msg = "Argument 'spec' cannot be null.";
                            throw new IllegalArgumentException(msg);
                        }
                        
                        URL rslt = null;
                        try {
                            rslt = new URL("file:///" + spec);
                        } catch (Throwable t) {
                            String msg = "Unable to read the specified file:  " + spec;
                            throw new RuntimeException(msg, t);
                        }                        
                        return rslt;

                    }
                });
    }
    
    // Instance Members.
    private Phrase contextPhrase;
    private Phrase locationPhrase;

    /*
     * Public API.
     */
    
    public static final Reagent CONTEXT_SOURCE = new SimpleReagent("CONTEXT", "@context", ReagentType.PHRASE, String.class,
            "Optional context from which missing elements of the LOCATION will be inferred if it is relative.  " +
            "The default is the value of the 'Attributes.ORIGIN' request attribute.", 
            new AttributePhrase(Attributes.ORIGIN));

    public static final Reagent CONTEXT_TARGET = new SimpleReagent("CONTEXT", "@context", ReagentType.PHRASE, String.class,
            "Optional context from which missing elements of the LOCATION will be inferred if it is relative.  " +
            "The default is the value of the 'Attributes.CONTEXT' request attribute (if present) or the directory " +
            "from which Java is executing (otherwise).", 
            new AttributePhrase(Attributes.CONTEXT, new CurrentDirectoryUrlPhrase()));

    public static final Reagent LOCATION_TASK = new SimpleReagent("LOCATION", "@location", ReagentType.PHRASE, String.class,
            "Optional location of a resource;  if omitted, the value of the 'Attributes.LOCATION' request attribute " +
            "will be used.  LOCATION may be a filesystem path or a URL, and may be absolute or relative;  if " +
            "a relative value is provided, it will be resolved based on the value of CONTEXT.", 
            new AttributePhrase(Attributes.LOCATION));

    public static final Reagent LOCATION_TASK_NODEFAULT = new SimpleReagent("LOCATION", "@location", ReagentType.PHRASE, String.class,
            "Location of a resource, which may be a filesystem path or a URL, and may be absolute or relative;  if " +
            "a relative value is provided, it will be resolved based on the value of CONTEXT.", null);

    public static final Reagent LOCATION_PHRASE = new SimpleReagent("LOCATION", "descendant-or-self::text()", ReagentType.PHRASE, String.class,
            "Optional location of a resource;  if omitted, the value of the 'Attributes.LOCATION' request attribute " +
            "will be used.  LOCATION may be a filesystem path or a URL, and may be absolute or relative;  if " +
            "a relative value is provided, it will be resolved based on the value of CONTEXT.", 
            new AttributePhrase(Attributes.LOCATION));

    public static final Reagent LOCATION_PHRASE_NODEFAULT = new SimpleReagent("LOCATION", "descendant-or-self::text()", ReagentType.PHRASE, String.class,
            "Optional location of a resource;  if omitted, the value of the 'Attributes.LOCATION' request attribute " +
            "will be used.  LOCATION may be a filesystem path or a URL, and may be absolute or relative;  if " +
            "a relative value is provided, it will be resolved based on the value of CONTEXT.", null);

    public void init(EntityConfig config) {

        // Instance Members.
        // NB:  Doesn't matter which CONTEXT & LOCATION reagents 
        // we use since they have the same name...
        this.contextPhrase = (Phrase) config.getValue(CONTEXT_SOURCE);
        this.locationPhrase = (Phrase) config.getValue(LOCATION_TASK);

    }
    
    /**
     * Indicates whether a resource has been specified in the Cernunnos XML.  
     * This method should only be invoked for resources that are optional and 
     * (consequently) in cases where {@link LOCATION_TASK_NODEFAULT} or 
     * {@link LOCATION_PHRASE_NODEFAULT} have been used to define the reagent.   
     * 
     * @param req The current {@link TaskRequest}
     * @param res The current {@link TaskResponse}
     * @return <code>true</code> if the Cernunnos XML specifies a resource, 
     * otherwise <code>false</code>
     */
    public boolean isSpecified(TaskRequest req, TaskResponse res) {
        return locationPhrase != null;
    }
    
    public static URL evaluate(String context, String location) {

        URL rslt = null;
        try {
            
            // LOCATION
            for (ProtocolTranslator r : translators) {
                if (r.appliesTo(location)) {
                    // Translate a custom protocol...
                    rslt = r.translate(location);
                }
            }
            if (rslt == null) {

                // CONTEXT
                URL u = null;
                for (ProtocolTranslator r : translators) {
                    if (r.appliesTo(context)) {
                        // Translate a custom protocol...
                        u = r.translate(context);
                    }
                }
                if (u == null) {
                    // Use a standard protocol...
                    u = new URL(context);
                }

                rslt = new URL(u, location);

            }

        } catch (Throwable t) {
            String msg = "Unable to construct a URL to the specified resource:" +
                            "\n\t\tCONTEXT=" + context +
                            "\n\t\tLOCATION=" + location;
            throw new RuntimeException(msg, t);
        }
        
        return rslt;

    }

    public URL evaluate(TaskRequest req, TaskResponse res) {
        String ctx = (String) contextPhrase.evaluate(req, res);
        String loc = (String) locationPhrase.evaluate(req, res);
        return ResourceHelper.evaluate(ctx, loc);
    }
    
    /*
     * Nested Types.
     */
    
    private static abstract class ProtocolTranslator {
        
        // Instance Members.
        private final String pattern;
        
        /*
         * Public API.
         */
        
        public ProtocolTranslator(String pattern) {
            
            // Assertions.
            if (pattern == null) {
                String msg = "Argument 'pattern' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            
            // Instance Members.
            this.pattern = pattern;

        }

        public boolean appliesTo(String spec) {
            return spec.matches(pattern);
        }
        
        public abstract URL translate(String spec);
        
    }

}
