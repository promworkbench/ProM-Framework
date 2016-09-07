package org.processmining.framework.connections;

import java.util.Collection;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.events.ConnectionObjectListener;

public interface ConnectionManager {

	/**
	 * Returns a collection of connections between the objects specified, such
	 * that the type of the connection is assignable from the given
	 * connectionType (unless the parameter equals null).
	 * 
	 * If no connections satisfying these criteria exist and the required type
	 * is specified and no required name is specified, then the global context
	 * searches for all available plugins with a ConnectionObjectFactory
	 * annotation, which can be executed in a child of the given PluginContext
	 * and accept the given objects as input
	 * 
	 * If such plugins exist, the first of these plugins is selected and invoked
	 * on the given objects. The result is obtained from the plugin and a new
	 * connection is registered of the right type. This connection is then
	 * returned.
	 * 
	 * @param <T>
	 *            the type of the requested connection.
	 * @param connectionType
	 *            The type of the object requested. This type can be null, in
	 *            which case all types are considered.
	 * @param context
	 *            The context which requests the connection. If a plugin is
	 *            invoked to create a connection, a child context of this
	 *            context is instantiated
	 * @param objects
	 *            the objects which should be connected by the requested
	 *            connection. There might be more objects involved in the
	 *            connection
	 * @return A collection of connections of the requested type T. If no
	 *         connection exists, an exception is thrown, hence the collection
	 *         is never empty.
	 * @throws ConnectionCannotBeObtained
	 *             if the requested connection does not exist and cannot be
	 *             produced in the given context.
	 */
	<T extends Connection> Collection<T> getConnections(Class<T> connectionType, PluginContext context,
			Object... objects) throws ConnectionCannotBeObtained;

	/**
	 * Returns a connection between the objects specified, such that the type of
	 * the connection is assignable from the given connectionType (unless the
	 * parameter equals null).
	 * 
	 * If no connections satisfying these criteria exist and the required type
	 * is specified and no required name is specified, then the global context
	 * searches for all available plugins with a ConnectionObjectFactory
	 * annotation, which can be executed in a child of the given PluginContext
	 * and accept the given objects as input
	 * 
	 * If such plugins exist, the first of these plugins is selected and invoked
	 * on the given objects. The result is obtained from the plugin and a new
	 * connection is registered of the right type. This connection is then
	 * returned.
	 * 
	 * @param <T>
	 *            the type of the requested connection.
	 * @param connectionType
	 *            The type of the object requested. This type can be null, in
	 *            which case all types are considered.
	 * @param context
	 *            The context which requests the connection. If a plugin is
	 *            invoked to create a connection, a child context of this
	 *            context is instantiated
	 * @param objects
	 *            the objects which should be connected by the requested
	 *            connection. There might be more objects involved in the
	 *            connection
	 * @return A connection of the requested type T. If no connection exists, an
	 *         exception is thrown, hence null is never returned.
	 * @throws ConnectionCannotBeObtained
	 *             if the requested connection does not exist and cannot be
	 *             produced in the given context.
	 */
	<T extends Connection> T getFirstConnection(Class<T> connectionType, PluginContext context, Object... objects)
			throws ConnectionCannotBeObtained;

	/**
	 * Returns the connection with the given ID.
	 * 
	 * @param id
	 *            the connection ID
	 * @return the connection with the given ID
	 */
	Connection getConnection(ConnectionID id) throws ConnectionCannotBeObtained;

	/**
	 * Returns the list of registered connectionObject listeners
	 * 
	 * @return the list of registered connectionObject listeners
	 */
	ConnectionObjectListener.ListenerList getConnectionListeners();

	/**
	 * Returns a collection of connection IDs registered to this global context.
	 * 
	 * @return the ids of the registered connections
	 */
	Collection<ConnectionID> getConnectionIDs();

	/**
	 * Adds the given connection to the framework.
	 * 
	 * @param connection
	 *            The connection to be registered
	 * @return the given parameter connection
	 */
	<T extends Connection> T addConnection(T connection);

	/**
	 * Returns whether connections are enabled. If not, then connecitons will not be added.
	 * @return whether connections are enabled
	 */
	boolean isEnabled();

	/**
	 * Sets whether connections are enabled.
	 * @param isEnabled whether connections should be enabled
	 */
	void setEnabled(boolean isEnabled);

	void clear();
}
