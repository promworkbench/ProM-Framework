package org.processmining.framework.connections;

/**
 * Tagger interface to indicate that a connection will change after
 * instantiation. Since the actual objects involved in the connection are not
 * allowed to change, these changes should be limited to the contents of the
 * connection itself.
 * 
 * The main use for this interface is in serialization, where DynamicConnections
 * are only serialized when ProM is closed.
 * 
 * @author bfvdonge
 * 
 */
public interface DynamicConnection {

}
