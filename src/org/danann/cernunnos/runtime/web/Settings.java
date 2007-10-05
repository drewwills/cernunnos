package org.danann.cernunnos.runtime.web;

import java.util.HashMap;
import java.util.Map;

public class Settings {

	// Instance Members.
	private final Map<Entry,String> entries;

	/*
	 * Public API.
	 */

	public static Settings load(Map<String,String> config) {
		Map<Entry,String> entries = new HashMap<Entry,String>();
		for (Entry y : Entry.values()) {
			// Explicitly defined values trump defaults...
			String specValue = config.get(y.name());
			String useValue = specValue != null ? specValue : y.getDefaultValue();
			entries.put(y, useValue);
		}
		return new Settings(entries);
	}

	public String getValue(Entry y) {

		// Assertions.
		if (!entries.containsKey(y)) {
			String msg = "The specified entry is not defined:  " + y.name();
			throw new RuntimeException(msg);
		}

		return entries.get(y);

	}

	/*
	 * Private Stuff.
	 */

	private Settings(Map<Entry,String> entries) {
		this.entries = entries;
	}

	/*
	 * Nested Types.
	 */

	public enum Entry {

		/*
		 * Public API.
		 */

		ACTION_PARAMETER("CernunnosPortlet.ACTION_PARAMETER", "action"),

		ACTION_PREFIX("CernunnosPortlet.ACTION_PREFIX", "/WEB-INF/actions/"),

		ACTION_SUFFIX("CernunnosPortlet.ACTION_SUFFIX", ".crn"),

		VIEW_PARAMETER("CernunnosPortlet.VIEW_PARAMETER", "view"),

		VIEW_PREFIX("CernunnosPortlet.VIEW_PREFIX", "/WEB-INF/views/"),

		VIEW_SUFFIX("CernunnosPortlet.VIEW_SUFFIX", ".crn"),

		DEFAULT_VIEW("CernunnosPortlet.DEFAULT_VIEW", "index");

		// Instance Members.
		private final String name;
		private final String defaultValue;

		public String getName() {
			return name;
		}

		public String getDefaultValue() {
			return defaultValue;
		}

		/*
		 * Private Stuff.
		 */

		private Entry(String name, String defaultValue) {
			this.name = name;
			this.defaultValue = defaultValue;
		}

	}

}
