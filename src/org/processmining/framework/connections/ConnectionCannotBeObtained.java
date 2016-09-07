package org.processmining.framework.connections;

import org.processmining.framework.connections.annotations.ConnectionDoesntExistMessage;

/**
 * Exception thrown by the connectionmanager if a requested connection does not
 * exist.
 * 
 * @author bfvdonge
 * 
 */
public class ConnectionCannotBeObtained extends Exception {

	private final Class<? extends Object> connectionType;

	/**
	 * Constructor with message and cause
	 * 
	 * @param reason
	 *            The reason of throwing this exception
	 * @param ex
	 *            the cause of this exception
	 */
	public ConnectionCannotBeObtained(String reason, ConnectionCannotBeObtained ex) {
		this(reason + " \n " + ex.getMessage(), ex.connectionType);
	}

	/**
	 * Constructor with reason, connection type and objects. The message is set
	 * to: "No known connection of type + connectionType + " between " +
	 * Arrays.toString(objects) + " [" + reason + "]";
	 * 
	 * @param reason
	 *            The reason of throwing this exception
	 * @param connectionType
	 *            The requested connection type
	 * @param objects
	 *            the objects on which a connection was requested
	 */
	public ConnectionCannotBeObtained(String reason, Class<?> connectionType, Object... objects) {
		super("No connection of type " + connectionType.getSimpleName() + 
//				" can be obtained for objects: " + Arrays.toString(objects) + 
				"\n The reason given is: " + reason + "\n "
				+ getDoesntExistMessage(connectionType))
				;
		this.connectionType = connectionType;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 224232280740635702L;

	private static String getDoesntExistMessage(Class<?> connectionType) {
		if (connectionType == null) {
			return "";
		}
		String s = "";
		ConnectionDoesntExistMessage message = connectionType.getAnnotation(ConnectionDoesntExistMessage.class);
		if (message != null) {
			s = message.message();
		}
		if ((connectionType != Connection.class) && (connectionType.getSuperclass() != null)
				&& (Connection.class.isAssignableFrom(connectionType.getSuperclass()))) {
			s = getDoesntExistMessage(connectionType.getSuperclass()) + " \n " + s;
		}
		for (Class<?> sup : connectionType.getInterfaces()) {
			if (Connection.class.isAssignableFrom(sup)) {
				s = getDoesntExistMessage(connectionType.getSuperclass()) + " \n " + s;
			}
		}
		return s;

	}

}
