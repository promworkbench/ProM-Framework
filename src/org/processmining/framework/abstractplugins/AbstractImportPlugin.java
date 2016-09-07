package org.processmining.framework.abstractplugins;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import javax.swing.filechooser.FileFilter;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.PluginVariant;

/**
 * Note that implementing classes of this baseclass should carry the
 * UIImportPlugin annotation
 * 
 * Subclasses of AbstractImportPlugin should use the @Plugin Annotation as
 * follows:
 * 
 * @Plugin( name = "{any name}", parameterLabels={"Filename"}, returnLabels = {
 *          {The right return labels} }, returnTypes = { {The right return
 *          classes} })
 * 
 * 
 * @author bfvdonge
 * 
 */
public abstract class AbstractImportPlugin implements ImportPlugin {

	private File file = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.plugins.abstractplugins.ImportPlugin#getFile()
	 */
	public File getFile() {
		return file;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.plugins.abstractplugins.ImportPlugin#importFile(org
	 * .processmining.framework.plugin.PluginContext, java.lang.String)
	 */
	@PluginVariant(requiredParameterLabels = { 0 })
	public Object importFile(PluginContext context, String filename) throws Exception {
		file = new File(filename);
		return importFromStream(context, new FileInputStream(file), filename, file.length());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.plugins.abstractplugins.ImportPlugin#importFile(org
	 * .processmining.framework.plugin.PluginContext, java.net.URI)
	 */
	@PluginVariant(requiredParameterLabels = { 0 })
	public Object importFile(PluginContext context, URI uri) throws Exception {
		return importFromStream(context, uri.toURL().openStream(), uri.toString(), 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.plugins.abstractplugins.ImportPlugin#importFile(org
	 * .processmining.framework.plugin.PluginContext, java.net.URL)
	 */
	@PluginVariant(requiredParameterLabels = { 0 })
	public Object importFile(PluginContext context, URL url) throws Exception {
		file = new File(url.toURI());
		return importFromStream(context, url.openStream(), url.toString(), 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.plugins.abstractplugins.ImportPlugin#importFile(org
	 * .processmining.framework.plugin.PluginContext, java.io.File)
	 */
	@PluginVariant(requiredParameterLabels = { 0 })
	public Object importFile(PluginContext context, File f) throws Exception {
		file = f;
		InputStream stream = getInputStream(f);
		return importFromStream(context, stream, file.getName(), file.length());
	}

	/**
	 * This method returns an inputStream for a file. Note that the default
	 * implementation returns "new FileInputStream(file);"
	 * 
	 * @param file
	 * @return
	 * @throws FileNotFoundException
	 */
	protected InputStream getInputStream(File file) throws Exception {
		return new FileInputStream(file);
	}

	/**
	 * This method is called by all plugin variants to do the actual importing.
	 * 
	 * @param context
	 * @param input
	 * @param filename
	 * @param fileSizeInBytes
	 * @return
	 * @throws Exception
	 */
	protected abstract Object importFromStream(PluginContext context, InputStream input, String filename,
			long fileSizeInBytes) throws Exception;

}

class ZipFilter extends FileFilter {

	private final FileFilter parent;

	public ZipFilter(FileFilter parent) {
		this.parent = parent;
	}

	public boolean accept(File f) {
		return (f.getAbsolutePath().endsWith(".zip") || parent.accept(f));
	}

	public String getDescription() {
		return parent.getDescription();
	}

}