package org.processmining.framework.packages;

public class UnknownPackageTypeException extends Exception {

	private static final long serialVersionUID = -7248277354439798414L;
	private final PackageDescriptor pack;

	public UnknownPackageTypeException(PackageDescriptor pack) {
		super("Unknow package file type: " + pack.getURL());
		this.pack = pack;
	}

	public PackageDescriptor getPack() {
		return pack;
	}
}
