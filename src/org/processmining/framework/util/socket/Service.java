package org.processmining.framework.util.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import org.processmining.framework.plugin.events.Logger.MessageLevel;

/**
 * This is a wrapper for a server socket. It has an arbitrary number of
 * registered ServiceHandler-s. Whenever a client connects to the service, all
 * registered handlers are notified. Each handler can further communucate with
 * the client.
 * 
 * @author christian
 * 
 */

public class Service {

	protected ServerSocket serverSocket; // server socket
	private final int port; // port for the server socket
	protected List<ServiceHandler> handlers = new ArrayList<ServiceHandler>(); // registered handlers

	/**
	 * The only constructor, which simply sets a port to be used by the server
	 * socket. Note that the constructor does not open the server socket.
	 * 
	 * @param port
	 *            for the server socket.
	 */
	public Service(int port) {
		super();
		this.port = port;
		serverSocket = null;
	}

	/**
	 * Registers a new handler. From this moment, the handler will be notified
	 * about new client connections.
	 * 
	 * @param handler
	 *            to be registered.
	 */
	public void addServiceHandler(ServiceHandler handler) {
		handlers.add(handler);
	}

	/**
	 * Unregister the handler. From this moment, the handler will not longer be
	 * notified about new client connections.
	 * 
	 * @param handler
	 *            to be unregistered.
	 */
	public void removeServiceHandler(ServiceHandler handler) {
		handlers.remove(handler);
	}

	/**
	 * Opens the server socked on the given port. Periodically notifies the
	 * environment that the service is alive. Notifies all registered handlers
	 * about every new client connection. It keeps on listening on the socket
	 * until the environment is canceled, upon which the socket is closed.
	 * 
	 * @param environment
	 *            is the environment of this service.
	 * @param executor
	 *            enables notification of handlers in a new thread.
	 * @throws IOException
	 */
	public void start(ServiceEnvironment environment, Executor executor) throws IOException {
		serverSocket = new ServerSocket(getPort());

		while (!environment.isCancelled()) {
			try {
				environment.stillAlive();
				// wait for and handle incoming connections
				serverSocket.setSoTimeout(500);
				Socket clientSocket = serverSocket.accept();
				handleConnection(clientSocket, environment, executor);
			} catch (SocketTimeoutException e) {
				// environment.log("No Connection Accepted", MessageLevel.DEBUG);
				continue;
			} catch (IOException e) {
				// abort and give up
				environment.log("Operational Support Server Failed: " + e.getMessage(), MessageLevel.ERROR);
				serverSocket.close();
				throw e;
			}
		}
		serverSocket.close();

		environment.log("Operational Support Server Finished", MessageLevel.DEBUG);
	}

	/**
	 * Notifies all registered handlers about every new client connection.
	 * 
	 * @param socket
	 *            is the client socket.
	 * @param environment
	 *            is the environment of this service.
	 * @param executor
	 *            enables notification of handlers in a new thread.
	 */
	protected void handleConnection(Socket socket, ServiceEnvironment environment, Executor executor) {
		ConnectionHandlerRunnable handler = new ConnectionHandlerRunnable(socket, environment);
		executor.execute(handler);
	}

	/**
	 * Get the server port.
	 * 
	 * @return the port.
	 */
	public int getPort() {
		return port;
	}

	/**
	 * A simple class that enables notification of ServiceHandler-s in a new
	 * thread.
	 * 
	 * @author christian
	 * 
	 */
	protected class ConnectionHandlerRunnable implements Runnable {

		protected Socket clientSocket;
		private final ServiceEnvironment environment;

		public ConnectionHandlerRunnable(Socket aSocket, ServiceEnvironment environment) {
			clientSocket = aSocket;
			this.environment = environment;
		}

		/**
		 * Notifies all registered handlers about a new client connection in a
		 * new thread.
		 */
		public void run() {
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
				for (ServiceHandler handler : handlers) {
					handler.handleRequest(environment, in, out);
				}
			} catch (IOException e) {
				// abort and give up

				environment.log("Fatal error handling connection,", MessageLevel.ERROR);
				environment.log("failed with IO Exception:", MessageLevel.ERROR);
				environment.log(e);
			} finally {
				// clean up connection
				try {
					if (clientSocket != null) {
						clientSocket.close();
					}
				} catch (IOException ie) { /* this one's forsaken.. */
				}
			}
		}
	}

}
