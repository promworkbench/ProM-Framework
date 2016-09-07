package org.processmining.framework.packages.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.processmining.framework.boot.Boot;
import org.processmining.framework.packages.PackageDescriptor;
import org.processmining.framework.packages.PackageDescriptor.OS;
import org.processmining.framework.packages.PackageManager;
import org.processmining.framework.packages.PackageManager.Canceller;
import org.processmining.framework.packages.PackageSet;
import org.processmining.framework.packages.Repository;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class PackageConfigPerister {

	private static final String nl = System.getProperty("line.separator");

	private PackageConfigPerister() {
	}

	public static void read(File f, Set<Repository> repositories, PackageSet available, PackageSet installed,
			Canceller canceller) throws ParserConfigurationException, SAXException, IOException {
		InputStream is = new BufferedInputStream(new FileInputStream(f));
		try {
			read(is, repositories, available, installed, canceller);
		} finally {
			is.close();
		}
	}

	public static void read(InputStream is, Set<Repository> repositories, PackageSet available, PackageSet installed,
			Canceller canceller) throws ParserConfigurationException, SAXException, IOException {
		BufferedInputStream bis = new BufferedInputStream(is);
		ConfigHandler handler = new ConfigHandler(repositories, available, installed, canceller);
		SAXParserFactory parserFactory = SAXParserFactory.newInstance();

		parserFactory.setNamespaceAware(false);
		parserFactory.setValidating(false);
		try {
			// Some old JAXP versions may throw the UnsupportedOperation Exception in the next call.
			parserFactory.setSchema(null);
		} catch (UnsupportedOperationException ex) {
			// Ignore.
		}

		SAXParser parser = parserFactory.newSAXParser();
		parser.parse(bis, handler);
		bis.close();
	}

	public static Set<PackageDescriptor> listRepository(InputStream is, Canceller canceller)
			throws ParserConfigurationException, SAXException, IOException {
		Set<Repository> repos = new HashSet<Repository>();
		PackageSet available = new PackageSet();
		PackageSet installed = new PackageSet();

		read(is, repos, available, installed, canceller);
		return available;
	}

	static private class ConfigHandler extends DefaultHandler {

		private static final String PACKAGES = "packages";
		private static final String URL_ATTR = "url";
		private static final String VERSION_ATTR = "version";
		private static final String NAME_ATTR = "name";
		private static final String DEPENDENCY = "dependency";
		private static final String PACKAGE = "package";
		private static final String REPOSITORY = "repository";
		private static final String CONFLICT = "conflict";
		private static final String DESCRIPTION_ATTR = "desc";
		private static final String ORGANISATION_ATTR = "org";
		private static final String AUTHOR_ATTR = "author";
		private static final String AUTO_ATTR = "auto";
		private static final String LICENSE_ATTR = "license";
		private static final Object INSTALLED = "installed-packages";
		private static final String HAS_PLUGINS_ATTR = "hasPlugins";
		private static final String OS_ATTR = "os";
		private static final String MAINTAINER_ATTR = "maintainer";

		private static final String LOGO_URL_ATTR = "logo";
		private static final String KEYWORDS_ATTR = "keywords";

		private Repository curRepo = null;
		private String curPackageName = null;
		private String curPackageVersion = null;
		private final List<String> dependencies = new ArrayList<String>();
		private final List<String> conflicts = new ArrayList<String>();
		private String curPackageURL;
		private String curLogoURL;
		private String curPackageDesc;
		private String curPackageOrg;
		private String curPackageAuthor;
		private String curPackageLicense;
		private String curPackageAutoInstalled;
		private String curPackageHasPlugins;
		private boolean insideInstalled = false;

		private final Set<Repository> repositories;
		private final Set<PackageDescriptor> available;
		private final Set<PackageDescriptor> installed;
		private final Canceller canceller;
		private String curPackageOS;
		private String curPackageMaintainer;
		private String curKeywords;

		public ConfigHandler(Set<Repository> repositories, Set<PackageDescriptor> available,
				Set<PackageDescriptor> installed, Canceller canceller) {
			this.available = available;
			this.installed = installed;
			this.repositories = repositories;
			this.canceller = canceller;
		}

		@Override
		public void startElement(String uri, String local, String qName, Attributes attributes) throws SAXException {
			if ((canceller != null) && canceller.isCancelled()) {
				throw new CancelledException();
			}
			qName = qName.toLowerCase();

			if ((curRepo == null) && (curPackageName == null)) {
				if (qName.equals(INSTALLED)) {
					insideInstalled = true;
				} else if (!insideInstalled && qName.equals(REPOSITORY)) {
					String url = attributes.getValue(URL_ATTR);

					if ((url != null) && (url.trim().length() > 0)) {
						try {
							curRepo = new Repository(new URL(url.trim()));
						} catch (MalformedURLException e) {
							System.err.println("Invalid URL for repository, skipping: " + url);
						}
					}
				} else if (qName.equals(PACKAGE)) {
					String name = attributes.getValue(NAME_ATTR);
					String version = attributes.getValue(VERSION_ATTR);
					String url = attributes.getValue(URL_ATTR);
					String logo = attributes.getValue(LOGO_URL_ATTR);
					String desc = attributes.getValue(DESCRIPTION_ATTR);
					String org = attributes.getValue(ORGANISATION_ATTR);
					String license = attributes.getValue(LICENSE_ATTR);
					String author = attributes.getValue(AUTHOR_ATTR);
					String auto = attributes.getValue(AUTO_ATTR);
					String hasPlugins = attributes.getValue(HAS_PLUGINS_ATTR);
					String os = attributes.getValue(OS_ATTR);
					String maintainer = attributes.getValue(MAINTAINER_ATTR);
					String keywords = attributes.getValue(KEYWORDS_ATTR);

					if ((name != null) && (name.trim().length() > 0) && //
							(version != null) && (version.trim().length() > 0) && //
							(os != null) && (os.trim().length() > 0) && //
							(url != null) && (url.trim().length() > 0)) {
						curPackageName = name;
						curPackageVersion = version;
						curPackageURL = url;
						curPackageOS = os;
						curLogoURL = logo == null ? "" : logo;
						curPackageDesc = desc == null ? "" : desc;
						curPackageOrg = org == null ? "" : org;
						curPackageLicense = license == null ? "" : license;
						curPackageAuthor = author == null ? "" : author;
						curPackageMaintainer = maintainer == null ? author : maintainer;
						curPackageAutoInstalled = auto == null ? "" : auto;
						curPackageHasPlugins = hasPlugins == null ? "" : hasPlugins;
						curKeywords = keywords == null ? "" : keywords;
						dependencies.clear();
						conflicts.clear();
					}
				}
			} else if ((curPackageName != null) && qName.equals(DEPENDENCY)) {
				String name = attributes.getValue(NAME_ATTR);

				if ((name != null) && (name.trim().length() > 0)) {
					dependencies.add(name);
				}
			} else if ((curPackageName != null) && qName.equals(CONFLICT)) {
				String name = attributes.getValue(NAME_ATTR);

				if ((name != null) && (name.trim().length() > 0)) {
					conflicts.add(name);
				}
			}
		}

		@Override
		public void endElement(String uri, String local, String qName) throws SAXException {
			if ((canceller != null) && canceller.isCancelled()) {
				throw new CancelledException();
			}
			qName = qName.toLowerCase();

			if (qName.equals(INSTALLED)) {
				insideInstalled = false;
			} else if ((curRepo != null) && qName.equals(REPOSITORY)) {
				repositories.add(curRepo);
				curRepo = null;
			} else if ((curPackageName != null) && qName.equals(PACKAGE)) {
				OS os = OS.fromString(curPackageOS);
				if (os.isUsable()) {
					PackageDescriptor pack = new PackageDescriptor(curPackageName, curPackageVersion, os,
							curPackageDesc, curPackageOrg, curPackageAuthor, curPackageMaintainer, curPackageLicense,
							curPackageURL, curLogoURL,  curKeywords, "true".equals(curPackageAutoInstalled), !"false"
									.equals(curPackageHasPlugins), dependencies, conflicts);
					if (insideInstalled) {
						installed.add(pack);
					} else {
						if (Boot.HIDE_OLD_PACKAGES) {
							// Suggested by Massimiliano de Leoni
							PackageDescriptor foundPack = null;
							for (PackageDescriptor availablePack : available) {
								if (availablePack.getName().equals(pack.getName())) {
									foundPack = availablePack;
									break;
								}
							}
							if (foundPack != null) {
								if (foundPack.getVersion().lessThan(pack.getVersion())) {
									available.remove(foundPack);
									available.add(pack);
								} else {
									// Skip, pack is dominated by foundPack.
								}
							} else {
								available.add(pack);
							}
						} else {
							available.add(pack);
						}
					}
				}
				curPackageName = null;
			}
		}
	}

	public static void write(File config, Set<Repository> repositories, Set<PackageDescriptor> available,
			Set<PackageDescriptor> installed) throws IOException {
		Writer writer = new FileWriter(config);

		// TODO properly escape all raw strings

		writer.write("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>" + nl);
		writer.write("<" + ConfigHandler.PACKAGES + ">" + nl);
		for (Repository repo : repositories) {
			writer.write("  <" + ConfigHandler.REPOSITORY + " " + ConfigHandler.URL_ATTR + "=\"" + repo.getURL() + "\""
					+ " />" + nl);
		}
		for (PackageDescriptor pack : available) {
			/*
			 * Do not write to local repo if known to be unavailable.
			 */
			if (PackageManager.getInstance().isAvailable(pack)) {
				writePackage(pack, writer);
			}
		}
		writer.write("  <" + ConfigHandler.INSTALLED + ">" + nl);
		for (PackageDescriptor pack : installed) {
			writePackage(pack, writer);
		}
		writer.write("  </" + ConfigHandler.INSTALLED + ">" + nl);

		writer.write("</" + ConfigHandler.PACKAGES + ">" + nl);
		writer.close();
	}

	private static void writePackage(PackageDescriptor pack, Writer writer) throws IOException {
		writer.write("  <" + ConfigHandler.PACKAGE + //
				" " + ConfigHandler.NAME_ATTR + "=\"" + pack.getName() + "\"" + //
				" " + ConfigHandler.VERSION_ATTR + "=\"" + pack.getVersion() + "\"" + //
				" " + ConfigHandler.OS_ATTR + "=\"" + pack.getOS().getName() + "\"" + //
				" " + ConfigHandler.URL_ATTR + "=\"" + pack.getURL() + "\"" + //
				" " + ConfigHandler.DESCRIPTION_ATTR + "=\"" + pack.getDescription() + "\"" + //
				" " + ConfigHandler.ORGANISATION_ATTR + "=\"" + pack.getOrganisation() + "\"" + //
				" " + ConfigHandler.AUTO_ATTR + "=\"" + (pack.getAutoInstalled() ? "true" : "false") + "\"" + //
				" " + ConfigHandler.HAS_PLUGINS_ATTR + "=\"" + (pack.hasPlugins() ? "true" : "false") + "\"" + //
				" " + ConfigHandler.LICENSE_ATTR + "=\"" + pack.getLicense() + "\"" + //
				" " + ConfigHandler.AUTHOR_ATTR + "=\"" + pack.getAuthor() + "\"" + //
				" " + ConfigHandler.MAINTAINER_ATTR + "=\"" + pack.getMaintainer() + "\"" + //
				" " + ConfigHandler.LOGO_URL_ATTR + "=\"" + pack.getLogoURL() + "\"" + //
				" " + ConfigHandler.KEYWORDS_ATTR + "=\"" + pack.getKeywords() + "\"" + //
				">" + nl);
		for (String dep : pack.getDependencies()) {
			writer.write("    <" + ConfigHandler.DEPENDENCY + " " + ConfigHandler.NAME_ATTR + "=\"" + dep + "\""
					+ " />" + nl);
		}
		for (String confl : pack.getConflicts()) {
			writer.write("    <" + ConfigHandler.CONFLICT + " " + ConfigHandler.NAME_ATTR + "=\"" + confl + "\""
					+ " />" + nl);
		}
		writer.write("  </" + ConfigHandler.PACKAGE + ">" + nl);
	}
}
