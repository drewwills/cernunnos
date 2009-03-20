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

import java.util.Map;

import org.danann.cernunnos.AbstractContainerTask;
import org.danann.cernunnos.Attributes;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.LiteralPhrase;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Node;

public final class SerializeGrammarTask extends AbstractContainerTask {

	// Static Members.
    private Phrase recursive;
    private static final DocumentFactory fac = new DocumentFactory();
	
    // Instance Members.
    private XmlGrammar grammar;

    /*
     * Public API.
     */

    public static final Reagent RECURSIVE = new SimpleReagent("RECURSIVE", "@recursive", ReagentType.PHRASE, 
                    Boolean.class, "If true, entries from parent grammar(s) will be inclded in the " +
                    "generated XML.  The default is true.", new LiteralPhrase(Boolean.TRUE));

    public Formula getFormula() {
        Reagent[] reagents = new Reagent[] {RECURSIVE, AbstractContainerTask.SUBTASKS};
        final Formula rslt = new SimpleFormula(getClass(), reagents);
        return rslt;
    }

    public void init(EntityConfig config) {

        super.init(config);

        // Instance Members.
        this.recursive = (Phrase) config.getValue(RECURSIVE);
        this.grammar = (XmlGrammar) config.getGrammar();

    }

    public void perform(TaskRequest req, TaskResponse res) {

        boolean recurse = (Boolean) this.recursive.evaluate(req, res);
        
        Element rslt = fac.createElement("grammar");
        rslt.addAttribute("name", grammar.getName());
        rslt.addAttribute("origin", grammar.getOrigin());

        for (Entry e : grammar.getEntries(recurse)) {
            rslt.add(serializeEntry(e, fac));
        }

        res.setAttribute(Attributes.NODE, rslt);
        super.performSubtasks(req, res);

    }

    /*
     * Implementation.
     */

    private static Node serializeEntry(Entry e, DocumentFactory fac) {

        // Assertions...
        if (e == null) {
            String msg = "Argument 'e [Entry]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (fac == null) {
            String msg = "Argument 'fac' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        Element rslt = fac.createElement("entry");
        rslt.addAttribute("type", e.getType().name());

        // Name.
        Element name = fac.createElement("name");
        name.setText(e.getName());
        rslt.add(name);

        // Deprecation.
        if (e.isDeprecated()) {
            Element dep = fac.createElement("deprecation");
            dep.addAttribute("version", e.getDeprecation().getVersion());
            for (Element n : e.getDeprecation().getDescription()) {
            	dep.add((Element) n.clone());
            }
            rslt.add(dep);
        }

        // Description.
        Element desc = e.getDescription() != null 
        				? (Element) e.getDescription().clone() 
        				: fac.createElement("description");
        rslt.add(desc);

        // Formula.
        rslt.add(serializeFormula(e.getFormula(), e.getMappings(), fac));

        // Examples.
        for (Node x : e.getExamples()) {
            rslt.add((Node) x.clone());
        }

        return rslt;

    }

    private static Node serializeFormula(Formula f, Map<Reagent,Object> mappings, DocumentFactory fac) {

        // Assertions...
        if (f == null) {
            String msg = "Argument 'f [Formula]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (fac == null) {
            String msg = "Argument 'fac' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        Element rslt = fac.createElement("formula");

        // Impl.
        rslt.addAttribute("impl", f.getImplementationClass().getName());

        // Reagents.
        Element reagents = fac.createElement("reagents");
        for (Reagent r : f.getReagents()) {
            if (mappings.containsKey(r)) {
                // We don't list it, since there's a final value...
                continue;
            }
            Element reagent = fac.createElement("reagent");
            reagent.addAttribute("name", r.getName());
            reagent.addAttribute("xpath", r.getXpath().getText());
            reagent.addAttribute("reagent-type", r.getReagentType().name());
            reagent.addAttribute("expected-type", r.getExpectedType().getName());
            boolean req = !r.hasDefault();
            reagent.addAttribute("required", req ? "Yes" : "No");
            Element desc = fac.createElement("description");
            if (r.getDescription() != null) {
                desc.setText(r.getDescription());
            }
            reagent.add(desc);
            if (r.isDeprecated()) {
            	Element dep = fac.createElement("deprecation");
            	dep.addAttribute("version", r.getDeprecation().getVersion());
            	for (Element e : r.getDeprecation().getDescription()) {
            		dep.add((Element) e.clone());
            	}
            	reagent.add(dep);
            }
            reagents.add(reagent);
        }
        rslt.add(reagents);

        return rslt;

    }

}