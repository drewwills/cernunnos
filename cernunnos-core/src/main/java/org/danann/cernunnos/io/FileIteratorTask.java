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

package org.danann.cernunnos.io;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

public final class FileIteratorTask extends AbstractContainerTask {
    
    private static final Pattern LIST_DELIM = Pattern.compile(",");
    private static final Pattern PATH_DELIM = Pattern.compile("/");

	// Instance Members.
	private Phrase attribute_name;
	private Phrase dir;
	private Phrase includes;
	private Phrase excludes;
	private final Log log = LogFactory.getLog(FileIteratorTask.class);	// Don't declare as static in general libraries

	/*
	 * Public API.
	 */

	public static final Reagent ATTRIBUTE_NAME = new SimpleReagent("ATTRIBUTE_NAME", "@attribute-name", ReagentType.PHRASE,
				String.class, "Each file will be registered as a request attribute under this name during iteration.",
				new LiteralPhrase(Attributes.LOCATION));

	public static final Reagent DIR = new SimpleReagent("DIR", "@dir", ReagentType.PHRASE, String.class,
				"File system location of a directory from which to begin matching files.  The default "
				+ "is the directory from which Java is executing.", new LiteralPhrase("."));

	public static final Reagent INCLUDES = new SimpleReagent("INCLUDES", "@includes", ReagentType.PHRASE, String.class,
				"Optional comma-separated list of pattern expressions specifying files to include (e.g. *, *.jpg, " +
				"**/*.java).  Files that match the pattern must be included, even if they also match the EXCLUDES " +
				"pattern.  The default is ** (i.e. all files).", new LiteralPhrase("**"));

	public static final Reagent EXCLUDES = new SimpleReagent("EXCLUDES", "@excludes", ReagentType.PHRASE, String.class,
				"Optional comma-separated list of pattern expressions specifying files to exclude from the result set " +
				"(e.g. *, *.jpg, **/*.java).  No files will be excluded if this phrase is omitted.", null);

	public Formula getFormula() {
		Reagent[] reagents = new Reagent[] {ATTRIBUTE_NAME, DIR, INCLUDES, EXCLUDES,
											AbstractContainerTask.SUBTASKS};
		final Formula rslt = new SimpleFormula(FileIteratorTask.class, reagents);
		return rslt;
	}

	public void init(EntityConfig config) {

		super.init(config);

		// Instance Members.
		this.attribute_name = (Phrase) config.getValue(ATTRIBUTE_NAME);
		this.dir = (Phrase) config.getValue(DIR);
		this.includes = (Phrase) config.getValue(INCLUDES);
		this.excludes = (Phrase) config.getValue(EXCLUDES);

	}

	public void perform(TaskRequest req, TaskResponse res) {

		String d = (String) dir.evaluate(req, res);
		File baseDir = new File(d);

		if (!baseDir.exists()) {
			// We're going to assume this won't happen
			// except where something's really wrong...
			String msg = "The specified DIR does not exist:  " + d;
			log.error(msg);
			throw new RuntimeException(msg);
		}

		String incl = (String) includes.evaluate(req, res);
		String[] inclTokens = LIST_DELIM.split(incl);
		List<String>[] inclStacks = new List[inclTokens.length];
		for (int i=0; i < inclTokens.length; i++) {
			inclStacks[i] = Arrays.asList(PATH_DELIM.split(inclTokens[i]));
		}

		String excl = "[Not Evaluated]";

		Set<File> fileSet = getMatchingDescendants(baseDir, inclStacks);

		if (excludes != null) {
			excl = (String) excludes.evaluate(req, res);
			String[] exclTokens = LIST_DELIM.split(excl);
			List<String>[] exclStacks = new List[exclTokens.length];
			for (int i=0; i < exclTokens.length; i++) {
				exclStacks[i] = Arrays.asList(PATH_DELIM.split(exclTokens[i]));
			}
			Set<File> exclSet = getMatchingDescendants(baseDir, exclStacks);
			fileSet.removeAll(exclSet);
		}

		// Report on the # of matched files...
		if (log.isTraceEnabled()) {
			StringBuilder msg = new StringBuilder();
			msg.append("FileIteratorTask details:")
					.append("\n\t\tDIR=").append(d)
					.append("\n\t\tINCLUDES=").append(incl)
					.append("\n\t\tEXCLUDES=").append(excl)
					.append("\n\t\tfound ").append(fileSet.size())
					.append(" matching files\n");
			log.trace(msg.toString());
		}

		// Set the default CONTEXT for subtasks...
		final URL contextUrl;
        try {
            contextUrl = baseDir.toURL();
        }
        catch (MalformedURLException mue) {
            throw new RuntimeException("Failed to convert dir attribute into URL: " + baseDir, mue);
        }
        res.setAttribute(Attributes.CONTEXT, contextUrl.toString());

		// Iterate over the results, setting each one to the specified request attribute...
		String name = (String) attribute_name.evaluate(req, res);
		String mask;
        try {
            mask = baseDir.getCanonicalPath();
        }
        catch (IOException ioe) {
            throw new RuntimeException("Could not convert dir attribute into canonical path: " + baseDir, ioe);
        }
		if (!mask.endsWith(File.separator)) {
			mask = mask + File.separator;
		}
		for (File f : fileSet) {
			final String canonicalPath;
            try {
                canonicalPath = f.getCanonicalPath();
            }
            catch (IOException ioe) {
                throw new RuntimeException("Could not convert file into canonical path: " + f, ioe);
            }
            
            String location = canonicalPath.substring(mask.length());
			res.setAttribute(name, location);
			super.performSubtasks(req, res);
		}
	}

