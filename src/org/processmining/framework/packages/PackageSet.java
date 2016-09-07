package org.processmining.framework.packages;

import java.util.HashSet;

import org.processmining.framework.packages.PackageDescriptor.OS;

public class PackageSet extends HashSet<PackageDescriptor> {

	private static final long serialVersionUID = -5010658216636461231L;

	public boolean add(PackageDescriptor e) {
		if (e.getOS() == OS.WIN64) {
			// check for removal of Win32 version
			for (PackageDescriptor pack : this) {
				if (pack.getName().equals(e.getName()) && //
						pack.getVersion().equals(e.getVersion()) && //
						pack.getOS() == OS.WIN32) {
					remove(pack);
					break;
				}
			}
		} else {
			if (e.getOS() == OS.WIN32) {
				// Check if Win64 version already in the set
				for (PackageDescriptor pack : this) {
					if (pack.getName().equals(e.getName()) && //
							pack.getVersion().equals(e.getVersion()) && //
							pack.getOS() == OS.WIN64) {
						// win64 version already available. Don't add 32 bit version.
						return false;
					}
				}
			}
		}
		return super.add(e);
	}
}
