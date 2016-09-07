package org.processmining.framework.providedobjects;

import org.processmining.framework.connections.ConnectionManager;
import org.processmining.framework.plugin.PluginManager;

/**
 * If an object implements this interface, then this object should use transient
 * fields to store pointers to the three managers.
 * 
 * Upon deserialization of the object by the ProM framework, the three methods
 * in this interface are called before the object is used and added to the
 * framework as a provided object.
 * 
 * Using this interface, objects can instantiate themselves as listeners on the
 * relevant parts of the framework that would otherwise only be available upon
 * instantiation from a PluginContext.
 * 
 * @author bfvdonge
 * 
 */
public interface ContextAwareObject {

	public void setManagers(ConnectionManager connectionManager, PluginManager pluginManager,
			ProvidedObjectManager providedObjectManager);
}
