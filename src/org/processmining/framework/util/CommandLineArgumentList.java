package org.processmining.framework.util;

import java.util.ArrayList;

public class CommandLineArgumentList extends ArrayList<String> {

	private static final long serialVersionUID = -1574623826051169007L;

	public String[] toStringArray() {
		return toArray(new String[0]);
	}

}
