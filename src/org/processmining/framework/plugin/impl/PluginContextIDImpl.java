package org.processmining.framework.plugin.impl;

import java.util.UUID;

import org.processmining.framework.plugin.PluginContextID;

public class PluginContextIDImpl implements PluginContextID {

	private final UUID id = UUID.randomUUID();

	public String toString() {
		return id.toString();
	}

	public int hashCode() {
		return id.hashCode();
	}

	public boolean equals(Object o) {
		if (!(o instanceof PluginContextIDImpl)) {
			return false;
		} else {
			return ((PluginContextIDImpl) o).id.equals(id);
		}
	}

	public int compareTo(PluginContextID o) {
		if (!(o instanceof PluginContextIDImpl)) {
			// Nasty implementation and not likely to be necessary,
			// as the ProvidedObjectManager should take care of not
			// using different object as ID.
			return PluginContextIDImpl.class.getName().compareTo(o.getClass().getName());
		} else {
			return ((PluginContextIDImpl) o).id.compareTo(id);
		}
	}
}
