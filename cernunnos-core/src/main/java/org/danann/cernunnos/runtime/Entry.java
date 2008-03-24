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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Node;

import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Reagent;


public final class Entry {

    // Static Members.
    private static final DocumentFactory fac = new DocumentFactory();

    // Instance Members.
    private final String name;
    private final Type type;
    private final Element description;
    private final Formula formula;
    private final Map<Reagent,Object> mappings;
    private final List<Node> examples;

    /*
     * Public API.
     */

    public enum Type {
        TASK,
        PHRASE
    }

    public Entry(String name, Type type, String description, Formula f, Map<Reagent,Object> mappings, List<Node> examples) {
        this(name, type, fac.createElement("description"), f, mappings, examples);
        Element p = fac.createElement("p");
        p.setText(description);
        this.description.add(p);
    }

    public Entry(String name, Type type, Element description, Formula f, Map<Reagent,Object> mappings, List<Node> examples) {

        // Assertions...
        if (name == null) {
            String msg = "Argument 'name' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (type == null) {
            String msg = "Argument 'type' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        // NB:  description may be null...
        if (f == null) {
            String msg = "Argument 'f [Formula]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (mappings == null) {
            String msg = "Argument 'mappings' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (examples == null) {
            String msg = "Argument 'examples' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.name = name;
        this.type = type;
        this.description = description;
        this.formula = f;
        this.mappings = (Map<Reagent,Object>) Collections.unmodifiableMap(mappings);
        this.examples = (List<Node>) Collections.unmodifiableList(examples);

    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public Element getDescription() {
        return description;
    }

    public Formula getFormula() {
        return formula;
    }

    public Map<Reagent,Object> getMappings() {
        return mappings;
    }

    public List<Node> getExamples() {
        return examples;
    }

}
