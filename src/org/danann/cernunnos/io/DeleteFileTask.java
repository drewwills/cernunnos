package org.danann.cernunnos.io;

import java.io.File;
import java.net.URL;

import org.danann.cernunnos.AttributePhrase;
import org.danann.cernunnos.Attributes;
import org.danann.cernunnos.CurrentDirectoryUrlPhrase;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.Task;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public class DeleteFileTask implements Task {

	// Instance Members.
	private Phrase context;
	private Phrase file;

	/*
	 * Public API.
	 */

	public static final Reagent CONTEXT = new SimpleReagent("CONTEXT", "@context", ReagentType.PHRASE, String.class, 
			"Optional context from which missing elements of the LOCATION will be inferred if it is "
			+ "relative.  If omitted, this task will use either: (1) the value of the 'Attributes.CONTEXT' "
			+ "request attribute if present; or (2) the directory within which Java is executing.", 
			new AttributePhrase(Attributes.CONTEXT, new CurrentDirectoryUrlPhrase()));

	public static final Reagent FILE = new SimpleReagent("FILE", "@file", ReagentType.PHRASE, String.class, 
			"Optional file system location of the file that will be deleted.  It may be absolute or "
			+ "relative.  If relative, the location will be evaluated from the CONTEXT.  If omitted, the "
			+ "value of the 'Attributes.LOCATION' request attribute will be used.", 
			new AttributePhrase(Attributes.LOCATION));

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {CONTEXT, FILE};
		final Formula rslt = new SimpleFormula(DeleteFileTask.class, reagents);
		return rslt;
	}
	
	public void init(EntityConfig config) {
		
		// Instance Members.
		this.context = (Phrase) config.getValue(CONTEXT); 
		this.file = (Phrase) config.getValue(FILE);

	}

	public void perform(TaskRequest req, TaskResponse res) {

		
		String ctx = (String) context.evaluate(req, res);
		String path = (String) file.evaluate(req, res);

		File f = null;
		try {

			URL ctxUrl = new URL(ctx);
			URL loc = new URL(ctxUrl, path);
			f = new File(loc.toURI());
			f.delete();
			
		} catch (Throwable t) {
			String msg = "Unable to delete the specified file:  " + f.getAbsolutePath();
			throw new RuntimeException(msg, t);
		}
		
	}

}