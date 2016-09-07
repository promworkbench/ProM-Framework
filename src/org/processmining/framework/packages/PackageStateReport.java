package org.processmining.framework.packages;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

public class PackageStateReport {

	private final Map<PackageDescriptor, Set<String>> missing;
	private final Map<PackageDescriptor, Set<String>> conflicts;
	private final Map<String, SortedSet<PackageDescriptor>> multipleVersions;

	public PackageStateReport(Map<String, SortedSet<PackageDescriptor>> multipleVersions,
			Map<PackageDescriptor, Set<String>> missing, Map<PackageDescriptor, Set<String>> conflicts) {
		this.multipleVersions = multipleVersions;
		this.conflicts = conflicts;
		this.missing = missing;
	}

	public Map<PackageDescriptor, Set<String>> getMissingDependencies() {
		return missing;
	}

	public Map<PackageDescriptor, Set<String>> getConflictingPackages() {
		return conflicts;
	}

	public Map<String, SortedSet<PackageDescriptor>> getPackagesWithMultipleVersions() {
		return multipleVersions;
	}

	public String toString() {
		List<String> lines = new ArrayList<String>();

		for (Map.Entry<String, SortedSet<PackageDescriptor>> item : getPackagesWithMultipleVersions().entrySet()) {
			lines.add("Package " + item.getKey() + " has or will have multiple versions installed:");
			for (PackageDescriptor p : item.getValue()) {
				lines.add("  " + p);
			}
		}
		for (Map.Entry<PackageDescriptor, Set<String>> item : getMissingDependencies().entrySet()) {
			lines.add("Package " + item.getKey() + " depends on packages which are and will not be installed:");
			for (String dep : item.getValue()) {
				lines.add("  " + dep);
			}
		}
		for (Map.Entry<PackageDescriptor, Set<String>> item : getConflictingPackages().entrySet()) {
			lines.add("Package " + item.getKey() + " conflicts with packages which are or will be installed:");
			for (String confl : item.getValue()) {
				lines.add("  " + confl);
			}
		}

		StringBuffer result = new StringBuffer();
		for (String s : lines) {
			result.append(s);
			result.append(System.getProperty("line.separator"));
		}
		return result.toString();
	}

	public boolean hasErrors() {
		return !getConflictingPackages().isEmpty() || !getMissingDependencies().isEmpty()
				|| !getPackagesWithMultipleVersions().isEmpty();
	}
}
