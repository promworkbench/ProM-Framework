package org.processmining.framework.util;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;

/**
 * Dynamically adds jars or dll files at runtime to the environment for direct
 * use
 * 
 * @author T. van der Wiel
 * 
 */
public class PathHacker {
	@SuppressWarnings("unchecked")
	private static final Class[] parameters = new Class[] { URL.class };

	/**
	 * ensures the given path is in java.library.path
	 * 
	 * @param path
	 * @throws Exception
	 */
	public static void addLibPath(String path) throws Exception {
		File file = new File(path);
		// Append the specified path to the existing java.library.path (if there is one already)
		String newLibraryPath = System.getProperty("java.library.path");
		if ((newLibraryPath == null) || (newLibraryPath.length() < 1)) {
			newLibraryPath = file.getCanonicalPath();
		} else if (newLibraryPath.contains(file.getCanonicalPath())) {
			return;
		} else {
			newLibraryPath += File.pathSeparator + file.getCanonicalPath();
		}

		// Reflect into java.lang.System to get the static Properties reference
		Field f = System.class.getDeclaredField("props");
		f.setAccessible(true);
		Properties props = (Properties) f.get(null);
		// replace the java.library.path with our new one
		props.put("java.library.path", newLibraryPath);

		// The classLoader may have already been initialized, so it needs to be fixed up.
		// Reflect into java.lang.ClassLoader to get the static String[] of user paths to native libraries
		Field usr_pathsField = ClassLoader.class.getDeclaredField("usr_paths");
		usr_pathsField.setAccessible(true);
		String[] usr_paths = (String[]) usr_pathsField.get(null);
		String[] newUsr_paths = new String[usr_paths == null ? 1 : usr_paths.length + 1];
		if (usr_paths != null) {
			System.arraycopy(usr_paths, 0, newUsr_paths, 0, usr_paths.length);
		}
		// Add the specified path to the end of a new String[] of user paths to native libraries
		newUsr_paths[newUsr_paths.length - 1] = file.getAbsolutePath();
		usr_pathsField.set(null, newUsr_paths);
	}

	public static void addLibraryPathFromDirectory(File dir) {
		if (!dir.exists() || !dir.isDirectory() || !dir.canRead()) {
			return;
		}

		for (File f : dir.listFiles()) {
			if (f.isDirectory()) {
				try {
					addLibPath(f.getAbsolutePath());
				} catch (Exception e) {
					// Failed, no big deal
				}
				addLibraryPathFromDirectory(f);
			} else {
				//skip
			}
		}
	}

	//	/**
	//	 * tries to load the given file to the runtime environment
	//	 * @param file
	//	 * @throws IOException if an UnsatisfiedLinkError occurs
	//	 * @throws SecurityException if a security manager exists and its checkLink method doesn't allow loading of the specified dynamic library
	//	 * @throws UnsatisfiedLinkError if the file does not exist. 
	//	 * @throws NullPointerException if filename is null
	//	 */
	//	public static void addLib(String file) throws Exception {
	//		try {
	//        	Runtime.getRuntime().load(file);
	//		} catch(UnsatisfiedLinkError e) {
	//			throw new IOException("UnsatisfiedLinkError");
	//		}
	//	}

	public static void addJar(String s) {
		File f = new File(s);
		addJar(f);
	}

	public static void addJar(File f) {
		try {
			// File.toURL() was deprecated, so use File.toURI().toURL()
			addJar(f.toURI().toURL());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Adds the given URL to the classpath for direct use
	 * 
	 * @param URL
	 *            of the Java Archive
	 */
	public static void addJar(URL u) {
		URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		for (URL u2 : sysloader.getURLs()) {
			if (u.equals(u2)) {
				return;
			}
		}
		try {
			/* Class was uncheched, so used URLClassLoader.class instead */
			Method method = URLClassLoader.class.getDeclaredMethod("addURL", parameters);
			method.setAccessible(true);
			method.invoke(sysloader, new Object[] { u });
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
