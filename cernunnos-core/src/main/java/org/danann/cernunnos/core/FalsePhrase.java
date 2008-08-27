package org.danann.cernunnos.core;

import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

@Deprecated
public final class FalsePhrase implements Phrase {
	
	/*
	 * Public API.
	 */
	
	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {};
		return new SimpleFormula(getClass(), reagents);
	}
	
	public void init(EntityConfig config) {}

	public Object evaluate(TaskRequest req, TaskResponse res) {

		return Boolean.FALSE;
		
	}
	
}
