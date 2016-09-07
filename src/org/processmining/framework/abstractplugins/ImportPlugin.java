package org.processmining.framework.abstractplugins;

import java.io.File;
import java.net.URI;
import java.net.URL;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.PluginVariant;

/**
 * This interface should be implemented by plugins that serve as input plugins.
 * 
 * Such a plugin should have the @Plugin annotation on the class level and does
 * not have to define any variants, as these are defined in the interface.
 * 
 * @author bfvdonge
 * 
 */

public interface ImportPlugin {

	/**
	 * Returns the File object this plugin was instantiated with.
	 * 
	 * @return
	 */
	public File getFile();

	@PluginVariant(requiredParameterLabels = { 0 })
	public Object importFile(PluginContext context, String filename) throws Exception;

	@PluginVariant(requiredParameterLabels = { 0 })
	public Object importFile(PluginContext context, URI uri) throws Exception;

	@PluginVariant(requiredParameterLabels = { 0 })
	public Object importFile(PluginContext context, URL url) throws Exception;

	@PluginVariant(requiredParameterLabels = { 0 })
	public Object importFile(PluginContext context, File f) throws Exception;

}