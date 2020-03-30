package org.processmining.framework.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class ProMClassLoader extends URLClassLoader {

	public ProMClassLoader(ClassLoader loader) {
		super(getClassPathURLs(), loader);
//		super(new URL[] { }, loader);
	}
	
	/*
	 * Returns the URLs of all (absolute) paths as found in the current class path.
	 */
	private static URL[] getClassPathURLs() {
		// Get the current class path.
		String classpath = System.getProperty("java.class.path");
		// Get the current path separator.
		String separator = System.getProperty("path.separator");
		// Separate the paths in the class path.
		String[] paths = classpath.split(separator);
		// Convert every entry into a URL. Assume absolute paths in class path.
		URL[] urls = new URL[paths.length];
		for (int i = 0; i < paths.length; i++) {
			try {
				urls[i] = new URL("file:///" + paths[i]);
				System.out.println("[PromClassLoader] found in classpath: " + urls[i]);
			} catch (MalformedURLException e) {
				System.err.println("[ProMClassLoader] " + e.getMessage());
			}
		}
		return urls;
	}
	
//	protected Class<?> findClass(String name) throws ClassNotFoundException {
//		System.err.println("[ProMClassLoader] Find class " + name);
//		return super.findClass(name);
//	}

//    @Override public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
//        System.out.println("[ProMClassLoader] Load class " + name);
//        return super.loadClass(name, resolve);
//    }
    
}
