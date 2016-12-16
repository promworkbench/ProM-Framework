package org.processmining.framework.packages;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.processmining.framework.boot.Boot;
import org.processmining.framework.boot.Boot.Level;
import org.processmining.framework.packages.events.PackageManagerListener;
import org.processmining.framework.packages.impl.CancelledException;
import org.processmining.framework.packages.impl.PackageConfigPerister;
import org.processmining.framework.plugin.impl.PluginCacheEntry;
import org.processmining.framework.plugin.impl.PluginManagerImpl;
import org.processmining.framework.util.OsUtil;
import org.xml.sax.SAXException;

/*
 * Package manager goals: - Packages are completely independent from plugins and
 * are simply a distribution mechanism - Easy to provide a package as a single
 * zip or jar file, with an xml description file - Easy addition of custom
 * repositories - Transparent caching of packages - Transparent use in
 * development - Should be possible to depend on / resolve packages in an Ant
 * script - Should be easy to develop a package in Eclipse - Versions need to be
 * managed and version conflicts need to be resolved - Handles (possibly
 * transitive and circular) dependencies - Cleaning out all files from the
 * package directory of packages which are de-installed
 * 
 * @author peter
 */
public class PackageManager {
	private final static String DO_AUTO_UPDATES = "do_auto_updates";
	private static final String LITE_VERSION_INSTALLED = "lite_version_installed";

	public static interface Canceller {
		public boolean isCancelled();
	}

	private static final String TEMP_INSTALL_DIR_POSTFIX = "-temp-install-dir";
	private static final String CONFIG_XML = "packages.xml";

	private static final String nl = System.getProperty("line.separator");

	private static final int UNIX_OWNER_EXECUTABLE_BIT = 64;

	private static PackageManager instance = null;
	private final Set<Repository> repositories = new HashSet<Repository>();
	private final PackageSet installed = new PackageSet();
	private final PackageSet available = new PackageSet();

	/*
	 * Maps every package descriptor to whether it is still available. This map
	 * acts as a cache to prevent us from have to access the URL over and over
	 * again.
	 * 
	 * This map is also used by PackageConfigPersiter when writing the packages
	 * to the local repo again. As a result, packages that are known to be
	 * unavailable will not be written back to the local repo.
	 */
	private Map<PackageDescriptor, Boolean> availability;

	/**
	 * Checks whether a package is still available. This prevents the user from
	 * installing or updating a package that cannot be installed anymore.
	 * 
	 * @param descriptor
	 *            The descriptor of the package.
	 * @return Whether the URL of the package descriptor can be opened
	 *         successfully.
	 */
	public boolean isAvailable(PackageDescriptor descriptor) {
		if (!Boot.CHECK_PACKAGES) {
			return true;
		}
		/*
		 * First check the cache.
		 */
		if (availability.containsKey(descriptor)) {
			/*
			 * In cache, return cached result.
			 */
			return availability.get(descriptor);
		}
		/*
		 * Not in cache, check whether URL still exists.
		 */
		InputStream is = null;
		try {
			URL url = new URL(descriptor.getURL());
			URLConnection conn = url.openConnection();
			if (conn instanceof HttpURLConnection) {
				HttpURLConnection httpCon = (HttpURLConnection) conn;
				if (Boot.CONNECT_TIMEOUT > 0) {
					httpCon.setConnectTimeout(Boot.CONNECT_TIMEOUT);
				}
				if (Boot.READ_TIMEOUT > 0) {
					httpCon.setReadTimeout(Boot.READ_TIMEOUT);
				}
				//					httpCon.connect();
			}

			is = conn.getInputStream();
		} catch (Exception e) {
			/*
			 * Something's wrong with this URL. Mark it as unavailable.
			 */
			System.err.println("Package found in local repository, but not in global repository: " + descriptor);
			availability.put(descriptor, false);
			return false;
		} finally {
			try {
				is.close();
			} catch (Exception e) {
			}
		}
		//		System.out.println("Package available: "+ descriptor);
		/*
		 * All fine, still available. Mark it as such.
		 */
		availability.put(descriptor, true);
		return true;
	}

	private PackageManager() {
		availability = new HashMap<PackageDescriptor, Boolean>();
	}

