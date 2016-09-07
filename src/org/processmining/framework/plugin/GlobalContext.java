package org.processmining.framework.plugin;

import org.processmining.framework.connections.ConnectionManager;
import org.processmining.framework.providedobjects.ProvidedObjectManager;

/**
 * Any implementation should maintain a referencte to a single plugin context.
 * This plugin context is then used create children, in which plugins are
 * executed.
 * 
 * @author bfvdonge
 * 
 */
public interface GlobalContext {

	/**
	 * Returns the plugin manager. The plugin manager can be used to query for
	 * plugins which are registered in ProM.
	 * 
	 * @return the plugin manager
	 */
	PluginManager getPluginManager();

	/**
	 * Returns the providedObject manager. The providedObject manager can be
	 * used to query for providedObjects which are registered in ProM. The
	 * manager should be a ProvidedObjectListener on all PluginInstanceContexts
	 * created by createRootInstanceContext.
	 * 
	 * @return the providedObject manager
	 */
	ProvidedObjectManager getProvidedObjectManager();

	/**
	 * Returns the connection manager. The connection manager can be used to
	 * query for connections which are registered in ProM.
	 * 
	 * @return the connection manager
	 */

	ConnectionManager getConnectionManager();

	/**
	 * The GlobalContext implementation should create IDs for all PluginContexts
	 * instantiated for it.
	 * 
	 * @return
	 */
	PluginContextID createNewPluginContextID();

	/**
	 * This method invokes the specified plugin in a context which is a child of
	 * the main plugin context maintained by this globalContext. No results are
	 * provided to the method calling this method and the plugin is executed
	 * some time in the future, as decided by the implementing class.
	 * 
	 * @param plugin
	 *            The plugin that should be invoked by the framework
	 * @param index
	 *            The index of the plugin method that should be invoked.
	 * @param objects
	 *            The objects to serve as input as accepted by the method at
	 *            index of the plugin
	 */
	void invokePlugin(PluginDescriptor plugin, int index, Object... objects);

	/**
	 * This method invokes the specified binding in a context which is a child
	 * of the main plugin context maintained by this globalContext. No results
	 * are provided to the method calling this method and the plugin is executed
	 * some time in the future, as decided by the implementing class.
	 * 
	 * @param binding
	 *            The binding that should be invoked by the framework
	 * @param objects
	 *            The objects to serve as input as accepted by the given
	 *            binding.
	 */
	void invokeBinding(PluginParameterBinding binding, Object... objects);

	/**
	 * Returns the specific type of the PluginContext. This type is used to
	 * instantiate new contexts in which to invoke plugins.
	 * 
	 * @return the type of PluginContext provided by this global context
	 */
	Class<? extends PluginContext> getPluginContextType();

}
