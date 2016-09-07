package org.processmining.framework.packages;

public class UnknownPackageException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1037754203430229972L;
	private final String name;

	public UnknownPackageException(String name) {
		super("Unknow package: " + name);
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
