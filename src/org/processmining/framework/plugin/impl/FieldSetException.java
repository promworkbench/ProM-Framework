package org.processmining.framework.plugin.impl;

public class FieldSetException extends Exception {

	private static final long serialVersionUID = 1024657629198603241L;

	public FieldSetException(String fieldName, String message) {
		super("Field " + fieldName + " was already set. " + message);
	}
}
