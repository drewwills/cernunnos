package org.danann.cernunnos.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

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

public final class ExtractArchiveTask implements Task {

	// Instance Members.
	private Phrase context;
	private Phrase location;
	private Phrase to_dir;

	/*
	 * Public API.
	 */

	public static final Reagent CONTEXT = new SimpleReagent("CONTEXT", "@context", ReagentType.PHRASE, String.class,
				"Optional context from which missing elements of the LOCATION will be inferred if it is "
				+ "relative.  If omitted, this task will use either: (1) the value of the 'Attributes.CONTEXT' "
				+ "request attribute if present; or (2) the directory within which Java is executing.",
				new AttributePhrase(Attributes.CONTEXT, new CurrentDirectoryUrlPhrase()));

	public static final Reagent LOCATION = new SimpleReagent("LOCATION", "@location", ReagentType.PHRASE, String.class,
				"Optional location of the archive that will be extracted.  It may be a filesystem path or "
				+ "a URL, and may be absolute or relative.  If relative, the location will be evaluated "
				+ "from the CONTEXT.  If omitted, the value of the 'Attributes.LOCATION' request "
				+ "attribute will be used.", new AttributePhrase(Attributes.LOCATION));

	public static final Reagent TO_DIR = new SimpleReagent("TO_DIR", "@to-dir", ReagentType.PHRASE, String.class,
				"Optional file system directory to which the specified archive will be extracted.  It may be "
				+ "absolute or relative.  If relative, it will be evaluated from the directory in "
				+ "which Java is executing.", null);

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {CONTEXT, LOCATION, TO_DIR};
		final Formula rslt = new SimpleFormula(ExtractArchiveTask.class, reagents);
		return rslt;
	}

	public void init(EntityConfig config) {

		// Instance Members.
		this.context = (Phrase) config.getValue(CONTEXT);
		this.location = (Phrase) config.getValue(LOCATION);
		this.to_dir = (Phrase) config.getValue(TO_DIR);

	}

	public void perform(TaskRequest req, TaskResponse res) {

		String origin = (String) location.evaluate(req, res);
		String dir = to_dir != null ? (String) to_dir.evaluate(req, res) : null;

		InputStream inpt = null;
		JarInputStream zip = null;
		try {

			URL ctx = new URL((String) context.evaluate(req, res));
			URL loc = new URL(ctx, origin);
			inpt = loc.openStream();
			zip = new JarInputStream(inpt);

			byte[] buffer = new byte[1024];
			for (JarEntry entry = zip.getNextJarEntry(); entry != null; entry = zip.getNextJarEntry()) {

				if (entry.isDirectory()) {
					// We need to skip directories...
					continue;
				}

				File f = new File(dir, entry.getName());
				if (f.getParentFile() != null) {
					// Make sure the necessary directories are in place...
					f.getParentFile().mkdirs();
				}

				int count;
				OutputStream os = new FileOutputStream(f);
				while ((count = zip.read(buffer)) > 0) {
					os.write(buffer, 0, count);
				};
				os.close();

				zip.closeEntry();

			}

		} catch (Throwable t) {
			String msg = "Unable to extract the specified archive:  " + origin;
			throw new RuntimeException(msg, t);
		} finally {
			if (zip != null) {
				try {
					zip.close();
				} catch (IOException ioe) {
					throw new RuntimeException(ioe);
				}
			}
			if (inpt != null) {
				try {
					inpt.close();
				} catch (IOException ioe) {
					throw new RuntimeException(ioe);
				}
			}
		}

	}

}