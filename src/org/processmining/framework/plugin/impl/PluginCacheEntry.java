package org.processmining.framework.plugin.impl;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.processmining.framework.boot.Boot;
import org.processmining.framework.boot.Boot.Level;
import org.processmining.framework.packages.PackageDescriptor;

public class PluginCacheEntry {

	//	private byte[] buffer = new byte[2 * 1024 * 1024];

	private static final String CURRENT_VERSION = "currentversion";

	private static final String FILE_PROTOCOL = "file";

	private static final Set<String> STANDARD_JRE_DIRS = new HashSet<String>(
			Arrays.asList(new String[] { "jdk", "jre", }));
	private static final String STANDARD_JRE_LIB_DIR = "lib";
	private static final String STANDARD_JRE_EXT_DIR = "ext";
	private static final Set<String> STANDARD_JAR_FILES = new HashSet<String>(Arrays.asList(new String[] {
			"resources.jar", "rt.jar", "jsse.jar", "jce.jar", "charsets.jar", "dnsns.jar", "localedata.jar",
			"qtjava.jar", "sunjce_provider.jar", "sunmscapi.jar", "sunpkcs11.jar" }));

	private final URL url;
	private boolean inCache;
	private Set<String> classNames;
	private String key;
	private Boot.Level verbose;

	private final PackageDescriptor packageDescriptor;

	private final String jarName;

	private static Preferences PACKAGECACHE = initCache();

	//	private static MessageDigest digest;
	//
	//	static {
	//		try {
	//			digest = MessageDigest.getInstance("MD5");
	//		} catch (NoSuchAlgorithmException e) {
	//			// no MD5 available, so we cannot reliably detect whether the JAR is
	//			// cached or not
	//			digest = null;
	//		}
	//	}

	/**
	 * Deprecated. Use the version with the package descriptor for a
	 * significantly faster cache lookup
	 * 
	 * @param url
	 * @param verbose
	 */
	@Deprecated
	public PluginCacheEntry(URL url, Boot.Level verbose) {
		this(url, verbose, null);
	}

	private static Preferences initCache() {
		return Preferences.userNodeForPackage(PluginCacheEntry.class).parent().node("_plugincache");
	}

	public PluginCacheEntry(URL url, Boot.Level verbose, PackageDescriptor packageDescriptor) {
		this.url = url;
		this.verbose = verbose;
		this.packageDescriptor = packageDescriptor;
		this.jarName = packageDescriptor == null ? url.toString().substring(url.toString().lastIndexOf('/') + 1)
				.toLowerCase() : packageDescriptor.getName().toLowerCase();
		reset();

		try {
			init();
		} catch (Throwable e) {
			System.err.println("Error caching JAR file: " + e.getMessage());
			reset();
		}
	}

	public String getKey() {
		return key;
	}

	private void reset() {
		inCache = false;
		classNames = new HashSet<String>();
		key = null;
	}

	public boolean isInCache() {
		return inCache;
	}

	public Set<String> getCachedClassNames() {
		return classNames;
	}

	public void removeFromCache() {
		if (key != null) {
			getSettings().remove(key);
		}
	}

	private void init() throws URISyntaxException {
		if (!url.getProtocol().equals(FILE_PROTOCOL)) {
			return;
		}

		if (isStandardJar()) {
			inCache = true;
			return;
		}

		if (packageDescriptor == null) {
			//			if (digest == null) {
			//				return;
			//			}
			key = createFileBasedKey(new File(url.toURI()));
			if (key == null) {
				return;
			}
		} else {
			key = createPackageBasedKey();
		}

		String names = getSettings().get(key, null);

		if (names == null) {
			return;
		}

		if (verbose == Level.ALL) {
			System.out.println("Plugins found in cache. ");
		}

		try {
			int subkeys = Integer.parseInt(names);
			for (int i = 0; i < subkeys; i++) {
				parseKey(key + "-" + i);
			}

		} catch (NumberFormatException e) {
			parseKey(key);
		}
		inCache = true;
	}

	private String createPackageBasedKey() {
		assert packageDescriptor != null;
		String key;
		key = packageDescriptor.getName();
		key += " ";
		key += packageDescriptor.getVersion();
		return key.toLowerCase();
	}

	private String createFileBasedKey(File file) {
		//		InputStream is = null;
		//		try {
		long modified = file.lastModified();

		key = Long.toHexString(modified);

		//			int numRead = 0;
		//
		//			is = url.openStream();
		//			while ((numRead = is.read(buffer)) > 0) {
		//				digest.update(buffer, 0, numRead);
		//			}

		//		} catch (IOException e) {
		//			return null;
		//		} finally {
		//			if (is != null) {
		//				try {
		//					is.close();
		//				} catch (IOException e) {
		//
		//				}
		//			}
		//		}

		//		key = "";
		//		for (byte b : digest.digest()) {
		//			// append the signed byte as an unsigned hex number
		//			key += Integer.toString(0xFF & b, 16);
		//		}

		// No need to put the jarName in the key anymore.
		//		key += " " + new File(new URI(url.toString())).getName();
		//		if (key.length() > 80) {
		//			// make sure they is not too long for the preferences API
		//			key = key.substring(0, 80);
		//		}
		return key;
	}

