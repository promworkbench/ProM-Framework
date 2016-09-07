package org.processmining.framework.plugin.events;

import java.util.EventListener;

import org.processmining.framework.connections.ConnectionID;

public interface ConnectionObjectListener extends EventListener {

	public class ListenerList extends ProMEventListenerList<ConnectionObjectListener> {
		public void fireConnectionCreated(ConnectionID connectionID) {
			for (ConnectionObjectListener listener : getListeners()) {
				listener.connectionCreated(connectionID);
			}
		}

		public void fireConnectionDeleted(ConnectionID id) {
			for (ConnectionObjectListener listener : getListeners()) {
				listener.connectionDeleted(id);
			}
		}

		public void fireConnectionUpdated(ConnectionID id) {
			for (ConnectionObjectListener listener : getListeners()) {
				listener.connectionUpdated(id);
			}
		}
	}

	/**
	 * This method signals the creation of a connection with the given ID. For
	 * access to the actual connection, the connectionManager should be used.
	 * 
	 * @param connectionID
	 */
	public void connectionCreated(ConnectionID connectionID);

	/**
	 * This method signals the deletion of a connection with the given ID. For
	 * access to the actual connection, the connectionManager should be used.
	 * 
	 * @param connectionID
	 */
	public void connectionDeleted(ConnectionID connectionID);

	/**
	 * This method signals that the connection with the given ID was updated.
	 * For access to the actual connection, the connectionManager should be
	 * used.
	 * 
	 * Be aware, that the actual pointers to the objects connected by the
	 * Connection with the given ID are not changed. Furthermore, it is wise not
	 * to update connections too often.
	 * 
	 * @param connectionID
	 */
	public void connectionUpdated(ConnectionID connectionID);

}
