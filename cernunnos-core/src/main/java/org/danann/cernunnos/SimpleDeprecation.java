package org.danann.cernunnos;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;

/**
 * Implements the <code>Deprecation</code> type in a straightforward way.
 */
public class SimpleDeprecation implements Deprecation {

	// Static Members.
	private static final DocumentFactory fac = new DocumentFactory();
	
	// Instance Members.
	private final String version;
	private final List<Element> description;
	
	/*
	 * Public API.
	 */
	
	public SimpleDeprecation(String version, String description) {
		this(version, buildDescription(description));
	}
	
	public SimpleDeprecation(String version, List<Element> description) {
		
		// Assertions.
		if (version == null) {
			String msg = "Argument 'version' cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		if (description == null) {
			String msg = "Argument 'description' cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		
		// Instance Members.
		this.version = version;
		this.description = Collections.unmodifiableList(description);
		
	}
	
	public String getVersion() {
		return version;
	}

	public List<Element> getDescription() {
		return description;
	}

	/*
	 * Implementation.
	 */

	private static final List<Element> buildDescription(String description) {
		
		// Assertions.
		if (description == null) {
			String msg = "Argument 'description' cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		
		List<Element> rslt = new LinkedList<Element>();
		Element e = fac.createElement("p");
		e.setText(description);
		rslt.add(e);
		return rslt;

	}
	
}