	private final PackageManagerListener.ListenerList listeners = new PackageManagerListener.ListenerList();
	private PackageStateReport report = null;
	private Canceller canceller = null;
	private boolean doAutoUpdate = false;
	private Preferences preferences = Preferences.userNodeForPackage(getClass());

	public static PackageManager getInstance() {
		if (instance == null) {
			instance = new PackageManager();
		}
		return instance;
	}

	public static void main(String[] args) {
		System.exit(new CommandLineInterface(getInstance()).run(args));
	}

	private File getConfigFile() {
		return new File(OsUtil.getProMPackageDirectory(), CONFIG_XML);
	}

	public void addListener(PackageManagerListener listener) {
		listeners.add(listener);
	}

	public void removeListener(PackageManagerListener listener) {
		listeners.remove(listener);
	}

	public File getPackagesDirectory() {
		return new File(Boot.PACKAGE_FOLDER);
	}

	public void initialize(Boot.Level verbose) {

		doAutoUpdate = Boolean.parseBoolean(preferences.get(DO_AUTO_UPDATES, Boolean.FALSE.toString()));

		String liteVersion = preferences.get(LITE_VERSION_INSTALLED, "UNKNOWN");
		if (Boot.PROM_VERSION.startsWith(Boot.LITE_PREFIX) && !liteVersion.equals(Boot.PROM_VERSION)) {
			preferences.put(LITE_VERSION_INSTALLED, Boot.PROM_VERSION);
			if (verbose == Level.ALL) {
				System.out.println(">>> New ProM-Lite installation found.");
				System.out.println(">>> Clearing package cache.");
			}
			try {
				cleanPackageCache();
			} catch (BackingStoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		getPackagesDirectory().mkdirs();
		File config = getConfigFile();

		if (verbose == Level.ALL) {
			System.out.println(">>> Loading packages from " + config.getAbsolutePath());
		}

		try {
			writeDefaultConfigIfNeeded(config);

			repositories.clear();
			available.clear();
			installed.clear();
			PackageConfigPerister.read(config, repositories, available, installed, canceller);
			resolveAllConflicts(verbose);

		} catch (Exception e) {
			listeners.fireException(e);
		}
	}

	private void scanRepositories(Boot.Level verbose) throws ParserConfigurationException, SAXException, IOException {

		final Comparator<Repository> comp = new Comparator<Repository>() {

			public int compare(Repository o1, Repository o2) {
				return o1.getURL().toString().compareTo(o2.getURL().toString());
			}
		};

		Set<Repository> read = new TreeSet<Repository>(comp);
		Set<Repository> toRead = new TreeSet<Repository>(comp);
		toRead.addAll(repositories);

		toRead.add(new Repository(Boot.DEFAULT_REPOSITORY));
		while (!toRead.isEmpty()) {
			for (Repository rep : toRead) {
				URL packages = rep.getURL();
				if (verbose == Level.ALL) {
					System.out.println(">>> Loading packages from " + packages);
				}
				URLConnection conn = packages.openConnection();
				if (conn instanceof HttpURLConnection) {
					HttpURLConnection httpCon = (HttpURLConnection) conn;
					if (Boot.CONNECT_TIMEOUT > 0) {
						httpCon.setConnectTimeout(Boot.CONNECT_TIMEOUT);
					}
					if (Boot.READ_TIMEOUT > 0) {
						httpCon.setReadTimeout(Boot.READ_TIMEOUT);
					}
					//					httpCon.connect();
				}
				long time = -System.currentTimeMillis();
				try {
					PackageConfigPerister.read(conn.getInputStream(), repositories, available, installed, canceller);
					time += System.currentTimeMillis();
					if (Boot.VERBOSE == Level.ALL) {
						System.out.println("Read package in " + time + " milliseconds.");
					}
				} catch (SocketTimeoutException e) {
					time += System.currentTimeMillis();
					if (Boot.VERBOSE != Level.NONE) {
						System.err.println("Failed to read package in " + time + " milliseconds.");
					}
				} catch (FileNotFoundException e) {
					// did not fine the file for some package
					time += System.currentTimeMillis();
					if (Boot.VERBOSE != Level.NONE) {
						System.err.println("Failed to read package (file not found).");
					}
				}
			}
			read.addAll(toRead);
			toRead.clear();
			toRead.addAll(repositories);
			toRead.removeAll(read);
		}

	}

	private void writeDefaultConfigIfNeeded(File config) throws IOException {
		config.createNewFile();
		if (config.length() == 0) {
			PackageConfigPerister
					.write(config,
							new HashSet<Repository>(Arrays.asList(new Repository[] { new Repository(
									Boot.DEFAULT_REPOSITORY) })), new HashSet<PackageDescriptor>(),
							new HashSet<PackageDescriptor>());
		}
	}

	private void resolveAllConflicts(Boot.Level verbose) throws UnknownPackageException {
		boolean ok;

		do {
			ok = true;

			Map<String, PackageDescriptor> enabled = new HashMap<String, PackageDescriptor>();
			for (PackageDescriptor pack : getEnabledPackages()) {
				enabled.put(pack.getName(), pack);
			}
			for (PackageDescriptor pack : enabled.values()) {
				for (String dep : pack.getDependencies()) {
					PackageDescriptor p = enabled.get(dep);

					if (p == null) {
						pack.setHasBrokenDependencies();
						ok = false;

						if (verbose == Level.ALL) {
							System.out.println("     deactivating: " + pack + " (missing at least " + dep + ")");
						}
						break;
					}
				}
				for (String conf : pack.getConflicts()) {
					PackageDescriptor p = enabled.get(conf);

					if (p != null) {
						pack.setHasBrokenDependencies();
						ok = false;

						if (verbose == Level.ALL) {
							System.out
									.println("     deactivating: " + pack + " (conflicts with at least " + conf + ")");
						}
						break;
					}
				}
			}
		} while (!ok);
	}

	public String toString() {
		StringBuffer result = new StringBuffer();
		Set<PackageDescriptor> allPackages = new HashSet<PackageDescriptor>();

		for (Repository repo : repositories) {
			result.append("Repository: " + repo.getURL() + nl);
		}

		allPackages.addAll(installed);
		allPackages.addAll(available);
		for (PackageDescriptor pack : allPackages) {
			String status;

			if (installed.contains(pack)) {
				if (!pack.hasBrokenDependencies()) {
					status = "I";
				} else {
					status = "M";
				}
				boolean hasUpgrade = false;
				if (available.contains(pack)) {
					for (PackageDescriptor p : available) {
						if (p.equals(pack)) {
							hasUpgrade = pack.getVersion().lessThan(p.getVersion());
							break;
						}
					}
				}
				status += hasUpgrade ? "+" : " ";
			} else {
				status = "A ";
			}
			result.append(status + " " + pack);
			result.append(nl);
			if (!pack.getDependencies().isEmpty()) {
				result.append("      (depends on: " + pack.getDependencies() + ")" + nl);
			}
			if (!pack.getConflicts().isEmpty()) {
				result.append("      (conflicts with: " + pack.getConflicts() + ")" + nl);
			}
		}
		return result.toString();
	}

	public Set<Repository> getRepositories() {
		return Collections.unmodifiableSet(repositories);
	}

	//	public void addRepository(Repository repository) {
	//		repositories.add(repository);
	//		save();
	//	}

	public void removeRepository(Repository repository) {
		repositories.remove(repository);
		save();
	}

	public Set<PackageDescriptor> getInstalledPackages() {
		return Collections.unmodifiableSet(installed);
	}

	public Set<PackageDescriptor> getAvailablePackages() {
		return Collections.unmodifiableSet(available);
	}

	public Collection<PackageDescriptor> getEnabledPackages() throws UnknownPackageException {
		List<PackageDescriptor> result = new ArrayList<PackageDescriptor>();

		Set<PackageDescriptor> broken = new HashSet<PackageDescriptor>();

		Set<PackageDescriptor> installed = new HashSet<PackageDescriptor>(this.installed);

		/*
		 * During every iteration we should be able to add at least one package
		 * to the result set. Hence, we should need at most as many iterations
		 * as we have packages to add. If after this number if iterations some
		 * packages have not yet been added, there should be a cyclic dependency
		 * somewhere between the remaining packages.
		 * 
		 * Initialize the number of iterations we have still left.
		 */
		int iterationsLeft = installed.size();

		/*
		 * Iterate as long as needed.
		 */
		while (!installed.isEmpty() && iterationsLeft > 0) {
			Set<String> requiredPackages = new HashSet<String>();

			Iterator<PackageDescriptor> it = installed.iterator();
			while (it.hasNext()) {
				PackageDescriptor pack = it.next();
				if (pack.hasBrokenDependencies()) {
					broken.add(pack);
					it.remove();
				} else {

					if (getPackageMap(result).keySet().containsAll(pack.getDependencies())) {
						result.add(pack);
						it.remove();
					} else {
						// remember all packages that have been required
						requiredPackages.addAll(pack.getDependencies());
					}
				}
			}

			// after this iteration check whether we have any chance to resolve
			// the remaining dependencies: if not, throw an exception
			Set<String> listedPackages = new HashSet<String>(getPackageMap(this.installed).keySet());
			requiredPackages.removeAll(listedPackages);
			if (!requiredPackages.isEmpty()) {
				for (String required : requiredPackages) {
					System.out.println("Cannot find required package: " + required);
				}
				throw new UnknownPackageException(requiredPackages.toString());
			}
			/*
			 * One less iteration left.
			 */
			iterationsLeft--;
		}

		if (installed.isEmpty()) {
			if (Boot.VERBOSE == Level.ALL) {
				System.out.println(">>> All dependencies have been resolved");
			}
		} else {
			if (Boot.VERBOSE != Level.NONE) {
				System.err.println(">>> The dependencies for the following packages have not been resolved:");
				for (PackageDescriptor pack : installed) {
					System.err.println(">>>     " + pack + " " + pack.getDependencies());
				}
			}
		}

		return result;
	}

	private void save() {
		try {
			PackageConfigPerister.write(getConfigFile(), repositories, available, installed);
		} catch (IOException e) {
			listeners.fireException(e);
			return;
		}
	}

	public void update(boolean autoInstall, Boot.Level verbose) throws CancelledException, UnknownPackageTypeException {

		try {
			scanRepositories(verbose);
		} catch (Exception e) {
			// continue with known repositories
			e.printStackTrace();
		}
		Map<String, SortedSet<PackageDescriptor>> map = PackageManager.getPackageMap(available);

		Set<PackageDescriptor> packs = new HashSet<PackageDescriptor>();

		// TODO allow clients to monitor progress and report errors

		//		for (Repository repository : repositories) {
		//			if ((canceller != null) && canceller.isCancelled()) {
		//				return;
		//			}
		//			System.out.println("Updating: " + repository);
		//			try {
		//				InputStream is = repository.getURL().openStream();
		//
		//				try {
		//					packs.addAll(PackageConfigPerister.listRepository(is, canceller));
		//				} catch (ParserConfigurationException e) {
		//					listeners.fireException(e);
		//					throw new CancelledException();
		//				} catch (CancelledException e) {
		//					throw e;
		//				} catch (SAXException e) {
		//					listeners.fireException(e);
		//					throw new CancelledException();
		//				} finally {
		//					is.close();
		//				}
		//			} catch (IOException e) {
		//				listeners.fireException(e);
		//				throw new CancelledException();
		//			}
		//			System.out.println("Done updating: " + repository);
		//		}
		//
		//		available.clear();
		//		available.addAll(packs);

		if (autoInstall) {
			List<PackageDescriptor> toRemove = new ArrayList<PackageDescriptor>();
			List<PackageDescriptor> toInstall = new ArrayList<PackageDescriptor>();
			for (PackageDescriptor pack : packs) {
				PackageDescriptor inst = findInstalledVersion(pack);
				if ((inst != null) && inst.getVersion().lessThan(pack.getVersion())) {
					// package was already installed, but new version is available.
					// old version remains, just in case this is required by some
					// existing plugins.
					System.out.println("Found new version of installed package: " + pack.getName());
					toInstall.add(pack);
					toRemove.add(inst);
				} else if ((inst == null) && pack.hasPlugins()) {
					// Install all available packages that carry plugins.
					System.out.println("Found new package which contains plugins: " + pack.getName());
					toInstall.add(pack);
					for (String dep : pack.getDependencies()) {
						if (!containsPackage(installed, dep)) {
							SortedSet<PackageDescriptor> packages = map.get(dep);
							if ((packages == null) || packages.isEmpty()) {
								toInstall.remove(pack);
								continue;
							}
							boolean found = false;
							for (PackageDescriptor p : packages) {
								toInstall.add(p);
								found = true;
								break;
							}
							if (!found) {
								toInstall.remove(pack);
							}

						}
					}
				}
			}
			if (!toInstall.isEmpty()) {
				System.out.println("Installing packages: " + toInstall.toString());
				install(toInstall);
				uninstall(toRemove);
				// execute does a save, no need to do that twice.
			} else {
				save();
			}
		} else {
			save();
		}
	}

	public PackageDescriptor findInstalledVersion(PackageDescriptor pack) {
		for (PackageDescriptor p : installed) {
			if (p.getName().equals(pack.getName())) {
				return p;
			}
		}
		return null;
	}

	public PackageDescriptor[] findOrInstallPackages(String... packageNames) throws UnknownPackageTypeException,
			UnknownPackageException, CancelledException {

		if (doAutoUpdate) {
			update(false, Level.NONE);
		}

		PackageDescriptor[] result = new PackageDescriptor[packageNames.length];
		List<PackageDescriptor> toInstall = new ArrayList<PackageDescriptor>(result.length);

		// First, consider all available packages
		Map<String, SortedSet<PackageDescriptor>> map = PackageManager.getPackageMap(available);

		// Check for each package if it might be installed
		for (int i = 0; i < packageNames.length; i++) {
			SortedSet<PackageDescriptor> packages = map.get(packageNames[i]);
			if ((packages == null) || packages.isEmpty()) {
				// No package is available with the name: names[i]
				throw new UnknownPackageException(packageNames[i]);
			}

			// Use the first available package for this name.
			// If there are more, they are sorted by 
			// version, latest version first.
			result[i] = packages.first();
			if (!doAutoUpdate) {
				// See if any of the versions is installed. If not,
				// install the latest
				for (PackageDescriptor d : packages) {
					if (installed.contains(d)) {
						result[i] = d;
						break;
					}
				}
			}
			if (!installed.contains(result[i])) {
				toInstall.add(result[i]);
			}

		}
		if (!toInstall.isEmpty()) {
			// Install some packages that were available, but not installed
			install(toInstall);
		}

		return result;
	}

	public void install(List<PackageDescriptor> toInstall) throws UnknownPackageTypeException, CancelledException {
		if ((canceller != null) && canceller.isCancelled()) {
			throw new CancelledException();
		}
		boolean exception = false;
		try {
			listeners.fireSessionStart();
			Set<PackageDescriptor> toAdd = new HashSet<PackageDescriptor>(toInstall);

			Map<String, SortedSet<PackageDescriptor>> map = PackageManager.getPackageMap(available);

			// TODO: rewrite these statements to take care of versions.
			//			// These need to be added
			//			toAdd.removeAll(getInstalledPackages());
			//			// No need to install packages that need to be removed anyway
			//			toAdd.removeAll(toRemove);

			Set<PackageDescriptor> newState = new HashSet<PackageDescriptor>(getInstalledPackages());
			newState.addAll(toAdd);

			checkConsistency(newState);
			boolean error = false;
			do {
				HashSet<String> extra = new HashSet<String>();
				for (Map.Entry<PackageDescriptor, Set<String>> item : report.getMissingDependencies().entrySet()) {
					extra.addAll(item.getValue());
				}
				if (!extra.isEmpty()) {
					for (String s : extra) {
						if (!map.containsKey(s) || map.get(s) == null) {
							// package required that is not available, break.
							error = true;
							System.err.println("[PackageManager] Package " + s + " is not available.");
						} else {
							PackageDescriptor first = map.get(s).first();
							toAdd.add(first);
							newState.add(first);
						}
					}
					checkConsistency(newState);
				}
			} while (!error && !report.getMissingDependencies().isEmpty());

			HashSet<PackageDescriptor> toRemove = new HashSet<PackageDescriptor>();
			while (!error && !report.getPackagesWithMultipleVersions().isEmpty()) {
				// Resolved all dependencies, now check for multiple versions.
				for (Map.Entry<String, SortedSet<PackageDescriptor>> item : report.getPackagesWithMultipleVersions()
						.entrySet()) {
					Iterator<PackageDescriptor> it = item.getValue().iterator();
					it.next();
					while (it.hasNext()) {
						PackageDescriptor p = it.next();
						newState.remove(p);
						toRemove.add(p);
					}
				}
				checkConsistency(newState);
			}

			if (!report.hasErrors()) {
				for (PackageDescriptor pack : toAdd) {
					installPackage(pack);
				}

				installed.clear();
				installed.addAll(newState);

				uninstall(toRemove);

				save();

			}

		} catch (UnknownPackageTypeException e) {
			exception = true;
			throw e;
		} catch (CancelledException e) {
			exception = true;
			throw e;
		} finally {

			// Before propagating the error, make sure to signal the listeners of this error.
			listeners.fireSessionComplete(exception);
		}

	}

	public void uninstall(Collection<PackageDescriptor> toRemove) throws CancelledException {
		if ((canceller != null) && canceller.isCancelled()) {
			throw new CancelledException();
		}

		Set<PackageDescriptor> newState = new HashSet<PackageDescriptor>(getInstalledPackages());
		newState.removeAll(toRemove);

		checkConsistency(newState);
		boolean error = false;
		do {
			// All packages that now have broken dependencies should also be uninstalled
			newState.removeAll(report.getMissingDependencies().keySet());
			checkConsistency(newState);
		} while (!error && !report.getMissingDependencies().isEmpty());

		if (!report.hasErrors()) {

			removePackages(newState);

			installed.clear();
			installed.addAll(newState);

			save();

		}

	}

	public void setCanceller(Canceller canceller) {
		this.canceller = canceller;

	}

	public PackageStateReport getLatestReport() {
		synchronized (report) {
			return report;
		}
	}

	public void checkConsistency() {
		checkConsistency(getInstalledPackages());
	}

	private void checkConsistency(Set<PackageDescriptor> packages) {
		Map<String, SortedSet<PackageDescriptor>> multipleVersions = new HashMap<String, SortedSet<PackageDescriptor>>();
		Map<PackageDescriptor, Set<String>> missing = new HashMap<PackageDescriptor, Set<String>>();
		Map<PackageDescriptor, Set<String>> conflicts = new HashMap<PackageDescriptor, Set<String>>();

		// check whether at most one version of each package is installed
		for (Map.Entry<String, SortedSet<PackageDescriptor>> item : getPackageMap(packages).entrySet()) {
			if (item.getValue().size() > 1) {
				SortedSet<PackageDescriptor> versions = multipleVersions.get(item.getKey());

				if (versions == null) {
					versions = new TreeSet<PackageDescriptor>();
					multipleVersions.put(item.getKey(), versions);
				}
				versions.addAll(item.getValue());
			}
		}

		// check whether all dependencies are satisfied
		for (PackageDescriptor pack : packages) {
			for (String dep : pack.getDependencies()) {
				if (!containsPackage(packages, dep)) {
					Set<String> misses = missing.get(pack);

					if (misses == null) {
						misses = new HashSet<String>();
						missing.put(pack, misses);
					}
					misses.add(dep);
				}
			}
		}

		// check whether all conflicts are satisfied
		for (PackageDescriptor pack : packages) {
			for (String conf : pack.getConflicts()) {
				if (containsPackage(packages, conf)) {
					Set<String> conflictsWith = conflicts.get(pack);

					if (conflictsWith == null) {
						conflictsWith = new HashSet<String>();
						conflicts.put(pack, conflictsWith);
					}
					conflictsWith.add(conf);
				}
			}
		}

		report = new PackageStateReport(multipleVersions, missing, conflicts);
	}

	private boolean containsPackage(Set<PackageDescriptor> packages, String dep) {
		for (PackageDescriptor pack : packages) {
			if (dep.equals(pack.getName())) {
				return true;
			}
		}
		return false;
	}

	public static Map<String, SortedSet<PackageDescriptor>> getPackageMap(Collection<PackageDescriptor> packages) {
		Map<String, SortedSet<PackageDescriptor>> result = new HashMap<String, SortedSet<PackageDescriptor>>();

		for (PackageDescriptor pack : packages) {
			SortedSet<PackageDescriptor> list = result.get(pack.getName());

			if (list == null) {
				list = new TreeSet<PackageDescriptor>();
				result.put(pack.getName(), list);
			}
			list.add(pack);
		}
		return result;
	}

	private void installPackage(PackageDescriptor pack) throws UnknownPackageTypeException, CancelledException {
		File packageDir = pack.getLocalPackageDirectory();
		File tempDir = new File(packageDir.getAbsolutePath() + TEMP_INSTALL_DIR_POSTFIX);
		URL url;
		try {
			url = new URL(pack.getURL());
		} catch (MalformedURLException e) {
			listeners.fireException(e);
			return;
		}

		recursivelyDeleteDir(tempDir);
		recursivelyDeleteDir(packageDir);

		tempDir.mkdirs();
		packageDir.mkdirs();

		if (url.toString().toLowerCase().endsWith(".jar")) {
			installJar(url, packageDir, pack.getName() + "-" + pack.getVersion(), pack);
		} else if (url.toString().toLowerCase().endsWith(".zip")) {
			installZip(url, tempDir, packageDir, pack.getName() + "-" + pack.getVersion(), pack);
		} else {
			throw new UnknownPackageTypeException(pack);
		}
		if (PluginManagerImpl.getInstance() != null) {
			Boot.addJarsForPackage(pack, Level.ALL, PluginManagerImpl.getInstance());
		}
		recursivelyDeleteDir(tempDir);
	}

	private void removePackages(Collection<PackageDescriptor> toKeep) throws CancelledException {
		if ((canceller != null) && canceller.isCancelled()) {
			throw new CancelledException();
		}
		File packageDir = getPackagesDirectory();
		File[] files = packageDir.listFiles();
		Set<File> installations = new HashSet<File>();

		for (PackageDescriptor pack : toKeep) {
			if ((canceller != null) && canceller.isCancelled()) {
				throw new CancelledException();
			}
			installations.add(pack.getLocalPackageDirectory());
		}

		if (files != null) {
			for (File file : files) {
				if (!file.isDirectory()) {
					file.delete();
				} else if (!installations.contains(file)) {
					recursivelyDeleteDir(file);
				}
			}
		}
	}

	/*
	 * private Set<PackageDescriptor>
	 * findReverseDependenciesInInstalled(PackageDescriptor pack) { return
	 * findReverseDependencies(pack, installed); }
	 * 
	 * private Set<PackageDescriptor>
	 * findDependenciesInAvailable(PackageDescriptor pack,
	 * Set<PackageVersionRange> missing) { return findDependencies(pack,
	 * missing, available); }
	 * 
	 * private static Set<PackageDescriptor> findDependencies(PackageDescriptor
	 * pack, Set<PackageVersionRange> missing, Set<PackageDescriptor> packages)
	 * { Set<PackageDescriptor> result = new HashSet<PackageDescriptor>();
	 * Set<PackageVersionRange> todo = new HashSet<PackageVersionRange>();
	 * Set<PackageVersionRange> done = new HashSet<PackageVersionRange>();
	 * 
	 * while (!todo.isEmpty()) { PackageVersionRange dep =
	 * todo.iterator().next(); boolean found = false;
	 * 
	 * todo.remove(dep); done.add(dep);
	 * 
	 * for (PackageDescriptor p : packages) { if (dep.isSatisfiedBy(p)) {
	 * result.add(p); found = true; for (PackageVersionRange r :
	 * p.getDependencies()) { if (!done.contains(r)) { todo.add(r); } } break; }
	 * } if (!found) { missing.add(dep); } } return result; }
	 * 
	 * private static Set<PackageDescriptor>
	 * findReverseDependencies(PackageDescriptor pack, Set<PackageDescriptor>
	 * packages) { Set<PackageDescriptor> result = new
	 * HashSet<PackageDescriptor>();
	 * 
	 * for (PackageDescriptor p : packages) { Set<PackageVersionRange> missing =
	 * new HashSet<PackageVersionRange>(); Set<PackageDescriptor> deps =
	 * findDependencies(p, missing, packages);
	 * 
	 * if (deps.contains(pack)) { result.add(p); } } return result; }
	 */

	private void installZip(URL source, File temp, File unzipTo, String name, PackageDescriptor pack)
			throws CancelledException {
		File sourceZipFile = new File(temp, ".package.zip");

		// download zip file

		listeners.fireStartDownload(name, source, pack);
		OutputStream out = null;
		try {
			out = new BufferedOutputStream(new FileOutputStream(sourceZipFile));
			copyInputStream(source.openStream(), out);
		} catch (Exception e) {
			listeners.fireException(e);
			throw new CancelledException();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					throw new CancelledException();
				}
			}

		}

		listeners.fireStartInstall(name, unzipTo, pack);

		try {
			// extract zip file
			ZipFile zipFile = new ZipFile(sourceZipFile);
			Enumeration<?> zipFileEntries = zipFile.getEntries();

			while (zipFileEntries.hasMoreElements()) {
				ZipArchiveEntry entry = (ZipArchiveEntry) zipFileEntries.nextElement();
				File destFile = new File(unzipTo, entry.getName());

				if (entry.isDirectory()) {
					destFile.mkdirs();
				} else {
					destFile.getParentFile().mkdirs();

					OutputStream o = new FileOutputStream(destFile);
					try {
						copyInputStream(zipFile.getInputStream(entry), o);
					} finally {
						o.close();
					}

					//Only for non-windows operating systems: Check if the executable bit was set in the zip-archive,
					//if so, set it on the file system too. (Only checks and sets the owner executable bit.)
					if (!OsUtil.isRunningWindows()
							&& (entry.getUnixMode() & UNIX_OWNER_EXECUTABLE_BIT) == UNIX_OWNER_EXECUTABLE_BIT) {
						destFile.setExecutable(true);
					}
				}

			}
			zipFile.close();
		} catch (Exception e) {
			listeners.fireException(e);
			throw new CancelledException();
		}
		listeners.fireFinishedInstall(name, unzipTo, pack);
	}

