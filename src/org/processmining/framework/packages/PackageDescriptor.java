package org.processmining.framework.packages;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.processmining.framework.util.OsUtil;

public class PackageDescriptor implements Comparable<PackageDescriptor> {

	public static enum OS {
		WIN64("win64", OsUtil.isRunningWindows() && OsUtil.is64Bit()), //
		WIN32("win32", OsUtil.isRunningWindows() && OsUtil.is32Bit()), //
		LIN32("linux32", OsUtil.isRunningLinux() && OsUtil.is32Bit()), //
		LIN64("linux64", OsUtil.isRunningLinux() && OsUtil.is64Bit()), //
		MAC("mac", OsUtil.isRunningMacOsX()), //
		ALL("all", true);

		private final String name;
		private final boolean usable;

		private OS(String name, boolean usable) {
			this.name = name;
			this.usable = usable;
		}

		public String getName() {
			return name;
		}

		public static OS fromString(String curPackageOS) {
			for (OS os : OS.values()) {
				if (os.name.equals(curPackageOS)) {
					return os;
				}
			}
			return ALL;
		}

		public boolean isUsable() {
			return usable;
		}
	}

	private final String name;
	private final PackageVersion version;
	private final String description;
	private final String organisation;
	private final String author;
	private final String license;
	private final String url;
	private final boolean autoInstalled;
	private final boolean hasPlugins;
	private final Set<String> dependencies;
	private final Set<String> conflicts;
	private boolean broken;
	private final String logoUrl;
	private final OS os;
	private final String maintainer;
	private final String keywords;

	public PackageDescriptor(String name, String version, OS os, String description, String organisation,
			String author, String maintainer, String license, String url, String logoUrl, String keywords,
			boolean autoInstalled, boolean hasPlugins, List<String> dependencies, List<String> conflicts) {
		this.name = name;
		this.os = os;
		this.maintainer = maintainer;
		this.logoUrl = logoUrl;
		this.autoInstalled = autoInstalled;
		this.hasPlugins = hasPlugins;
		this.version = new PackageVersion(version);
		this.description = description;
		this.organisation = organisation;
		this.author = author;
		this.license = license;
		this.url = url;
		this.keywords = keywords;
		this.dependencies = Collections.unmodifiableSet(new HashSet<String>(dependencies));
		this.conflicts = Collections.unmodifiableSet(new HashSet<String>(conflicts));
		broken = false;
	}

	@Override
	public String toString() {
		return name + "-" + version;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof PackageDescriptor)) {
			return false;
		}
		return name.equals(((PackageDescriptor) other).name) && //
				version.equals(((PackageDescriptor) other).version) && //
				os.equals(((PackageDescriptor) other).os);
	}

	@Override
	public int hashCode() {
		return name.hashCode() * 37 + version.hashCode();
	}

	public String getName() {
		return name;
	}

	public PackageVersion getVersion() {
		return version;
	}

	public String getURL() {
		return url;
	}

	public String getLogoURL() {
		return logoUrl;
	}

	public Set<String> getDependencies() {
		return dependencies;
	}

	public Set<String> getConflicts() {
		return conflicts;
	}

	public File getLocalPackageDirectory() {
		return new File(PackageManager.getInstance().getPackagesDirectory(), makeFilename(name + "-" + version));
	}

	private static String makeFilename(String filename) {
		return filename.toLowerCase().replaceAll("[^a-zA-Z0-9-.]", "_");
	}

	public void setHasBrokenDependencies() {
		broken = true;
	}

	public boolean hasBrokenDependencies() {
		return broken;
	}

	public String getDescription() {
		return description;
	}

	public String getOrganisation() {
		return organisation;
	}

	public String getAuthor() {
		return author;
	}

	public String getLicense() {
		return license;
	}

	public OS getOS() {
		return os;
	}

	public String getMaintainer() {
		return maintainer;
	}

	public String getKeywords() {
		return keywords;
	}

	public boolean getAutoInstalled() {
		return autoInstalled;
	}

	public boolean hasPlugins() {
		return hasPlugins;
	}

	public String toHTML() {
		StringBuffer buffer = new StringBuffer();

		buffer.append("<HTML><TABLE>");
		buffer.append("<TR><TD>Package:</TD><TD>" + description + "</TD></TR>");
		buffer.append("<TR><TD>Version:</TD><TD>" + version + "</TD></TR>");
		buffer.append("<TR><TD>Organisation:</TD><TD>" + organisation + "</TD></TR>");
		buffer.append("<TR><TD>Author(s):</TD><TD>" + author + "</TD></TR>");
		buffer.append("<TR><TD>Maintained by:</TD><TD>" + maintainer + "</TD></TR>");
		buffer.append("<TR><TD>License:</TD><TD>" + license + "</TD></TR>");

		buffer.append("<TR><TD>Dependencies:</TD>");
		if (dependencies.isEmpty()) {
			buffer.append("<TD>none</TD></TR>");
		} else {
			for (Iterator<String> it = dependencies.iterator(); it.hasNext();) {
				buffer.append("<TD>" + it.next() + "</TD></TR>");
				if (it.hasNext()) {
					buffer.append("<TR><TD> </TD>");
				}
			}
		}
		buffer.append("<TR><TD>Conflicts:</TD>");
		if (conflicts.isEmpty()) {
			buffer.append("<TD>none</TD></TR>");
		} else {
			for (Iterator<String> it = conflicts.iterator(); it.hasNext();) {
				buffer.append("<TD>" + it.next() + "</TD></TR>");
				if (it.hasNext()) {
					buffer.append("<TR><TD> </TD>");
				}
			}
		}

		buffer.append("</TABLE></HTML>");

		return buffer.toString();
	}

	public int compareTo(PackageDescriptor pack) {
		if (pack.name.equals(name)) {
			if (version.equals(pack.version)) {
				return os.compareTo(pack.os);
			}
			return -version.compareTo(pack.version);
		}
		return name.compareTo(pack.name);
	}
}
