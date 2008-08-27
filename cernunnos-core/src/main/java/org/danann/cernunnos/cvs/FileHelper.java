package org.danann.cernunnos.cvs;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class FileHelper {
	
	// Instance Members.
	private final Log log = LogFactory.getLog(getClass());

	/*
	 * Public API.
	 */

	public List<File> collectFiles(String context, String path, boolean recurse) {
		
		// Assertions.
		if (context == null) {
			String msg = "Argument 'context' cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		if (path == null) {
			String msg = "Argument 'path' cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		
		List<File> rslt = new LinkedList<File>();
		File file = new File(context, path);
		
		if (log.isDebugEnabled()) {
			log.debug("Placing file '" + file.getAbsolutePath() + 
						"' into the list for CVS AddTask.");
		}
		
		rslt.add(file);
System.out.println("FileHelper collecting:  " + file.getAbsolutePath());

		// Recurse if appropriate...
		if (recurse && file.isDirectory()) {
			for (File f : file.listFiles()) {
				if (f.getName().equals("CVS")) {
					// We *must* prevent CVS directories from 
					// being send to the cvsclient API...
					continue;
				}
				rslt.addAll(collectFiles(file.getAbsolutePath(), f.getName(), true));
			}
		}
		
		return rslt;
		
	}

}
