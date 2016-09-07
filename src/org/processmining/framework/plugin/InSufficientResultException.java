package org.processmining.framework.plugin;

/**
 * Thrown when a plugin returns less results than declared.
 * 
 * @author bfvdonge
 * 
 */
public class InSufficientResultException extends RuntimeException {

	private static final long serialVersionUID = -1416206990218147728L;

	public InSufficientResultException(String plugin, int expected, int returned) {
		super("Plugin " + plugin + " produced " + returned + " results, while " + expected + " results were declared.");
	}

}
