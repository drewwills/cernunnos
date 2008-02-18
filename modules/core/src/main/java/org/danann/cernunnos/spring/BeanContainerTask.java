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
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public final class BeanContainerTask extends AbstractContainerTask {

	// Instance Members.
	private Phrase context;
	private Phrase location;

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

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {CONTEXT, LOCATION, AbstractContainerTask.SUBTASKS};
		final Formula rslt = new SimpleFormula(BeanContainerTask.class, reagents);
		return rslt;
	}

	public void init(EntityConfig config) {
		
		super.init(config);

		// Instance Members.
		this.context = (Phrase) config.getValue(CONTEXT);
		this.location = (Phrase) config.getValue(LOCATION);
		
	}

	public void perform(TaskRequest req, TaskResponse res) {
		
		String ctx_str = (String) context.evaluate(req, res);
		String loc_str = (String) location.evaluate(req, res);
		
		try {
			
			URL ctx = new URL(ctx_str);
			URL doc = new URL(ctx, loc_str);

			ApplicationContext beans = new FileSystemXmlApplicationContext(doc.toExternalForm());
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
	
}