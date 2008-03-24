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

package org.danann.cernunnos.ldap;

import java.util.LinkedList;
import java.util.List;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;

import org.dom4j.Node;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapTemplate;

import org.danann.cernunnos.AbstractContainerTask;
import org.danann.cernunnos.AttributePhrase;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.LiteralPhrase;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.Task;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;
import org.danann.cernunnos.sql.SqlAttributes;

/**
 * Performs a specified LDAP query, then invokes child tasks once for each row 
 * in the result set.
 */
public final class SearchTask extends AbstractContainerTask {

	// Instance Members.
	private Phrase attributeName;
	private Phrase contextSource;
	private Phrase baseDn;
	private Phrase filter;
	private Phrase scope;
	private Phrase limit;
	private Phrase timeout;
	private List<Phrase> attributes;
	private Phrase returnObject;
	private Phrase dereferenceLinks;
	private Phrase attributesMapper;

	/*
	 * Public API.
	 */

	public static final Reagent ATTRIBUTE_NAME = new SimpleReagent("ATTRIBUTE_NAME", "@attribute-name", ReagentType.PHRASE, String.class,
					"Optional name under which each search result will be registered as a request attribute.  If omitted, " +
					"the name 'Attributes.OBJECT' will be used.", new LiteralPhrase(org.danann.cernunnos.Attributes.OBJECT));

	public static final Reagent CONTEXT_SOURCE = new SimpleReagent("CONTEXT_SOURCE", "@context-source", 
    				ReagentType.PHRASE, ContextSource.class, "Spring-LDAP ContextSource object to use " +
    				"for executing the LDAP search. If omitted the request attribute under the name " +
            		"'LdapAttributes.CONTEXT_SOURCE' will be used.", 
            		new AttributePhrase(LdapAttributes.CONTEXT_SOURCE));

    public static final Reagent BASE_DN = new SimpleReagent("BASE_DN", "@base-dn", ReagentType.PHRASE, 
    				String.class, "Optional base DN from which the search will begin.  The default " +
    				"is null.", new LiteralPhrase(""));
    	
    public static final Reagent FILTER = new SimpleReagent("FILTER", "@filter", ReagentType.PHRASE, 
					String.class, "The filter (query) to use in the search.");

    public static final Reagent SCOPE = new SimpleReagent("SCOPE", "@scope", ReagentType.PHRASE, Integer.class, 
    				"Either SearchControls.OBJECT_SCOPE, SearchControls.ONELEVEL_SCOPE, or " +
    				"SearchControls.SUBTREE_SCOPE.  SearchControls is provided by the JDK in the " +
    				"'javax.naming.directory' package.  The default is SearchControls.ONELEVEL_SCOPE.", 
    				new LiteralPhrase(SearchControls.ONELEVEL_SCOPE));

    public static final Reagent LIMIT = new SimpleReagent("LIMIT", "@limit", ReagentType.PHRASE, 
    				String.class, "The maximum number of entries to return from the search. If 0 " +
    				"(zero) is specified, the search will return all entries that satisfy FILTER.  " +
    				"The default is 0 (zero).", new LiteralPhrase("0"));
    
    public static final Reagent TIMEOUT = new SimpleReagent("TIMEOUT", "@timeout", ReagentType.PHRASE, 
					String.class, "The number of milliseconds to wait before returning. If 0 (zero) " +
					"is specified, this task will wait indefinitely.  The default is 0 (zero).", 
					new LiteralPhrase("0"));

	public static final Reagent ATTRIBUTES = new SimpleReagent("ATTRIBUTES", "attribute/text()", ReagentType.NODE_LIST, 
					List.class, "The identifiers of the attributes to return along with the entry. If null, " +
					"all attributes will be returned;  If empty, no attributes will be returned.  The default " +
					"is null (all attributes).", null);

    public static final Reagent RETURN_OBJECT = new SimpleReagent("RETURN_OBJECT", "@return-object", ReagentType.PHRASE, 
					Boolean.class, "Return the object bound to the entry name if true;  otherwise, return only the " +
					"name and class of the object.  The default is Boolean.FALSE.",  new LiteralPhrase(Boolean.FALSE));

    public static final Reagent DEREFERENCE_LINKS = new SimpleReagent("DEREFERENCE_LINKS", "@dereference-links", 
    				ReagentType.PHRASE, Boolean.class, "If true, dereference links during search.  The default " +
    				"is Boolean.FALSE.", new LiteralPhrase(Boolean.FALSE));

