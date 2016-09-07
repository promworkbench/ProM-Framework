package org.processmining.framework.providedobjects;

import java.util.List;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.events.ProvidedObjectLifeCycleListener;

public interface ProvidedObjectManager {

	ProvidedObjectLifeCycleListener.ListenerList getProvidedObjectLifeCylceListeners();

	/**
	 * This method creates a new providedObjectID. The context passed to this
	 * method should refer to the PluginContext of which the ProMFuture object
	 * is the result, i.e. this.getProvidedObjectObject(createProvidedObject(
	 * String name, PluginContext context)) = context.getFutureResult(); The
	 * object will be wrapped in a ProMFuture, and a new childContext is created
	 * for this ProMFuture
	 * 
	 * @param name
	 *            The label of the providedObject identified by the returned
	 *            ProvidedObjectID (can be retrieved with
	 *            getProvidedObjectLabel()
	 * @param object
	 *            the object to be wrapped in a new ProMFuture.
	 * @param type
	 *            the type of the object,
	 * @return a globally new id, identifying the providedObject
	 */
	<T> ProvidedObjectID createProvidedObject(String name, T object, Class<? super T> type, PluginContext context);

	/**
	 * This method creates a new providedObjectID. The context passed to this
	 * method should refer to the PluginContext of which the ProMFuture object
	 * is the result, i.e. this.getProvidedObjectObject(createProvidedObject(
	 * String name, PluginContext context)) = context.getFutureResult(); The
	 * object will be wrapped in a ProMFuture, and a new childContext is created
	 * for this ProMFuture
	 * 
	 * Note that no type is provided with this method. Instead, the framework
	 * will use the type of the given object, or if an @SubstitutionType
	 * annotation was used on the object's class definition, the substitution
	 * type is used.
	 * 
	 * @param name
	 *            The label of the providedObject identified by the returned
	 *            ProvidedObjectID (can be retrieved with
	 *            getProvidedObjectLabel()
	 * @param object
	 *            the object to be wrapped in a new ProMFuture.
	 * 
	 * @return a globally new id, identifying the providedObject
	 */
	<T> ProvidedObjectID createProvidedObject(String name, T object, PluginContext context);

	/**
	 * This method creates a list of new providedObjectIDs for each object in
	 * the PluginExecutionResult of the context.
	 * 
	 * @param context
	 *            The context of which this providedObject will be the result
	 * @return a list of globally new ids, identifying the providedObjects
	 */
	List<ProvidedObjectID> createProvidedObjects(PluginContext context);

	/**
	 * returns the provided object identified by this ID. Do not use this method
	 * if not necessary, as the providedObject might not exist in memory
	 * locally.
	 * 
	 * @param id
	 * @param waitIfFuture
	 *            TODO
	 * @return
	 * @throws ProvidedObjectDeletedException
	 */
	Object getProvidedObjectObject(ProvidedObjectID id, boolean waitIfFuture) throws ProvidedObjectDeletedException;

	String getProvidedObjectLabel(ProvidedObjectID id) throws ProvidedObjectDeletedException;

	Class<?> getProvidedObjectType(ProvidedObjectID id) throws ProvidedObjectDeletedException;

	void changeProvidedObjectObject(ProvidedObjectID id, Object newObject) throws ProvidedObjectDeletedException;

	void deleteProvidedObject(ProvidedObjectID id) throws ProvidedObjectDeletedException;

	/**
	 * Returns all provided Object IDs known to the framework. The list is
	 * ordered in order of Arrival, i.e. the first objectID added to the
	 * framework is returned first.
	 * 
	 * @return
	 */
	List<ProvidedObjectID> getProvidedObjects();

	void relabelProvidedObject(ProvidedObjectID id, String label) throws ProvidedObjectDeletedException;

	void setEnabled(boolean enabled);

	boolean isEnabled();

	void clear();
	
}
