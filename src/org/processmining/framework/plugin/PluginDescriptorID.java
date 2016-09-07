package org.processmining.framework.plugin;

/**
 * This interface provides an ID for each plugin. These IDs are persistent
 * between executions of ProM, provided that no code changes are made to the
 * plugin in question.
 * 
 * @author bfvdonge
 * 
 */
public interface PluginDescriptorID extends Comparable<PluginDescriptorID> {

	/**
	 * ProvidedObjectIDs are used in Collections, so this method has to be
	 * implemented in all its subclasses.
	 * 
	 * @return
	 */
	public boolean equals(Object o);

	/**
	 * ProvidedObjectIDs are used in HashMaps, so this method has to be
	 * implemented in all its subclasses.
	 * 
	 * @return
	 */
	public int hashCode();

}
