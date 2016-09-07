package org.processmining.framework.plugin;

/**
 * Thrown when a plugin returns an object of the wrong type.
 * 
 * @author bfvdonge
 * 
 */
public class IncorrectReturnTypeException extends RuntimeException {

	private static final long serialVersionUID = 8553687837509819311L;

	public IncorrectReturnTypeException(String plugin, int index, Class<?> expected, Class<?> returned) {
		super("Plugin " + plugin + " produced the wrong type of result at index " + index + ": Expected: " + expected
				+ ", but received: " + (returned == null ? "nothing" : returned) + ".");
	}
}