	private void installJar(URL source, File dest, String name, PackageDescriptor pack) throws CancelledException {
		InputStream in = null;
		try {
			in = source.openStream();

			OutputStream out = null;
			File outFile = new File(dest, name + ".jar");
			try {
				out = new FileOutputStream(outFile);
			} catch (FileNotFoundException e) {
				listeners.fireException(e);
				return;
			} finally {
				if (out != null) {
					out.close();
				}
			}
			listeners.fireStartDownload(name, source, pack);
			copyInputStream(in, out);
			listeners.fireStartInstall(name, outFile, pack);

			in.close();
			listeners.fireFinishedInstall(name, outFile, pack);
		} catch (IOException e) {
			listeners.fireException(e);
			throw new CancelledException();
		}
	}

	private void recursivelyDeleteDir(File dir) {
		if (dir.isDirectory()) {
			for (String child : dir.list()) {
				recursivelyDeleteDir(new File(dir, child));
			}
		}
		dir.delete();
	}

	private void copyInputStream(InputStream in, OutputStream out) throws IOException, CancelledException {
		try {
			byte[] buffer = new byte[1024];
			int len;

			while ((len = in.read(buffer)) >= 0) {
				if ((canceller != null) && canceller.isCancelled()) {
					throw new CancelledException();
				}
				out.write(buffer, 0, len);
			}
		} finally {
			try {
				in.close();
			} finally {
				out.close();
			}
		}
	}

	public PackageManagerListener.ListenerList getListeners() {
		return listeners;
	}

	@Deprecated
	public boolean doAutoUpdate() {
		return doAutoUpdate;
	}

	@Deprecated
	public void setAutoUpdate(boolean doAutoUpdate) {
		this.doAutoUpdate = doAutoUpdate;
		this.preferences.put(DO_AUTO_UPDATES, Boolean.toString(doAutoUpdate));

	}

	/**
	 * Cleans the package cache in the registry. This is automatically done for
	 * ProM-Lite the first time when a new version of ProM-Lite is booted.
	 * 
	 * @throws BackingStoreException
	 */
	public void cleanPackageCache() throws BackingStoreException {
		PluginCacheEntry.clearSettingsCache();

	}
}
