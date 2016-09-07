package org.processmining.framework.plugin.impl;

import java.lang.reflect.Method;

import org.processmining.framework.plugin.PluginDescriptorID;

public class PluginDescriptorIDImpl implements PluginDescriptorID {

	private final String id;

	public PluginDescriptorIDImpl(Method pluginMethod) {
		String temp = pluginMethod.toGenericString();
		int index = temp.indexOf(" throws ");
		if (index >= 0) {
			temp = temp.substring(0, index);
		}
		id = temp;
	}

	public PluginDescriptorIDImpl(Class<?> pluginClass) {
		id = pluginClass.getName();
	}

	public PluginDescriptorIDImpl(MacroPluginDescriptorImpl macroPluginDescriptorImpl) {
		id = macroPluginDescriptorImpl.getFileName();
	}

	public String toString() {
		return id;
	}

	public int hashCode() {
		return id.hashCode();
	}

	public boolean equals(Object o) {
		if (!(o instanceof PluginDescriptorIDImpl)) {
			return false;
		} else {
			return ((PluginDescriptorIDImpl) o).id.equals(id);
		}
	}

	public int compareTo(PluginDescriptorID o) {
		if (!(o instanceof PluginDescriptorIDImpl)) {
			// Nasty implementation and not likely to be necessary,
			// as the ProvidedObjectManager should take care of not
			// using different object as ID.
			return PluginDescriptorIDImpl.class.getName().compareTo(o.getClass().getName());
		} else {
			return ((PluginDescriptorIDImpl) o).id.compareTo(id);
		}
	}
}
