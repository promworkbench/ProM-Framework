package org.processmining.framework.connections.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.processmining.framework.connections.Connection;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.connections.ConnectionID;
import org.processmining.framework.connections.ConnectionManager;
import org.processmining.framework.connections.annotations.ConnectionObjectFactory;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.PluginExecutionResult;
import org.processmining.framework.plugin.PluginManager;
import org.processmining.framework.plugin.PluginParameterBinding;
import org.processmining.framework.plugin.events.ConnectionObjectListener;
import org.processmining.framework.plugin.events.Logger.MessageLevel;
import org.processmining.framework.util.Pair;

public class ConnectionManagerImpl implements ConnectionManager {

	private final Map<ConnectionID, Connection> connections = new HashMap<ConnectionID, Connection>();
	private final ConnectionObjectListener.ListenerList connectionListeners = new ConnectionObjectListener.ListenerList();
	private final PluginManager pluginManager;
	private boolean isEnabled = true;

	public ConnectionManagerImpl(PluginManager pluginManager) {
		this.pluginManager = pluginManager;

	}

	/**
	 * Returns the list of registered connectionObject listeners
	 * 
	 * @return the list of registered connectionObject listeners
	 */
	public ConnectionObjectListener.ListenerList getConnectionListeners() {
		return connectionListeners;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	public void clear() {
		connections.clear();
	}
	
	public <T extends Connection> T addConnection(T connection) {
		if (isEnabled) {
			synchronized (connections) {
				connections.put(connection.getID(), connection);
				connection.setManager(this);
				connectionListeners.fireConnectionCreated(connection.getID());
			}
		}
		return connection;
	}

	public Connection getConnection(ConnectionID id) throws ConnectionCannotBeObtained {
		Connection c = connections.get(id);
		synchronized (connections) {
			if (c == null || c.isRemoved()) {
				connections.remove(c);
				connectionListeners.fireConnectionDeleted(id);
				if (c != null)
					throw new ConnectionCannotBeObtained("Objects were deleted", c.getClass());
				else
					throw new ConnectionCannotBeObtained("Objects were deleted", Object.class);
			}
			return c;
		}
	}

	public <T extends Connection> T getFirstConnection(Class<T> connectionType, PluginContext context,
			Object... objects) throws ConnectionCannotBeObtained {
		return getConnections(true, connectionType, context, objects).iterator().next();
	}

	public <T extends Connection> Collection<T> getConnections(Class<T> connectionType, PluginContext context,
			Object... objects) throws ConnectionCannotBeObtained {
		return getConnections(false, connectionType, context, objects);
	}

	@SuppressWarnings("unchecked")
	private <T extends Connection> Collection<T> getConnections(boolean stopAtFirst, Class<T> connectionType,
			PluginContext context, Object... objects) throws ConnectionCannotBeObtained {
		List<T> available = new ArrayList<T>(1);
		//System.gc();
		synchronized (connections) {
			Iterator<Map.Entry<ConnectionID, Connection>> it = connections.entrySet().iterator();
			while (it.hasNext()) {
				Entry<ConnectionID, Connection> entry = it.next();
				Connection c = entry.getValue();
				if (c.isRemoved()) {
					it.remove();
					connectionListeners.fireConnectionDeleted(c.getID());
					continue;
				}
				if (((connectionType == null) || connectionType.isAssignableFrom(c.getClass()))
						&& c.containsObjects(objects)) {
					context.log("Connection found: " + c, MessageLevel.DEBUG);
					available.add((T) c);
					if (stopAtFirst) {
						return available;
					}
				}
			}
		}
		if (!available.isEmpty()) {
			return available;
		}
		if ((connectionType == null) || (objects.length <= 1)) {
			throw new ConnectionCannotBeObtained("No plugin available to create connection", connectionType, objects);

		}
		Class<?>[] types = new Class<?>[objects.length];
		for (int i = 0; i < objects.length; i++) {
			types[i] = objects[i].getClass();
		}

		Collection<Pair<Integer, PluginParameterBinding>> plugins = pluginManager.find(ConnectionObjectFactory.class,
				connectionType, context.getClass(), true, false, false, types);
		if (plugins.isEmpty()) {
			throw new ConnectionCannotBeObtained("No plugin available to create connection", connectionType, objects);
		}
		PluginContext c2 = context.createChildContext("Creating connection of Type " + connectionType);
		Pair<Integer, PluginParameterBinding> pair = plugins.iterator().next();
		PluginParameterBinding binding = pair.getSecond();
		try {

			PluginExecutionResult pluginResult = binding.invoke(c2, objects);
			pluginResult.synchronize();
			T connectionObject = pluginResult.<T>getResult(pair.getFirst());

			if (connectionObject == null) {
				throw new ConnectionCannotBeObtained("Factory plugin returned null.", connectionType, objects);
			}
			available.add(addConnection(connectionObject));
			context.log("Added connection: " + connectionObject, MessageLevel.DEBUG);
			return available;
		} catch (Exception e) {
			throw new ConnectionCannotBeObtained(e.getMessage(), connectionType, objects);
		} finally {
			c2.getParentContext().deleteChild(c2);
		}
	}

	public Collection<ConnectionID> getConnectionIDs() {
		return connections.keySet();
	}

}
