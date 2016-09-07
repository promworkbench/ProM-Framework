package org.processmining.framework.providedobjects.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.PluginExecutionResult;
import org.processmining.framework.plugin.ProMFuture;
import org.processmining.framework.plugin.events.ProvidedObjectLifeCycleListener;
import org.processmining.framework.providedobjects.ProvidedObject;
import org.processmining.framework.providedobjects.ProvidedObjectDeletedException;
import org.processmining.framework.providedobjects.ProvidedObjectID;
import org.processmining.framework.providedobjects.ProvidedObjectManager;
import org.processmining.framework.providedobjects.SubstitutionType;

public class ProvidedObjectManagerImpl implements ProvidedObjectManager {

	private final ProvidedObjectLifeCycleListener.ListenerList providedObjectLifeCycleListeners = new ProvidedObjectLifeCycleListener.ListenerList();
	private final HashMap<ProvidedObjectID, ProvidedObjectImpl> localProvidedObjects;
	private final List<ProvidedObjectID> ids;

	private boolean enabled = true;

	public ProvidedObjectManagerImpl() {
		localProvidedObjects = new HashMap<ProvidedObjectID, ProvidedObjectImpl>();
		ids = new ArrayList<ProvidedObjectID>();
	}

	public ProvidedObjectLifeCycleListener.ListenerList getProvidedObjectLifeCylceListeners() {
		return providedObjectLifeCycleListeners;
	}

	@SuppressWarnings("unchecked")
	public <T> ProvidedObjectID createProvidedObject(String label, T object, PluginContext context) {
		Class<?> realType;
		if (object instanceof ProMFuture<?>) {
			realType = ((ProMFuture<?>) object).getReturnType();
		} else {
			realType = object.getClass();
		}
		if (realType.isAnnotationPresent(SubstitutionType.class)) {
			Class<?> declaredType = realType.getAnnotation(SubstitutionType.class).substitutedType();
			if (declaredType.isAssignableFrom(realType)) {
				realType = declaredType;
			}
		}
		return createProvidedObject(label, object, (Class<? super T>) realType, context);
	}

	public <T> ProvidedObjectID createProvidedObject(String label, T object, Class<? super T> type,
			PluginContext context) {

		// construct a new ProvidedObject
		ProvidedObjectImpl po = new ProvidedObjectImpl(label, new ProvidedObjectIDImpl(), object, type, this);

		if (enabled) {
			// add it to the list of maintained PO's
			localProvidedObjects.put(po.getID(), po);
			ids.add(po.getID());
			providedObjectLifeCycleListeners.fireProvidedObjectCreated(po.getID(), context);
			if (!(object instanceof ProMFuture<?>)) {
				providedObjectLifeCycleListeners.fireProvidedObjectFutureReady(po.getID());
			} else {
				try {
					po.setLabel(((ProMFuture<?>) object).toString());
				} catch (ProvidedObjectDeletedException e) {
					assert (false);
				}
			}
		}
		return po.getID();

	}

	public List<ProvidedObjectID> createProvidedObjects(PluginContext context) {
		List<ProvidedObjectID> newIds = new ArrayList<ProvidedObjectID>();
		PluginExecutionResult result = context.getResult();
		for (int i = 0; i < result.getSize(); i++) {
			ProvidedObjectID id = createProvidedObject(result.getResultName(i), result.getResult(i), result.getType(i),
					context);
			newIds.add(id);
			//			ids.add(id);
			result.setProvidedObjectID(i, id);
		}
		return newIds;
	}

	private ProvidedObjectImpl getProvidedObject(ProvidedObjectID id) throws ProvidedObjectDeletedException {
		ProvidedObjectImpl po = localProvidedObjects.get(id);
		if (po == null) {
			throw new ProvidedObjectDeletedException("ProvidedObject with ID " + id + " is not known to the manager.");
		}
		return po;
	}

	public Object getProvidedObjectObject(ProvidedObjectID id, boolean waitIfFuture)
			throws ProvidedObjectDeletedException {
		ProvidedObject po = getProvidedObject(id);
		return po.getObject(waitIfFuture);
	}

	public String getProvidedObjectLabel(ProvidedObjectID id) throws ProvidedObjectDeletedException {
		ProvidedObject po = getProvidedObject(id);
		return po.getLabel();
	}

	public Class<? extends Object> getProvidedObjectType(ProvidedObjectID id) throws ProvidedObjectDeletedException {
		ProvidedObject po = getProvidedObject(id);
		return po.getType();
	}

	public void changeProvidedObjectObject(ProvidedObjectID id, Object newObject) throws ProvidedObjectDeletedException {
		ProvidedObject po = getProvidedObject(id);
		po.setObject(newObject);
	}

	public void deleteProvidedObject(ProvidedObjectID id) throws ProvidedObjectDeletedException {
		ProvidedObjectImpl po = getProvidedObject(id);
		localProvidedObjects.remove(id);
		ids.remove(id);
		po.deleteObject();
	}

	public List<ProvidedObjectID> getProvidedObjects() {
		return Collections.unmodifiableList(ids);
	}

	public void relabelProvidedObject(ProvidedObjectID id, String label) throws ProvidedObjectDeletedException {
		getProvidedObject(id).setLabel(label);
	}

	public void providedObjectNameChanged(ProvidedObjectID objectID) {
		// Ignore
	}

	public void providedObjectObjectChanged(ProvidedObjectID objectID) {
		// Ignore
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isEnabled() {
		return enabled;
	}
	
	public void clear() {
		localProvidedObjects.clear();
		ids.clear();
		providedObjectLifeCycleListeners.removeAll();
	}
	
	
}
