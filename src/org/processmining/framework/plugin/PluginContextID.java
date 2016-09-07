package org.processmining.framework.plugin;

/**
 * Represents an ID of a plugin context.
 * 
 * @author bfvdonge
 * 
 */
public interface PluginContextID extends Comparable<PluginContextID> {

	/**
	 * PluginContextID are used in Collections, so this method has to be
	 * implemented in all its subclasses.
	 * 
	 * @return
	 */
	public boolean equals(Object o);

	/**
	 * PluginContextID are used in HashMaps, so this method has to be
	 * implemented in all its subclasses.
	 * 
	 * @return
	 */
	public int hashCode();

}
