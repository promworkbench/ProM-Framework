package org.processmining.framework.boot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;
import java.util.prefs.Preferences;

import org.processmining.framework.packages.PackageDescriptor;
import org.processmining.framework.packages.PackageManager;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.PluginManager;
import org.processmining.framework.plugin.annotations.Bootable;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginQuality;
import org.processmining.framework.plugin.impl.PluginManagerImpl;
import org.processmining.framework.util.CommandLineArgumentList;
import org.processmining.framework.util.OsUtil;
import org.processmining.framework.util.PathHacker;

public class Boot {

	public static enum Level {
		NONE, ERROR, ALL

	}

	public static String PROM_VERSION;
	public static String RELEASE_PACKAGE;
	public static String LIB_FOLDER;
	public static String IMAGES_FOLDER;
	public static String PROM_USER_FOLDER;
	public static String MACRO_FOLDER;
	public static String PACKAGE_FOLDER;
	public static String WORKSPACE_FOLDER;
	public static PluginQuality PLUGIN_QUALITY_THRESHOLD;
	public static PluginLevel PLUGIN_LEVEL_THRESHOLD;
	public static Level VERBOSE;
	public static URL DEFAULT_REPOSITORY;
	public static int OPENXES_SHADOW_SIZE;
	public static int CONNECT_TIMEOUT;
	public static int READ_TIMEOUT;

	public static boolean HIDE_OLD_PACKAGES;
	public static boolean CHECK_PACKAGES;

	public static String AUTO_UPDATE;

	public final static String LAST_RELEASE_AUTOINSTALLED_KEY = "last_release_autoinstalled";
	public static final String LAST_RELEASE_PACKAGE_KEY = "last_release_package_installed";
	public final static String TRACKING_BY_GA_ALLOWED = "tracking_by_GA_allowed";

	/**
	 * Versions of ProM.Lite should have a PROM_VERSION in the ini file that
	 * identifies the specific version. This should be prefixed by the
	 * LITE_PREFIX
	 */
	public static final String LITE_PREFIX = ".Lite";

