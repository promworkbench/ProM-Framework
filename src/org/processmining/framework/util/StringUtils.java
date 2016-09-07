package org.processmining.framework.util;

public class StringUtils {

	private StringUtils() {

	}

	public static String getJavaIdentifier(String name) {
		StringBuffer result = new StringBuffer();
		boolean underscoreAdded = false;

		name = name.toLowerCase().trim();
		for (int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);
			if ((('a' <= c) && (c <= 'z')) || (('A' <= c) && (c <= 'Z'))
					|| ((result.length() > 0) && ('0' <= c) && (c <= '9'))) {
				result.append(c);
				underscoreAdded = false;
			} else if (!underscoreAdded) {
				result.append("_");
				underscoreAdded = true;
			}
		}
		return result.toString();
	}

}
