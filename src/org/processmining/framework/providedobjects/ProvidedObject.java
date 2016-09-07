package org.processmining.framework.providedobjects;

public interface ProvidedObject {

	/**
	 * Returns the ID of this Provided Object
	 * 
	 * @return
	 */
	ProvidedObjectID getID();

	/**
	 * Returns the label of this provided object
	 * 
	 * @return
	 */
	String getLabel();

	/**
	 * Sets the label of this provided object to the new label.
	 * 
	 * @param label
	 *            the new label of the object
	 * @throws ProvidedObjectDeletedException
	 *             If the object was removed from memory because there were no
	 *             useful references to it anymore
	 */
	void setLabel(String label) throws ProvidedObjectDeletedException;

	/**
	 * Returns the object stored in this provided object. If the object is not
	 * an instance of ProMFuture, then the object itself is returned. Otherwise,
	 * it depends on the parameter whether the ProMFuture is returned, or the
	 * calling thread is blocked until the future finished (or is cancelled)
	 * 
	 * @param waitIfFuture
	 *            if set to false, the returned object might be an instance of
	 *            ProMFuture. Otherwise not.
	 * @return
	 * @throws ProvidedObjectDeletedException
	 *             If the object was removed from memory because there were no
	 *             useful references to it anymore
	 */
	Object getObject(boolean waitIfFuture) throws ProvidedObjectDeletedException;

	/**
	 * Same as calling getObject(true);
	 * 
	 * @return
	 * @throws ProvidedObjectDeletedException
	 *             If the object was removed from memory because there were no
	 *             useful references to it anymore
	 */
	Object getObject() throws ProvidedObjectDeletedException;

	/**
	 * Changes the content of this provided object by replacing the original
	 * object with the new object. This can only be done if the current object
	 * has not been deleted yet.
	 * 
	 * @param object
	 * @throws ProvidedObjectDeletedException
	 *             If the object was removed from memory because there were no
	 *             useful references to it anymore
	 */
	void setObject(Object object) throws ProvidedObjectDeletedException;

	/**
	 * Returns whether or not this object has been deleted. Note that if this
	 * method returns false, no ProvidedObjectDeletedExceptions are thrown in
	 * subsequent, synchronized calls to this provided object.
	 * 
	 * @return
	 */
	boolean isDeleted();

	/**
	 * Signals the provided object to delete itself. After calling this method,
	 * isDeleted() will return true and where applicable,
	 * ProvidedObjectDeletedExceptions will be thrown.
	 */
	void deleteObject();

	/**
	 * Returns the type of the object contained in this Provided Object. If the
	 * internal object is a ProMFuture, then the result type of the future is
	 * returned, i.e. ProMFuture.class is never returned by this method.
	 * 
	 * @return
	 * @throws ProvidedObjectDeletedException
	 *             If the object was removed from memory because there were no
	 *             useful references to it anymore
	 */
	Class<?> getType() throws ProvidedObjectDeletedException;

}
