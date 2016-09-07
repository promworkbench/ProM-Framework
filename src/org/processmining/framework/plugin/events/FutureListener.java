package org.processmining.framework.plugin.events;

import java.util.EventListener;

import org.processmining.framework.plugin.ProMFuture;

public interface FutureListener extends EventListener {

	public class ListenerList extends ProMEventListenerList<FutureListener> {
		public void fireFutureReady(ProMFuture<? extends Object> future) {
			for (FutureListener listener : getListeners()) {
				listener.futureReady(future);
			}
		}
	}

	public void futureReady(ProMFuture<? extends Object> future);

}
