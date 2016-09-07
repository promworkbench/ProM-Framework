package org.processmining.framework.plugin.impl;

import org.processmining.framework.connections.ConnectionManager;
import org.processmining.framework.connections.impl.ConnectionManagerImpl;
import org.processmining.framework.plugin.GlobalContext;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.PluginContextID;
import org.processmining.framework.plugin.PluginDescriptor;
import org.processmining.framework.plugin.PluginManager;
import org.processmining.framework.plugin.PluginParameterBinding;
import org.processmining.framework.plugin.events.Logger;
import org.processmining.framework.providedobjects.ProvidedObjectManager;
import org.processmining.framework.providedobjects.impl.ProvidedObjectManagerImpl;

/**
 * Since this context should maintain a link to a single plugin context, and
 * PluginContextImpl is abstract, this context is also abstract.
 * 
 * @author bfvdonge
 * 
 */
public abstract class AbstractGlobalContext implements GlobalContext {

	private final ProvidedObjectManager providedObjectManager = new ProvidedObjectManagerImpl();
	private final ConnectionManager connectionManager = new ConnectionManagerImpl(PluginManagerImpl.getInstance());
	protected final Logger.ListenerList loggingEventListeners = new Logger.ListenerList();

	public PluginManager getPluginManager() {
		return PluginManagerImpl.getInstance();
	}

	public ConnectionManager getConnectionManager() {
		return connectionManager;
	}

	public ProvidedObjectManager getProvidedObjectManager() {
		return providedObjectManager;
	}

	public PluginContextID createNewPluginContextID() {
		return new PluginContextIDImpl();
	}

	public void invokeBinding(PluginParameterBinding binding, Object... objects) {

		PluginContext c2 = getMainPluginContext().createChildContext(binding.getPlugin().getName());

		try {
			// Create a new providedObject, by passing the future to the
			// providedObjectManager;
			getMainPluginContext().getPluginLifeCycleEventListeners().firePluginCreated(c2);
			binding.invoke(c2, objects);
			getProvidedObjectManager().createProvidedObjects(c2);

		} catch (IllegalArgumentException e) {
			getMainPluginContext().log(e);
			return;
		} finally {
			c2.getParentContext().deleteChild(c2);
		}

		// Since the result is still being calculated, we can only
		// use the defaultResultName for the providedObject.

	}

	public void invokePlugin(PluginDescriptor plugin, int index, Object... objects) {
		PluginContext c2 = getMainPluginContext().createChildContext(plugin.getName());

		try {
			// Create a new providedObject, by passing the future to the
			// providedObjectManager;
			getMainPluginContext().getPluginLifeCycleEventListeners().firePluginCreated(c2);
			plugin.invoke(index, c2, objects);
			getProvidedObjectManager().createProvidedObjects(c2);

		} catch (IllegalArgumentException e) {
			getMainPluginContext().log(e);
			return;
		} finally {
			c2.getParentContext().deleteChild(c2);
		}

		// Since the result is still being calculated, we can only
		// use the defaultResultName for the providedObject.

	}

	protected abstract PluginContext getMainPluginContext();

	public abstract Class<? extends PluginContext> getPluginContextType();

}
