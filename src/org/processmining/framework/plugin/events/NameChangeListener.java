package org.processmining.framework.plugin.events;

import java.util.EventListener;

public interface NameChangeListener extends EventListener {

	public class ListenerList extends ProMEventListenerList<NameChangeListener> {
		public void fireNameChanged(String newName) {
			for (NameChangeListener listener : getListeners()) {
				listener.nameChanged(newName);
			}
		}
	}

	public void nameChanged(String newName);
}
