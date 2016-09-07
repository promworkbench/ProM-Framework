package org.processmining.framework.plugin;

import java.util.Collection;

import org.processmining.framework.connections.Connection;
import org.processmining.framework.connections.ConnectionCannotBeObtained;

public interface ObjectConstructor {

	/**
	 * Finds as many objects of the given type as can be found through the
	 * connections of the other given type. If no objects can be found, it
	 * constructs as many objects of the given type as there are plugins
	 * available to the framework to do so. These plugins should be able to
	 * execute in a child of the given context and accept, in any order, exactly
	 * the input provided. Any other objects returned by the plugin, not being
	 * the requested type are added to the provided objects of the framework.
	 * Also, listeners are notified where applicable. If no plugings exist, or
	 * if these plugins fail, a ConnectionDoesntExistException is thrown.
	 * 
	 * @param <T>
	 *            The type of the return object required.
	 * @param <C extends Connection> The type of the connections to use when
	 *        trying to find the objects.
	 * @param type
	 *            the type of the return object required.
	 * @param connectionType
	 *            the type of the connections to use when trying to find the
	 *            objects.
	 * @param role
	 *            the assumed role of the object in the connection. Given a
	 *            connection, the object can be retrieved through the role.
	 * @param input
	 *            the input on which the plugin should work
	 * @return a collection of objects as found or returned by the plugins.
	 */
	public <T, C extends Connection> Collection<T> tryToFindOrConstructAllObjects(Class<T> type,
			Class<C> connectionType, String role, Object... input) throws ConnectionCannotBeObtained;

	/**
	 * Returns the first object of the given type as can be found through the
	 * connections of the other given type. If no objects can be found, it
	 * constructs as an object of the given type using a plugin available to the
	 * framework to do so. This plugins should be able to execute in a child of
	 * the given context and accept, in any order, exactly the input provided.
	 * Any other objects returned by the plugin, not being the requested type
	 * are added to the provided objects of the framework. Also, listeners are
	 * notified where applicable. If no plugins exist, or if these plugins fail,
	 * a ConnectionDoesntExistException is thrown.
	 * 
	 * @param <T>
	 *            The type of the return object required.
	 * @param <C extends Connection> The type of the connections to use when
	 *        trying to find the objects.
	 * @param type
	 *            the type of the return object required.
	 * @param connectionType
	 *            the type of the connections to use when trying to find the
	 *            objects.
	 * @param role
	 *            the assumed role of the object in the connection. Given a
	 *            connection, the object can be retrieved through the role.
	 * @param input
	 *            the input on which the plugin should work
	 * @return a collection of objects as returned by the plugins.
	 */
	public <T, C extends Connection> T tryToFindOrConstructFirstObject(Class<T> type, Class<C> connectionType,
			String role, Object... input) throws ConnectionCannotBeObtained;

	/**
	 * Returns the first object of the given type as can be found through the
	 * connections of the other given type. If no objects can be found, it
	 * constructs as an object of the given type using a plugin available to the
	 * framework to do so. This plugins should be able to execute in a child of
	 * the given context and accept, in any order, exactly the input provided.
	 * Furthermore, the returned object should have the right label assigned to
	 * it in the plugin definition. Any other objects returned by the plugin,
	 * not being the requested type are added to the provided objects of the
	 * framework. Also, listeners are notified where applicable. If no plugins
	 * exist, or if these plugins fail, a ConnectionDoesntExistException is
	 * thrown.
	 * 
	 * @param <T>
	 *            The type of the return object required.
	 * @param <C extends Connection> The type of the connections to use when
	 *        trying to find the objects.
	 * @param type
	 *            the type of the return object required.
	 * @param name
	 *            the name of the plugin to use.
	 * @param connectionType
	 *            the type of the connections to use when trying to find the
	 *            objects.
	 * @param role
	 *            the assumed role of the object in the connection. Given a
	 *            connection, the object can be retrieved through the role.
	 * @param input
	 *            the input on which the plugin should work
	 * @return a collection of objects as returned by the plugins.
	 */
	public <T, C extends Connection> T tryToFindOrConstructFirstNamedObject(Class<T> type, String name,
			Class<C> connectionType, String role, Object... input) throws ConnectionCannotBeObtained;

}