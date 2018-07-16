package org.processmining.framework.boot;

import java.io.File;
import java.net.MalformedURLException;

import org.processmining.framework.boot.Boot.Level;
import org.processmining.framework.packages.PackageDescriptor;
import org.processmining.framework.plugin.PluginManager;
import org.processmining.framework.util.PathHacker;

/**
 * Threaded executions of PathHacker addJar
 * 
 * @author berti
 *
 */
public class PathHackerRunnable extends Thread {
	PackageDescriptor pack;
	Level verbose;
	PluginManager plugins;
	File f;
	
	public PathHackerRunnable(PackageDescriptor pack, Level verbose, PluginManager plugins, File f) {
		this.pack = pack;
		this.verbose = verbose;
		this.plugins = plugins;
		this.f = f;
	}
	
	/**
	 * Entry point for thread
	 */
	public void run() {
		//System.out.println("pathHacker "+f.toURI());
		try {
			PathHacker.addJar(f.toURI().toURL());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
