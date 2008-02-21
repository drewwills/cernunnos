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

package org.danann.cernunnos.spring;

import java.net.URL;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import org.danann.cernunnos.AbstractContainerTask;
import org.danann.cernunnos.AttributePhrase;
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

public final class ApplicationContextTask extends AbstractContainerTask {

	// Instance Members.
	private Phrase context;
	private Phrase location;
	private Phrase cache;
	private URL prevUrl = null;
	private ApplicationContext prevBeans = null;

	/*
	 * Public API.
	 */

	public static final Reagent CONTEXT = new SimpleReagent("CONTEXT", "@context", ReagentType.PHRASE, String.class,
					"The context from which missing elements of the LOCATION can be inferred if it "
					+ "is relative.  The default is the value of the 'Attributes.ORIGIN' request attribute.",
					new AttributePhrase(Attributes.ORIGIN));

	public static final Reagent LOCATION = new SimpleReagent("LOCATION", "@location", ReagentType.PHRASE, String.class,
					"Location of a spring XML bean definition file.  May be a filesystem path (absolute or relative), or " +
					"a URL.  If relative, the location will be evaluated from the CONTEXT.  If omitted, the value of the " +
					"'Attributes.LOCATION' request attribute will be used.", new AttributePhrase(Attributes.LOCATION));

	public static final Reagent CACHE = new SimpleReagent("CACHE", "@cache", ReagentType.PHRASE, Boolean.class,
					"If true (the default), this task will retain and reuse an existing ApplicationContext " +
					"unless/until either:  (1) a different XML file is specified;  or (2) this reagent " +
					"becomes false.", new LiteralPhrase(Boolean.TRUE));

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {CONTEXT, LOCATION, CACHE, AbstractContainerTask.SUBTASKS};
		final Formula rslt = new SimpleFormula(ApplicationContextTask.class, reagents);
		return rslt;
	}

	public void init(EntityConfig config) {
		
		super.init(config);

		// Instance Members.
		this.context = (Phrase) config.getValue(CONTEXT);
		this.location = (Phrase) config.getValue(LOCATION);
		this.cache = (Phrase) config.getValue(CACHE);
		
	}

	public void perform(TaskRequest req, TaskResponse res) {
		
		String ctx_str = (String) context.evaluate(req, res);
		String loc_str = (String) location.evaluate(req, res);
		
		try {
			
			URL ctx = new URL(ctx_str);
			URL config = new URL(ctx, loc_str);

			ApplicationContext beans = getApplicationContext(config, 
							(Boolean) cache.evaluate(req, res));
			for (String name : beans.getBeanDefinitionNames()) {
				res.setAttribute(name, beans.getBean(name));
			}

		} catch (Throwable t) {
			String msg = "Unable to read the specified bean definition file:"
				+ "\n\tCONTEXT=" + ctx_str
				+ "\n\tLOCATION=" + loc_str;
			throw new RuntimeException(msg, t);
		}

		super.performSubtasks(req, res);
		
	}
	
	/*
	 * Implementation.
	 */
	
	private synchronized ApplicationContext getApplicationContext(URL config, boolean useCache) {
		
		if (!useCache || !config.equals(prevUrl)) {
			prevBeans = new FileSystemXmlApplicationContext(config.toExternalForm());
			prevUrl = config;
		}
		
		return prevBeans;		

	}
	
}