	static {

		/*
		 * Preset defaults, just in case something fails down th eroad.
		 */
		PROM_VERSION = "";
		RELEASE_PACKAGE = "RunnerUpPackages";
		VERBOSE = Level.NONE;
		LIB_FOLDER = "lib";
		IMAGES_FOLDER = "images";
		MACRO_FOLDER = "macros";
		try {
			DEFAULT_REPOSITORY = new URL("http://www.promtools.org/prom6/packages/packages.xml");
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}
		PROM_USER_FOLDER = System.getProperty("user.home", "") + File.separator + ".ProM";
		PACKAGE_FOLDER = PROM_USER_FOLDER + File.separator + "packages";
		WORKSPACE_FOLDER = PROM_USER_FOLDER + File.separator + "workspace";
		HIDE_OLD_PACKAGES = false;
		CHECK_PACKAGES = false;
		PLUGIN_QUALITY_THRESHOLD = PluginQuality.VeryPoor;
		PLUGIN_LEVEL_THRESHOLD = PluginLevel.Local;
		CONNECT_TIMEOUT = 100;
		READ_TIMEOUT = 1000;

		Properties ini = new Properties();
		FileInputStream is;
		try {
			is = new FileInputStream("ProM.ini");

			try {
				ini.load(is);
				is.close();

				if (!ini.containsKey("PROM_VERSION") || !ini.containsKey("RELEASE_PACKAGE")) {
					throw new RuntimeException("Error while reading ProM.ini file, missing required"
							+ " keys PROM_VERSION and or RELEASE_PACKAGE . Exiting ProM.");
				}

				PROM_VERSION = ini.getProperty("PROM_VERSION");
				if (!PROM_VERSION.contains(".")) {
					PROM_VERSION = "";
				}

				RELEASE_PACKAGE = ini.getProperty("RELEASE_PACKAGE");

				//		OPENXES_SHADOW_SIZE = Integer.parseInt(ini.getProperty("OPENXES_SHADOW_SIZE", "4"));
				//		NikeFS2FileAccessMonitor.instance(OPENXES_SHADOW_SIZE);

				CONNECT_TIMEOUT = Integer.parseInt(ini.getProperty("CONNECT_TIMEOUT", "100"));
				READ_TIMEOUT = Integer.parseInt(ini.getProperty("READ_TIMEOUT", "1000"));

				try {
					VERBOSE = Level.valueOf(ini.getProperty("VERBOSE", Level.ALL.name()));
				} catch (IllegalArgumentException e) {
					if (e.getMessage().toLowerCase().endsWith(".true")) {
						VERBOSE = Level.ALL;
					} else if (e.getMessage().toLowerCase().endsWith(".false")) {
						VERBOSE = Level.NONE;
					} else {
						throw e;
					}
				}

				LIB_FOLDER = ini.getProperty("LIB_FOLDER", "lib").replace("/", File.separator);
				PathHacker.addLibraryPathFromDirectory(new File("." + File.separator + LIB_FOLDER));

				IMAGES_FOLDER = LIB_FOLDER + File.separator
						+ ini.getProperty("IMAGES_FOLDER", "images").replace("/", File.separator);
				PathHacker.addLibraryPathFromDirectory(new File("." + File.separator + IMAGES_FOLDER));

				MACRO_FOLDER = LIB_FOLDER + File.separator
						+ ini.getProperty("MACRO_FOLDER", "macros").replace("/", File.separator);
				PathHacker.addLibraryPathFromDirectory(new File("." + File.separator + MACRO_FOLDER));

				try {
					DEFAULT_REPOSITORY = new URL(ini.getProperty("PACKAGE_URL",
							"http://www.promtools.org/prom6/packages" + PROM_VERSION.replaceAll("\\.", "")
									+ "/packages.xml"));
				} catch (MalformedURLException e) {
					try {
						DEFAULT_REPOSITORY = new URL("http://www.promtools.org/prom6/packages"
								+ PROM_VERSION.replaceAll("\\.", "") + "/packages.xml");
					} catch (MalformedURLException e1) {
						assert (false);
					}
				}

				String prom_user_folder = ini.getProperty("PROM_USER_FOLDER", "").replace("/", File.separator);
				if (prom_user_folder.equals("")) {
					PROM_USER_FOLDER = System.getProperty("user.home", "") + File.separator + ".ProM"
							+ PROM_VERSION.replaceAll("\\.", "");
				} else {
					PROM_USER_FOLDER = prom_user_folder;
				}

				PACKAGE_FOLDER = PROM_USER_FOLDER + File.separator
						+ ini.getProperty("PACKAGE_FOLDER", "packages").replace("/", File.separator);

				WORKSPACE_FOLDER = PROM_USER_FOLDER + File.separator
						+ ini.getProperty("WORKSPACE_FOLDER", "workspace").replace("/", File.separator);

				HIDE_OLD_PACKAGES = new Boolean(ini.getProperty("HIDE_OLD_PACKAGES", "false"));
				CHECK_PACKAGES = new Boolean(ini.getProperty("CHECK_PACKAGES", "false"));
				AUTO_UPDATE = new String(ini.getProperty("AUTO_UPDATE", "never"));

				PLUGIN_QUALITY_THRESHOLD = PluginQuality.VeryPoor;
				String threshold = ini.getProperty("PLUGIN_QUALITY_THRESHOLD", PLUGIN_QUALITY_THRESHOLD.getName());
				for (PluginQuality quality : PluginQuality.values()) {
					if (quality.getName().equals(threshold)) {
						PLUGIN_QUALITY_THRESHOLD = quality;
						break;
					}
				}

				PLUGIN_LEVEL_THRESHOLD = PluginLevel.Local;
				threshold = ini.getProperty("PLUGIN_LEVEL_THRESHOLD", PLUGIN_LEVEL_THRESHOLD.getName());
				for (PluginLevel level : PluginLevel.values()) {
					if (level.getName().equals(threshold)) {
						PLUGIN_LEVEL_THRESHOLD = level;
						break;
					}
				}

				if (VERBOSE == Level.ALL) {
					System.out.println("Plug-in level threshold set to " + PLUGIN_LEVEL_THRESHOLD.getName());
					System.out.println("Plug-in quality threshold set to " + PLUGIN_QUALITY_THRESHOLD.getName());
					System.out.println("Ini file processed");
				}
			} catch (IOException e) {
				//					throw new RuntimeException("Error while reading ProM.ini file. Exiting ProM.", e);
				if (VERBOSE != Level.NONE) {
					System.err.println("Error while reading ProM.ini file.\n" + e + "\nReverting to default settings.");
				}
			}
		} catch (FileNotFoundException e) {
			//				throw new RuntimeException("ProM.ini file not found. Exiting ProM.", e);
			if (VERBOSE != Level.NONE) {
				System.err.println("ProM.ini file not found.\n" + e + "\nReverting to default settings.");
			}
		}

	}

