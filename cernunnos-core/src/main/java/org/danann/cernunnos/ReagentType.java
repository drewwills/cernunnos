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

package org.danann.cernunnos;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.dom4j.Node;
import org.dom4j.XPath;

/**
 * Identifies the runtime type of a <code>SimpleReagent</code>.  SimpleReagent types help
 * Cernunnos bootstrap reagents appropriately.  The vast majority of
 * circumstances call for <code>ReagentType.PHRASE</code>.  Use
 * <code>ReagentType.NODE_LIST</code> when the value of a reagent is an XML
 * structure.  Use <code>ReagentType.STRING</code> when the value must be
 * evaluated at bootstrap time.
 */
public enum ReagentType {

    /*
     * Public API.
     */

    /**
     * Use <code>ReagentType.PHRASE</code> in most cases.  This type allows the
     * final value to be calculated using <code>PhraseComponent</code>
     * implementations, which provide tremendous flexibility.
     */
    PHRASE {
        @Override
        public Object evaluate(Grammar grammar, Node src, XPath xpath) {

            // Assertions...
            if (grammar == null) {
                String msg = "Argument 'grammar' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (src == null) {
                String msg = "Argument 'src' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (xpath == null) {
                String msg = "Argument 'xpath' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            Node match = xpath.selectSingleNode(src);
			
            // We're obligated to intercept empty 
            // TEXT nodes and treat them as null...
            if (match != null && match.getNodeType() == Node.TEXT_NODE 
			                        && match.getText().length() == 0) {
                match = null;
            }
			
            return match != null ? grammar.newPhrase(match) : null;

        }
    },

    /**
     * Use <code>ReagentType.STRING</code> when the final value of the
     * <code>SimpleReagent</code> is used (or must be known) at bootstrap time.
     */
    STRING {
        @Override
        public Object evaluate(Grammar grammar, Node src, XPath xpath) {

            // Assertions...
            if (grammar == null) {
                String msg = "Argument 'grammar' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (src == null) {
                String msg = "Argument 'src' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (xpath == null) {
                String msg = "Argument 'xpath' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            final String value = xpath.valueOf(src).trim();	// do we need to trim?
            return value.length() > 0 ? value : null;

        }
    },

    /**
     * Use <code>ReagentType.NODE_LIST</code> when the final value of the
     * <code>SimpleReagent</code> is an XML structure.
     */
    NODE_LIST {
        @Override
        public Object evaluate(Grammar grammar, Node src, XPath xpath) {

            // Assertions...
            if (grammar == null) {
                String msg = "Argument 'grammar' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (src == null) {
                String msg = "Argument 'src' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (xpath == null) {
                String msg = "Argument 'xpath' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            // The following fancy conversion is here to avoid type safety warnings..
            final List<?> matches = xpath.selectNodes(src);
            final List<Node> rslt = new LinkedList<Node>();

            for (final Iterator<?> it = matches.iterator(); it.hasNext();) {
                Node match = (Node) it.next();
                // We're obligated to intercept empty 
                // TEXT nodes and treat them as null...
                if (match != null && !(match.getNodeType() == Node.TEXT_NODE 
	                                    && match.getText().length() == 0)) {
                    rslt.add(match);
                }
            }

            return rslt.size() != 0 ? rslt : null;

        }
    };

    /**
     * Calculates the value of a <code>Reagent</code> given the specified
     * <code>Grammar</code>, XML, and <code>XPath</code>.  This method returns 
     * <code>null</null> if either (1) the XPath expression matches nothing or 
     * (2) the XPath expression matches an empty <code>TEXT</code> node. 
     *
     * @param grammar The currently governing <code>Grammar</code> instance
     * @param src An XML structure that defines a task
     * @param xpath A pre-compiled <code>XPath</code> object
     * node to calculate the value of a <code>SimpleReagent</code>
     * @return A reagent or <code>null</code>
     */
    public abstract Object evaluate(Grammar grammar, Node src, XPath xpath);

}
