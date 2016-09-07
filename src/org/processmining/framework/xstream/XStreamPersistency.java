package org.processmining.framework.xstream;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.XStream;

public class XStreamPersistency {

	private static List<XStreamConverter> converters = new ArrayList<XStreamConverter>();

	public static boolean addConverter(XStreamConverter converter) {
		if (!converters.contains(converter)) {
			converters.add(converter);
			return true;
		}
		return false;
	}

	/**
	 * Registers all XES converters for XStream with the provided stream,
	 * registers corresponding aliases.
	 * 
	 * @param stream
	 *            The XStream instance to register with
	 */
	public static void register(XStream stream) {
		for (XStreamConverter converter : converters) {
			converter.register(stream);
		}
	}
}