	private void parseKey(String key) {
		String names = getSettings().get(key, null);

		//System.out.println("  -> FOUND IN CACHE");
		for (String className : names.split("/")) {
			if (className.length() > 0) {
				//System.out.println("     - " + className);
				classNames.add(className);
			}
		}
	}

	private boolean isStandardJar() throws URISyntaxException {
		try {
			File file = new File(new URI(url.toString()));
			String filename = file.getName().toLowerCase();

			if (STANDARD_JAR_FILES.contains(filename)) {
				String libDir = file.getParentFile().getName().toLowerCase();
				String jreDir = removeNonAlphaChars(file.getParentFile().getParentFile().getName().toLowerCase());

				if (libDir.equals(STANDARD_JRE_EXT_DIR)) {
					libDir = file.getParentFile().getParentFile().getName().toLowerCase();
					jreDir = removeNonAlphaChars(file.getParentFile().getParentFile().getParentFile().getName()
							.toLowerCase());
				}
				if (libDir.equals(STANDARD_JRE_LIB_DIR)) {
					return STANDARD_JRE_DIRS.contains(jreDir);
				}
			}
		} catch (NullPointerException e) {
			// probably the file doesn't have enough parent paths
		}
		return false;
	}

	private String removeNonAlphaChars(String s) {
		String result = "";

		for (int i = 0; i < s.length(); i++) {
			if (('a' <= s.charAt(i)) && (s.charAt(i) <= 'z')) {
				result += s.substring(i, i + 1);
			}
		}
		return result;
	}

	public void update(List<String> classes) {
		if (key != null) {
			String newKey = key;
			//			if (packageDescriptor == null) {
			//				newKey = createKey();
			//				if (newKey == null) {
			//					return;
			//				}
			//			} else {
			//				newKey = createPackageBasedKey();
			//			}

			if (verbose == Level.ALL) {
				System.out.println("UPDATING CACHE: " + key);
			}

			// updating. Remove the previpous version if present and add the new classes

			String previous = getSettings().get(CURRENT_VERSION, null);
			if (previous != null) {
				TreeSet<String> installed = new TreeSet<>(Arrays.asList(previous.split("/")));
				Iterator<String> it = installed.iterator();
				if (installed.size() >= 5) {
					// already keeping 5 versions alive. Remove one if
					// current not already present.
					if (!installed.contains(newKey)) {
						String toRemove = it.next();
						getSettings().remove(toRemove);

					}
				}
				previous = newKey;
				while (it.hasNext()) {
					previous += '/';
					previous += it.next();
				}
				getSettings().put(CURRENT_VERSION, previous);

			} else {
				getSettings().put(CURRENT_VERSION, newKey);
			}

			classNames.clear();
			for (String name : classes) {
				if ((name != null) && (name.length() > 0)) {
					classNames.add(name);
				}
			}

			StringBuffer value = new StringBuffer("");
			for (String name : classNames) {
				if (verbose == Level.ALL) {
					System.out.println("               : " + name);
				}
				value.append(name);
				value.append("/");
			}

			if (value.length() > Preferences.MAX_VALUE_LENGTH) {
				int subkeys = (value.length() / Preferences.MAX_VALUE_LENGTH) + 1;
				getSettings().put(key, "" + subkeys);
				for (int i = 0; i < subkeys; i++) {
					getSettings().put(
							key + "-" + i,
							value.substring(i * Preferences.MAX_VALUE_LENGTH,
									Math.min((i + 1) * Preferences.MAX_VALUE_LENGTH, value.length())));
				}
			} else {

				getSettings().put(key, value.toString());
			}
		}
	}

	/**
	 * If a package descriptor is given, we use that to build the cache. The
	 * version number is increased automatically now with every build/release,
	 * hence we can use that to determine the cache.
	 * 
	 * @return
	 */
	private Preferences getSettings() {

		//		String className = getClass().getName();
		//		int pkgEndIndex = className.lastIndexOf('.');
		//		if (pkgEndIndex < 0) {
		//			className = "/<unnamed>";
		//		} else {
		//			String packageName = className.substring(0, pkgEndIndex);
		//			className = "/" + packageName.replace('.', '/');
		//		}
		if (packageDescriptor == null) {
			//			return Preferences.userRoot().node(className + "/_jarfiles/" + jarName);
			return PACKAGECACHE.node("_jarfiles/" + jarName);

		} else {
			return PACKAGECACHE.node(jarName);
			//			return Preferences.userRoot().node(className + '/' + jarName);
		}
	}

	/**
	 * Clear the cache here.
	 * 
	 * @return
	 */
	public static void clearSettingsCache() throws BackingStoreException {
		Preferences node = Preferences.userNodeForPackage(PluginCacheEntry.class);
		node.removeNode();
		node.flush();
		PACKAGECACHE.removeNode();
		PACKAGECACHE.flush();
		PACKAGECACHE = initCache();
	}

}
