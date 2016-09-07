package org.processmining.framework.providedobjects.impl;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.ProMFuture;
import org.processmining.framework.plugin.events.FutureListener;
import org.processmining.framework.plugin.events.NameChangeListener;
import org.processmining.framework.providedobjects.ProvidedObject;
import org.processmining.framework.providedobjects.ProvidedObjectDeletedException;
import org.processmining.framework.providedobjects.ProvidedObjectID;
import org.processmining.framework.providedobjects.ProvidedObjectManager;

public class ProvidedObjectImpl implements ProvidedObject, NameChangeListener, FutureListener {

	private final ProvidedObjectID id;
	private Object object;
	private boolean deleted = false;
	private final ProvidedObjectManager manager;
	private String label;
	private final Class<?> type;

	public <T> ProvidedObjectImpl(String label, ProvidedObjectID id, T object, Class<? super T> type,
			ProvidedObjectManager manager) throws NullPointerException {
		this.manager = manager;
		this.id = id;
		this.type = type;
		if (object == null) {
			throw new NullPointerException("Null cannot be provided as an object");
		}
		try {
			setObject(object, false);
		} catch (ProvidedObjectDeletedException e) {
			assert (false);
		}
		this.label = label;
	}

	public void deleteObject() {
		deleted = true;
		// unregister as a name-change listener on the old object
		unregisterFromFuture();
		object = null;
		// Notify all listeners to the deleted object
		manager.getProvidedObjectLifeCylceListeners().fireProvidedObjectDeleted(getID());
	}

	private void unregisterFromFuture() {
		if (object instanceof ProMFuture<?>) {
			((ProMFuture<?>) object).getNameChangeListeners().remove(this);
			((ProMFuture<?>) object).getFutureListeners().remove(this);
		}
	}

	public ProvidedObjectID getID() {
		return id;
	}

	public Object getObject() throws ProvidedObjectDeletedException {
		return getObject(true);
	}

	public Object getObject(boolean waitIfFuture) throws ProvidedObjectDeletedException {
		if (deleted) {
			throw new ProvidedObjectDeletedException("Object " + getLabel() + " has been deleted.");
		}
		if (waitIfFuture && (object instanceof ProMFuture<?>)) {
			try {
				return ((ProMFuture<?>) object).get();
			} catch (Exception e) {
				// This is a listener on object and will receive a message
				// from ProMFuture resulting in a delete.
				setObject(null);
			}
		}
		return object;
	}

	public void setObject(Object newObject) throws ProvidedObjectDeletedException {
		setObject(newObject, true);
	}

	private void setObject(Object newObject, boolean signalChange) throws ProvidedObjectDeletedException {
		if (deleted) {
			throw new ProvidedObjectDeletedException("Object " + getLabel() + " has been deleted.");
		}
		// unregister as a name-change listener on the old object
		unregisterFromFuture();
		if (newObject == null) {
			deleteObject();
			return;
		}
		object = newObject;
		synchronized (object) {
			if (object instanceof ProMFuture<?>) {
				// register as a name-change listener on the new object
				((ProMFuture<?>) object).getNameChangeListeners().add(this);
				// To make sure that no name changed are missed:
				label = (((ProMFuture<?>) object).getLabel());
				((ProMFuture<?>) object).getFutureListeners().add(this);
			}
		}
		if (signalChange) {
			// Notify the listeners of the manager
			manager.getProvidedObjectLifeCylceListeners().fireProvidedObjectObjectChanged(id);
		}
	}

	public String toString() {

		return getLabel() + ": " + (deleted ? " DELETED" : object.toString());
	}

	public String getLabel() {
		return label;
	}

	public int hashCode() {
		return id.hashCode();
	}

	public boolean equals(Object o) {
		if (o instanceof ProvidedObject) {
			return id.equals(((ProvidedObject) o).getID());
		} else {
			return false;
		}
	}

	public synchronized void nameChanged(String newName) {
		label = newName;
		manager.getProvidedObjectLifeCylceListeners().fireProvidedObjectNameChanged(getID());
	}

	public void setLabel(String label) throws ProvidedObjectDeletedException {
		if (isDeleted()) {
			throw new ProvidedObjectDeletedException("Provided Object with ID " + getID() + " was deleted before.");
		}
		nameChanged(label);
	}

	public synchronized void pluginCancelled(PluginContext context) {
		// if the plugin was cancelled, then delete this
		// providedObject
		try {
			setObject(null);
		} catch (ProvidedObjectDeletedException e) {
			// Don't care
		}
	}

	public boolean isDeleted() {
		return deleted;
	}

	public Class<? extends Object> getType() throws ProvidedObjectDeletedException {
		if (isDeleted()) {
			throw new ProvidedObjectDeletedException("Provided Object with ID " + getID() + " was deleted before.");
		}
		return type;
	}

	public synchronized void futureReady(ProMFuture<? extends Object> future) {
		try {
			try {
				if (!deleted) {
					Object tmpObject = ((ProMFuture<?>) object).get();
					setObject(tmpObject, false);
					if (tmpObject != null) {
						// if tmpObject == null, then this is deleted by
						// the setObject method;
						manager.getProvidedObjectLifeCylceListeners().fireProvidedObjectFutureReady(getID());
					}
				}
			} catch (CancellationException e) {
				setObject(null);
			} catch (InterruptedException e) {
				setObject(null);
			} catch (ExecutionException e) {
				setObject(null);
			}
		} catch (ProvidedObjectDeletedException e) {
			// Ignore;

		}
	}
}
