package org.processmining.framework.boot;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.processmining.framework.boot.Boot.Level;
import org.processmining.framework.packages.PackageDescriptor;
import org.processmining.framework.plugin.PluginManager;
import org.processmining.framework.util.PathHacker;

/**
 * Threaded implementation of the addJarsForPackage method
 * 
 * @author berti
 *
 */
public class AddJarsForPackageRunnable extends Thread {
	PackageDescriptor pack;
	Level verbose;
	PluginManager plugins;
	
	public AddJarsForPackageRunnable(PackageDescriptor pack, Level verbose, PluginManager plugins) {
		this.pack = pack;
		this.verbose = verbose;
		this.plugins = plugins;
	}
	
	/**
	 * Entry point for thread
	 */
	public void run() {
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
				Boot.addJarsFromPackageDirectory(f, verbose, plugins);
				try {
					Boot.addURLToClasspath(f.toURI().toURL());
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
					Boot.addURLToClasspath(url);
					if (f.getAbsolutePath().endsWith(PluginManager.JAR_EXTENSION)) {
						plugins.register(url, pack);
					}
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
		}
		
		List<Thread> subthreads = new ArrayList<Thread>();
		PathHacker.addLibraryPathFromDirectory(pack.getLocalPackageDirectory());
		try {
			PathHacker.addJar(pack.getLocalPackageDirectory().toURI().toURL());
			for (File f : pack.getLocalPackageDirectory().listFiles()) {
				if (f.isDirectory()) {
					PathHackerRunnable runnable = new PathHackerRunnable(pack, verbose, plugins, f);
					subthreads.add(runnable);
					subthreads.get(subthreads.size()-1).start();
				}
			}
		} catch (MalformedURLException e) {
			assert (false);
		}
				
		for (Thread t : subthreads) {
			try {
				t.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