	public static boolean isLatestReleaseInstalled() {
		return Preferences.userNodeForPackage(Boot.class).get(LAST_RELEASE_AUTOINSTALLED_KEY, "").equals(PROM_VERSION)
				&& Preferences.userNodeForPackage(Boot.class).get(LAST_RELEASE_PACKAGE_KEY, "").equals(RELEASE_PACKAGE);
	}

	public static void setReleaseInstalled(String version, String releasePackage) {
		Preferences.userNodeForPackage(Boot.class).put(LAST_RELEASE_AUTOINSTALLED_KEY, version);
		Preferences.userNodeForPackage(Boot.class).put(LAST_RELEASE_PACKAGE_KEY, releasePackage);
	}

	public static void setLatestReleaseInstalled() {
		setReleaseInstalled(PROM_VERSION, RELEASE_PACKAGE);
	}

	public static boolean isTrackingByGAAllowed() {
		return Preferences.userNodeForPackage(Boot.class).get(TRACKING_BY_GA_ALLOWED, "false").equals("true");
	}
	
	public static void boot(Class<?> bootClass, Class<? extends PluginContext> pluginContextClass, String... args)
			throws Exception {
		long start = System.currentTimeMillis();
		// First instantiate the package manager
		PackageManager packages = PackageManager.getInstance();
		// Then the plugin manager, as it listens to the package manager
		PluginManagerImpl.initialize(pluginContextClass);
		PluginManager plugins = PluginManagerImpl.getInstance();

		OsUtil.setWorkingDirectoryAtStartup();

		long startPackages = System.currentTimeMillis();
		packages.initialize(VERBOSE);
		if (VERBOSE == Level.ALL) {
			System.out.println(">>> Scanning for packages took " + (System.currentTimeMillis() - startPackages)
					/ 1000.0 + " seconds");
		}

		long startPlugins = System.currentTimeMillis();
		URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		URL[] defaultURLs = sysloader.getURLs();

		//	new URLClassLoader(new URL[] { packages.getPackagesDirectory().toURL() });

		if (VERBOSE == Level.ALL) {
			for (URL def : defaultURLs) {
				System.out.println("known jar file: " + def);
			}
			System.out.println("Loading plugins from packages.");
		}

		for (PackageDescriptor pack : packages.getEnabledPackages()) {
			if (VERBOSE == Level.ALL) {
				System.out.println("Processing Package: " + pack.getName());
			}
			addJarsForPackage(pack, VERBOSE, plugins);
		}

		if (VERBOSE == Level.ALL) {
			System.out.println("Loading from: classpath");
		}

		File f = new File("." + File.separator + LIB_FOLDER);
		String libPath = f.getCanonicalPath();
		addURLToClasspath(f.toURI().toURL());
		for (URL url : defaultURLs) {
			if (VERBOSE == Level.ALL) {
				System.out.println("Processing url: " + url);
			}
			if (!(new File(url.toURI()).getCanonicalPath().startsWith(libPath))) {
				if (VERBOSE == Level.ALL) {
					System.out.println("Scanning for plugins: " + url);
				}
				plugins.register(url, null);
			} else {
				if (VERBOSE == Level.ALL) {
					System.out.println("Skipping: " + url.getFile() + " while scanning for plugins.");
				}
			}
		}

		if (VERBOSE == Level.ALL) {
			System.out.println(">>> Scanning for plugins took " + (System.currentTimeMillis() - startPlugins) / 1000.0
					+ " seconds");
		}

		boot(bootClass, args);

		if (VERBOSE == Level.ALL) {
			System.out.println(">>> Total startup took " + (System.currentTimeMillis() - start) / 1000.0 + " seconds");
		}
	}

