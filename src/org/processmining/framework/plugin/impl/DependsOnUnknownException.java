package org.processmining.framework.plugin.impl;

/**
 * thrown by the constructor of a plugindescritor if it depends on a plugin not
 * yet in the system.
 * 
 * @author bfvdonge
 * 
 */
public class DependsOnUnknownException extends Exception {

	public DependsOnUnknownException(String message) {
		super(message);
	}

	private static final long serialVersionUID = 4219323628021104089L;

}
