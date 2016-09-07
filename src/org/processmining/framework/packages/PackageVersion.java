package org.processmining.framework.packages;

import java.util.ArrayList;
import java.util.List;

public class PackageVersion implements Comparable<PackageVersion> {

	private final List<Integer> version;

	public PackageVersion(String version) {
		this.version = parse(version);
	}

	private List<Integer> parse(String v) {
		List<Integer> result = new ArrayList<Integer>();
		String[] components = v.split("[^0-9]");

		for (String c : components) {
			if (c.length() > 0) {
				result.add(Integer.parseInt(c));
			}
		}
		return result;
	}

	public boolean equals(Object o) {
		if (!(o instanceof PackageVersion)) {
			return false;
		}
		return version.equals(((PackageVersion) o).version);
	}

	public int hashCode() {
		return version.hashCode();
	}

	public String toString() {
		String result = new String();

		for (Integer i : version) {
			if (result.length() > 0) {
				result += '.';
			}
			result += i;
		}
		return result;
	}

	public boolean lessOrEqual(PackageVersion v) {
		return compareTo(v) <= 0;
	}

	public boolean lessThan(PackageVersion v) {
		return compareTo(v) < 0;
	}

	public int compareTo(PackageVersion v) {
		for (int i = 0; i < version.size(); i++) {
			if (i < v.version.size()) {
				int self = version.get(i);
				int other = v.version.get(i);

				if (self < other) {
					return -1;
				} else if (self > other) {
					return 1;
				}
			} else {
				return 1;
			}
		}
		return version.size() == v.version.size() ? 0 : -1;
	}
}