    public static final Reagent ATTRIBUTES_MAPPER = new SimpleReagent("ATTRIBUTES_MAPPER", "@attributes-mapper", 
					ReagentType.PHRASE, AttributesMapper.class, "Interface defined by Spring LDAP for mapping " +
					"LDAP Attributes to beans.  The default ATTRIBUTES_MAPPER simply returns an instance of " +
					"javax.naming.directory.Attributes.", new LiteralPhrase(new DefaultAttributesMapper()));

	public static final Reagent SUBTASKS = new SimpleReagent("SUBTASKS", "subtasks/*", ReagentType.NODE_LIST, List.class,
					"The set of tasks that are children of this search task.", new LinkedList<Task>());

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {ATTRIBUTE_NAME, CONTEXT_SOURCE, 
						BASE_DN, FILTER, SCOPE, LIMIT, TIMEOUT, ATTRIBUTES, 
						RETURN_OBJECT, DEREFERENCE_LINKS, ATTRIBUTES_MAPPER, 
						SUBTASKS};
		final Formula rslt = new SimpleFormula(getClass(), reagents);
		return rslt;
	}

	@Override
    public void init(EntityConfig config) {

		super.init(config);

		// Instance Members.
		this.attributeName = (Phrase) config.getValue(ATTRIBUTE_NAME);
		this.contextSource = (Phrase) config.getValue(CONTEXT_SOURCE);
		this.baseDn = (Phrase) config.getValue(BASE_DN);
		this.filter = (Phrase) config.getValue(FILTER);
		this.scope = (Phrase) config.getValue(SCOPE);
		this.limit = (Phrase) config.getValue(LIMIT);
		this.timeout = (Phrase) config.getValue(TIMEOUT);
        final Object attr = config.getValue(ATTRIBUTES);
        if (attr != null) {
        	final List<Node> nodes = (List<Node>) attr;
        	this.attributes = new LinkedList<Phrase>();
            for (final Node n : nodes) {
                attributes.add(config.getGrammar().newPhrase(n.getText()));
            }
        } else {
    		this.attributes = null;	// also the default...
        }
		this.returnObject = (Phrase) config.getValue(RETURN_OBJECT);
		this.dereferenceLinks = (Phrase) config.getValue(DEREFERENCE_LINKS);
		this.attributesMapper = (Phrase) config.getValue(ATTRIBUTES_MAPPER);

	}

	public void perform(TaskRequest req, TaskResponse res) {

		// Construct the LdapRemplate...
		final ContextSource cs = (ContextSource) contextSource.evaluate(req, res);
		final LdapTemplate template = new LdapTemplate(cs);
		
		// Construct the SearchControls...
		final int p = (Integer) scope.evaluate(req, res);
		final long m = Long.valueOf((String) limit.evaluate(req, res));
		final int o = Integer.valueOf((String) timeout.evaluate(req, res));
		String[] a = null;	// default...
		if (attributes != null) {
			a = new String[attributes.size()];
			for (int i=0; i < a.length; i++) {
				a[i] = (String) attributes.get(i).evaluate(req, res);
			}
		}
		final boolean ro = (Boolean) returnObject.evaluate(req, res);
		final boolean dl = (Boolean) dereferenceLinks.evaluate(req, res);
		SearchControls controls = new SearchControls(p, m, o, a, ro, dl);
		
		// Execute the search...
		final String name = (String) attributeName.evaluate(req, res);
		final String bdn = (String) baseDn.evaluate(req, res);
		final String ftr = (String) filter.evaluate(req, res);
		final AttributesMapper am = (AttributesMapper) attributesMapper.evaluate(req, res);
		try {
			
			final List<?> rslt = template.search(bdn, ftr, controls, am);
			for (Object j : rslt) {
				res.setAttribute(name, j);
				super.performSubtasks(req, res);
			}

		} catch (Throwable t) {
			String msg = "Error performing the specified LDAP search:"
						+ "\n\t\tBASE_DN=" + bdn
						+ "\n\t\tFILTER=" + ftr
						+ "\n\t\tSCOPE=" + p
						+ "\n\t\tLIMIT=" + m
						+ "\n\t\tTIMEOUT=" + o
						+ "\n\t\tATTRIBUTES=" + a
						+ "\n\t\tRETURN_OBJECT=" + ro
						+ "\n\t\t=DEREFERENCE_LINKS=" + dl
						+ "\n\t\t=ATTRIBUTES_MAPPER (class)=" + am.getClass().getName();
			throw new RuntimeException(msg, t);
		}
				
	}
	
	/*
	 * Nested Types.
	 */

	private static final class DefaultAttributesMapper implements AttributesMapper {
		public Object mapFromAttributes(Attributes attr) throws javax.naming.NamingException {
			return attr;
		}
	}
	
}