	/*
	 * (non-Javadoc) * @see
	 * org.processmining.framework.plugin.PluginManager#boot(java.lang.Class,
	 * java.lang.String[])
	 */
	public static Object boot(Class<?> bootClass, String... args) throws Exception {
		Method bootMethod = null;

		for (Method method : bootClass.getMethods()) {
			if (method.isAnnotationPresent(Bootable.class)) {
				if (bootMethod == null) {
					bootMethod = method;
				} else {
					throw new IllegalArgumentException("Cannot have more than one @Bootable method in a class");
				}
			}
		}
		if (bootMethod == null) {
			throw new IllegalArgumentException("No @Bootable annotation found: " + bootClass.getName());
		}

		CommandLineArgumentList argList = new CommandLineArgumentList();
		for (String arg : args) {
			argList.add(arg);
		}

		return bootMethod.invoke(bootMethod.getDeclaringClass().newInstance(), argList);
	}

	public static void addJarsForPackage(PackageDescriptor pack, Boot.Level verbose, PluginManager plugins) {
		if (verbose == Level.ALL) {
			System.out.println("Scanning package: " + pack);
		}
		File dir = pack.getLocalPackageDirectory();
		if (!dir.exists() || !dir.isDirectory() || !dir.canRead()) {
			if (verbose == Level.ALL) {
				System.out.println("  Error: package directory does not exist: " + dir);
			}
			return;
		}
		// First, recusively iterate subfolders, where no scanning for plugins is necessary
		// this ensures all requires libraries are known when scanning for plugins
		for (File f : dir.listFiles()) {
			// Scan for jars. Only jars in the root of the package will be scanned for
			// plugins and other annotations.
			if (f.isDirectory()) {
				addJarsFromPackageDirectory(f, verbose, plugins);
				try {
					addURLToClasspath(f.toURI().toURL());
				} catch (MalformedURLException e) {
				}
			}
		}
		// Now scan the jar files in the package root folder.
		for (File f : dir.listFiles()) {
			if (f.getAbsolutePath().endsWith(PluginManager.JAR_EXTENSION)) {
				URL url;
				try {
					url = f.toURI().toURL();
					if (verbose == Level.ALL) {
						System.out.println("  scanning for plugins: " + url);
					}
					addURLToClasspath(url);
					if (f.getAbsolutePath().endsWith(PluginManager.JAR_EXTENSION)) {
						plugins.register(url, pack);
					}
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}

		}
		PathHacker.addLibraryPathFromDirectory(pack.getLocalPackageDirectory());
		try {
			PathHacker.addJar(pack.getLocalPackageDirectory().toURI().toURL());
			for (File f : pack.getLocalPackageDirectory().listFiles()) {
				if (f.isDirectory()) {
					PathHacker.addJar(f.toURI().toURL());
				}
			}
		} catch (MalformedURLException e) {
			assert (false);
		}

	}

	/**
	 * Scan for jars and add them to the classpath.
	 * 
	 * @param dir
	 *            the folder (or jar file) to scan
	 * @param verbose
	 *            true if output required
	 * @param plugins
	 *            the plugin manager
	 * @param scanClasses
	 *            If true, then all classes are scanned for annotations and for
	 *            plugins. This property recusively propagates to sub-folders.
	 */
	private static void addJarsFromPackageDirectory(File dir, Boot.Level verbose, PluginManager plugins) {

		for (File f : dir.listFiles()) {
			if (f.isDirectory()) {
				addJarsFromPackageDirectory(f, verbose, plugins);
			}
		}
		for (File f : dir.listFiles()) {
			if (f.getAbsolutePath().endsWith(PluginManager.JAR_EXTENSION)) {
				try {
					URL url = f.toURI().toURL();
					if (verbose == Level.ALL) {
						System.out.println("  adding to classpath: " + url);
					}
					addURLToClasspath(url);
				} catch (MalformedURLException e) {
					e.printStackTrace();

				}
			}
		}
	}

	private static void addURLToClasspath(URL url) {
		try {
			URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
			Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class<?>[] { URL.class });

			method.setAccessible(true);
			method.invoke(sysloader, new Object[] { url });
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}
