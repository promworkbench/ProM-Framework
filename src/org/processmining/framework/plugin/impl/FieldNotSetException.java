package org.processmining.framework.plugin.impl;

public class FieldNotSetException extends Exception {

	private static final long serialVersionUID = 6942427887070071817L;

	public FieldNotSetException(String fieldName, String message) {
		super("Field " + fieldName + " was not yet set. " + message);
	}
}