	/*
	 * Implementation.
	 */

	private static Set<File> getMatchingDescendants(File f, List<String>[] stackLists) {

		Set<File> rslt = new TreeSet<File>();

		for (List<String> stack : stackLists) {
			rslt.addAll(getMatchingDescendants(f, stack));
		}

		return rslt;

	}

	private static Set<File> getMatchingDescendants(File f, List<String> stack) {

		// Assertions...
		if (f == null) {
			String msg = "Argument 'f [File]' cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		if (stack == null) {
			String msg = "Argument 'stack' cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		if (stack.size() == 0) {
			String msg = "Argument 'stack' must contain at least one element.";
			throw new IllegalArgumentException(msg);
		}

		Set<File> rslt = new TreeSet<File>();

		switch (stack.size()) {
			case 1:
				// We're ready to compile the results...
				rslt.addAll(new FileGrabber(stack.get(0), false).grabChildren(f));
				break;
			default:
				// Make recursive calls...
				List<String> nextStack = new LinkedList<String>();
				nextStack.addAll(stack.subList(1, stack.size()));
				for (File ff : new FileGrabber(stack.get(0), true).grabChildren(f)) {
					rslt.addAll(getMatchingDescendants(ff, nextStack));
				}
				break;
		}

		return rslt;

	}

	/*
	 * Nested Types.
	 */

	private static final class FileGrabber {

		// Static Members.
		private static final int LAST_CHARACTER = -2;
		private static final int ONLY_CHARACTER = -3;

		// Instance Members.
		private final String startsWith;	// Everything before *
		private final String endsWith;		// Everything after *
		private final String contains;		// Between * and *
		private final boolean grabDirectories;
		private final boolean recurse;

		/*
		 * Public API.
		 */

		public FileGrabber(String pattern, boolean isDirectory) {

			// Assertions...
			if (pattern == null) {
				String msg = "Argument 'pattern' cannot be null.";
				throw new IllegalArgumentException(msg);
			}

			// Instance Members.
			this.grabDirectories = isDirectory;

			// Special Case.
			if (pattern.equals("**")) {
				this.startsWith = null;
				this.endsWith = null;
				this.contains = null;
				this.recurse = true;
			} else {

				// Everything Else.
				int index = pattern.indexOf("*");
				if (index == pattern.length() - 1) {
					// Seems pretty lame to have to do this simply to use a switch()...
					index = LAST_CHARACTER;
				}
				if (index == 0 && pattern.length() == 1) {
					// The only character is a wildcard...
					index = ONLY_CHARACTER;
				}
				switch (index) {
					case -1:
						// No wildcrad...
						startsWith = pattern;
						endsWith = pattern;
						contains = null;
						break;
					case 0:
						// Starts with wildcard...
						startsWith = null;
						// Another Special Case:  use "contains" if the pattern
						// both begins and ends with a wildcard.
						if (pattern.endsWith("*")) {
							endsWith = null;
							contains = pattern.substring(1, pattern.length() - 1);
						} else {
							// Just use endsWith...
							endsWith = pattern.substring(1, pattern.length());
							contains = null;
						}
						break;
					case LAST_CHARACTER:
						// Ends with wildcard...
						startsWith = pattern.substring(0, pattern.length() - 1);
						endsWith = null;
						contains = null;
						break;
					case ONLY_CHARACTER:
						// Only a wildcard...
						startsWith = null;
						endsWith = null;
						contains = null;
						break;
					default:
						// Somewhere in the middle...
						startsWith = pattern.substring(0, index);
						endsWith = pattern.substring(index + 1);
						contains = null;
						break;
				}
				this.recurse = false;

			}

		}

		public Set<File> grabChildren(File dir) {

			// Assertions...
			if (dir == null) {
				String msg = "Argument 'dir' cannot be null.";
				throw new IllegalArgumentException(msg);
			}

			Set<File> rslt = new TreeSet<File>();

			if (recurse && grabDirectories) {
				// In this case we need to add the current dir to the output as
				// well so its children get checked again on the next revolution...
				rslt.add(dir);
			}

			final File[] listFiles = dir.listFiles();
			if (listFiles == null) {
                throw new IllegalArgumentException("Could not get listing of files under '" + dir + "'");
			}
            for (File child : listFiles) {

				boolean addIt = (grabDirectories == child.isDirectory());
				if (addIt && startsWith != null) {
					addIt = child.getName().startsWith(startsWith);
				}
				if (addIt && endsWith != null) {
					addIt = child.getName().endsWith(endsWith);
				}
				if (addIt && contains != null) {
					addIt = child.getName().contains(contains);
				}
				if (addIt) {
//					System.out.println("FileIteratorTask.FileGrabber.grabChildren():child.getAbsolutePath()="+child.getAbsolutePath());
					rslt.add(child);
				}
				if (recurse && child.isDirectory()) {
					rslt.addAll(grabChildren(child));
				}

			}

			return rslt;

		}

	}

}