package org.processmining.framework.util.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * This interface enables a service to respond to newly connected clients. If a
 * class wants to be informed about and/or respond to new requests of a service,
 * then this class should implement this interface and it should be added as a
 * handler to the service.
 * 
 * @author christian
 */

public interface ServiceHandler {
	/**
	 * Invoked whenever a client connects to the service.
	 * 
	 * @param environment
	 *            is the registered environment of the service. A handler can
	 *            use this parameter to, for example, log messages.
	 * @param in
	 *            is the buffer from which the client's request can be read.
	 * @param out
	 *            is the buffer where a possible response to the client can be
	 *            written.
	 * @throws IOException
	 */
	public void handleRequest(ServiceEnvironment environment, BufferedReader in, PrintWriter out) throws IOException;
}
