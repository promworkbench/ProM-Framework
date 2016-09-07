package org.processmining.framework.plugin;

public class RecursiveCallException extends Exception {

	private static final long serialVersionUID = -1731740485590676086L;

	public RecursiveCallException(PluginContext context, PluginDescriptor plugin, int methodIndex) {
		super("A recursive call to method " + methodIndex + " of plugin " + plugin.getName()
				+ " was discrovered when creating context " + context.getID() + ".");
	}
}
