package org.processmining.framework.util.socket;

import org.processmining.framework.plugin.events.Logger.MessageLevel;

/**
 * This interface represents a service environment. It allows communication
 * between a service and its environment.
 * 
 * @author Maja Pesic
 */

public interface ServiceEnvironment {

	/**
	 * Provides the information about the state of the environment. A service
	 * uses this method to decide when to close its socket.
	 * 
	 * @return true - if the socket should be closed false - if the service
	 *         should continue listening on the socket.
	 */
	public boolean isCancelled();

	/**
	 * A service uses this method to log messages in the environment.
	 * 
	 * @param message
	 *            is the message that should be logged
	 * @param level
	 *            is the type of message
	 */
	public void log(String message, MessageLevel level);

	/**
	 * A service uses this method to log messages in the environment.
	 * 
	 * @param message
	 *            is the message that should be logged
	 */
	public void log(String message);

	/**
	 * A service uses this method to log occurrence of an exception in the
	 * environment.
	 * 
	 * @param t
	 *            is the exception that occurred and should be logged
	 */
	public void log(Throwable t);

	/**
	 * While listening on its socket, a service will regularly invoke this
	 * method. This method periodically signals to the environment that the
	 * service is still listening to its socket.
	 */
	public void stillAlive();

	/**
	 * A service uses this method to signal that it has been canceled (i.e., the
	 * socket is closed).
	 */
	public void cancel();

}
