package org.processmining.framework.plugin.events;

import java.util.EventListener;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.providedobjects.ProvidedObjectID;

public interface ProvidedObjectLifeCycleListener extends EventListener {

	public void providedObjectCreated(ProvidedObjectID objectID, PluginContext context);

	public void providedObjectFutureReady(ProvidedObjectID objectID);

	public void providedObjectNameChanged(ProvidedObjectID objectID);

	public void providedObjectObjectChanged(ProvidedObjectID objectID);

	public void providedObjectDeleted(ProvidedObjectID objectID);

	public class ListenerList extends ProMEventListenerList<ProvidedObjectLifeCycleListener> {
		public void fireProvidedObjectCreated(ProvidedObjectID objectID, PluginContext context) {
			for (ProvidedObjectLifeCycleListener listener : getListeners()) {
				listener.providedObjectCreated(objectID, context);
			}
		}

		public void fireProvidedObjectNameChanged(ProvidedObjectID objectID) {
			for (ProvidedObjectLifeCycleListener listener : getListeners()) {
				listener.providedObjectNameChanged(objectID);
			}
		}

		public void fireProvidedObjectObjectChanged(ProvidedObjectID objectID) {
			for (ProvidedObjectLifeCycleListener listener : getListeners()) {
				listener.providedObjectObjectChanged(objectID);
			}
		}

		public void fireProvidedObjectDeleted(ProvidedObjectID objectID) {
			for (ProvidedObjectLifeCycleListener listener : getListeners()) {
				listener.providedObjectDeleted(objectID);
			}
		}

		public void fireProvidedObjectFutureReady(ProvidedObjectID objectID) {
			for (ProvidedObjectLifeCycleListener listener : getListeners()) {
				listener.providedObjectFutureReady(objectID);
			}
		}
	}
}
