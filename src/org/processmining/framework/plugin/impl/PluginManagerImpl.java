package org.processmining.framework.plugin.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import javax.swing.event.EventListenerList;
import javax.xml.parsers.ParserConfigurationException;

import org.processmining.framework.boot.Boot;
import org.processmining.framework.boot.Boot.Level;
import org.processmining.framework.packages.PackageDescriptor;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.PluginDescriptor;
import org.processmining.framework.plugin.PluginDescriptorID;
import org.processmining.framework.plugin.PluginManager;
import org.processmining.framework.plugin.PluginParameterBinding;
import org.processmining.framework.plugin.annotations.Bootable;
import org.processmining.framework.plugin.annotations.KeepInProMCache;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.Pair;
import org.processmining.framework.util.collection.ComparablePair;
import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

public final class PluginManagerImpl implements PluginManager {

	private static final char PACKAGE_SEPARATOR = '.';
	private static final char URL_SEPARATOR = '/';
	private static final char INNER_CLASS_MARKER = '$';

	private static PluginManagerImpl pluginManager;

	private final Set<Class<?>> knownObjectTypes = new HashSet<Class<?>>();

	private final Map<PluginDescriptorID, PluginDescriptor> plugins = new HashMap<PluginDescriptorID, PluginDescriptor>();
	private final Map<Class<? extends Annotation>, Set<PluginDescriptor>> annotation2plugins = new HashMap<Class<? extends Annotation>, Set<PluginDescriptor>>();
	private final EventListenerList pluginManagerListeners = new EventListenerList();
	private final Class<? extends PluginContext> pluginContextType;

	private final Map<Class<? extends Annotation>, Set<Class<?>>> annotatedClasses = new HashMap<Class<? extends Annotation>, Set<Class<?>>>();

	private PluginManagerImpl(Class<? extends PluginContext> pluginContextType) {
		this.pluginContextType = pluginContextType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.plugin.PluginManager#addErrorListener(org
	 * .processmining.framework.plugin.PluginManagerImpl.ErrorListener)
	 */
	public void addListener(PluginManagerListener listener) {
		pluginManagerListeners.add(PluginManagerListener.class, listener);
	}

	public Set<Class<?>> getKnownClassesAnnotatedWith(Class<? extends Annotation> annotationType) {
		Set<Class<?>> set = annotatedClasses.get(annotationType);
		if (set == null) {
			return Collections.emptySet();
		} else {
			return Collections.unmodifiableSet(set);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.plugin.PluginManager#removeErrorListener(
	 * org.processmining.framework.plugin.PluginManagerImpl.ErrorListener)
	 */
	public void removeListener(PluginManagerListener listener) {
		pluginManagerListeners.remove(PluginManagerListener.class, listener);
	}

	protected void fireError(URL url, Throwable t, String className) {
		for (PluginManagerListener listener : pluginManagerListeners.getListeners(PluginManagerListener.class)) {
			listener.error(url, t, className);
		}

	}

	protected void firePluginAdded(PluginDescriptor plugin, Collection<Class<?>> types) {
		for (PluginManagerListener listener : pluginManagerListeners.getListeners(PluginManagerListener.class)) {
			listener.newPlugin(plugin, types);
		}
	}

	public static void initialize(Class<? extends PluginContext> pluginContextType) {
		if (pluginManager == null) {
			pluginManager = new PluginManagerImpl(pluginContextType);
		}
	}

	public static PluginManager getInstance() {
		return pluginManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.plugin.PluginManager#register(java.net.URL)
	 */
	public void register(URL url, PackageDescriptor pack) {
		URLClassLoader loader = new URLClassLoader(new URL[] { url });
		register(url, pack, loader);
	}

	public void register(URL url, PackageDescriptor pack, ClassLoader loader) {
		if (url.getProtocol().equals(FILE_PROTOCOL)) {
			try {
				File file = new File(url.toURI());

				if (file.isDirectory()) {
					scanDirectory(file, pack, loader);
					return;
				}
				if (file.getAbsolutePath().endsWith(PluginManager.MCR_EXTENSION)) {
					try {
						loadClassFromMacro(url.toURI(), pack);
					} catch (DependsOnUnknownException e) {
						// Can't add this URL.
					}
				}
				if (file.getAbsolutePath().endsWith(JAR_EXTENSION)) {
					scanUrl(url, pack, loader);
				}
			} catch (URISyntaxException e) {
				fireError(url, e, null);
			}
		} else {
			scanUrl(url, pack, loader);
		}
	}

	private void scanDirectory(File file, PackageDescriptor pack, ClassLoader loader) {
		try {
			URL url = file.toURI().toURL();

			Queue<File> todo = new LinkedList<File>();
			FileFilter filter = new FileFilter() {
				public boolean accept(File pathname) {
					return pathname.isDirectory() || pathname.getPath().endsWith(CLASS_EXTENSION)
							|| pathname.getPath().endsWith(MCR_EXTENSION) || pathname.getPath().endsWith(JAR_EXTENSION);

				}
			};

			todo.add(file);
			while (!todo.isEmpty()) {
				File dir = todo.remove();

				for (File f : dir.listFiles(filter)) {
					if (f.isDirectory()) {
						todo.add(f);
					} else {
						if (f.getAbsolutePath().endsWith(CLASS_EXTENSION)) {
							loadClassFromFile(loader, url,
									makeRelativePath(file.getAbsolutePath(), f.getAbsolutePath()), pack);
						} else if (f.getAbsolutePath().endsWith(MCR_EXTENSION)) {
							try {
								loadClassFromMacro(f.toURI(), pack);
							} catch (DependsOnUnknownException e) {
								todo.add(dir);
							}
						} else if (f.getAbsolutePath().endsWith(JAR_EXTENSION)) {
							scanUrl(f.toURI().toURL(), pack, loader);
						}
					}
				}
			}
		} catch (MalformedURLException e) {
			fireError(null, e, null);
		}
	}

	private String makeRelativePath(String root, String absolutePath) {
		String relative = absolutePath;

		if (relative.startsWith(root)) {
			relative = relative.substring(root.length());
			if (relative.startsWith(File.separator)) {
				relative = relative.substring(File.separator.length());
			}
		}
		return relative;
	}

	private void scanUrl(URL url, PackageDescriptor pack, ClassLoader loader) {

		PluginCacheEntry cached = new PluginCacheEntry(url, Boot.VERBOSE, pack);

		if (cached.isInCache()) {
			for (String className : cached.getCachedClassNames()) {
				loadClass(loader, url, className, pack);
			}
		} else {
			try {
				InputStream is = url.openStream();
				JarInputStream jis = new JarInputStream(is);
				JarEntry je;
				List<String> loadedClasses = new ArrayList<String>();

				while ((je = jis.getNextJarEntry()) != null) {
					if (!je.isDirectory() && je.getName().endsWith(CLASS_EXTENSION)) {
						String loadedClass = loadClassFromFile(loader, url, je.getName(), pack);
						if (loadedClass != null) {
							loadedClasses.add(loadedClass);
						}
					}
				}
				jis.close();
				is.close();

				cached.update(loadedClasses);
			} catch (IOException e) {
				fireError(url, e, null);
			}
		}
	}

	private String loadClassFromFile(ClassLoader loader, URL url, String classFilename, PackageDescriptor pack) {
		if (classFilename.indexOf(INNER_CLASS_MARKER) >= 0) {
			// we're not going to load inner classes
			return null;
		}
		return loadClass(loader, url, classFilename.substring(0, classFilename.length() - CLASS_EXTENSION.length())
				.replace(URL_SEPARATOR, PACKAGE_SEPARATOR).replace(File.separatorChar, PACKAGE_SEPARATOR), pack);
	}

	private String loadClassFromMacro(URI macroFile, PackageDescriptor pack) throws DependsOnUnknownException {
		MacroPluginDescriptorImpl plugin = null;
		try {
			plugin = new MacroPluginDescriptorImpl(new File(macroFile), this, pack);
			addPlugin(plugin);
		} catch (DOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (DependsOnUnknownException e) {
			throw e;
		}
		return plugin == null ? null : plugin.getFileName();
	}

	/**
	 * Returns the name of the class, if it is annotated, or if any of its
	 * methods carries a plugin annotation!
	 * 
	 * @param loader
	 * @param url
	 * @param className
	 * @return
	 */
	private String loadClass(ClassLoader loader, URL url, String className, PackageDescriptor pack) {
		boolean isAnnotated = false;

		if ((className == null) || className.trim().equals("") || className.startsWith("bin-test-instrument")) {
			return null;
		}

		className = className.trim();
		try {
			Class<?> pluginClass = Class.forName(className, false, loader);
			//isAnnotated = (pluginClass.getAnnotations().length > 0);

			// register all annotated classes
			if (pluginClass.isAnnotationPresent(KeepInProMCache.class)
					&& !Modifier.isAbstract(pluginClass.getModifiers())) {
				Annotation[] annotations = pluginClass.getAnnotations();
				isAnnotated = true;
				for (int i = 0; i < annotations.length; i++) {
					Set<Class<?>> set = annotatedClasses.get(annotations[i].annotationType());
					if (set == null) {
						set = new HashSet<Class<?>>();
						annotatedClasses.put(annotations[i].annotationType(), set);
					}
					set.add(pluginClass);
				}
			}

			Method[] methods = pluginClass.getMethods();
			// Check if plugin annotation is present
			if (pluginClass.isAnnotationPresent(Plugin.class) && isGoodPlugin(pluginClass, methods)) {
				PluginDescriptorImpl pl = new PluginDescriptorImpl(pluginClass, pluginContextType, pack);
				addPlugin(pl);
				isAnnotated = true;
			}

			for (Method method : methods) {
				if (method.isAnnotationPresent(Plugin.class) && isGoodPlugin(method)) {
					try {
						PluginDescriptorImpl pl = new PluginDescriptorImpl(method, pack);
						addPlugin(pl);
						isAnnotated = true;
					} catch (Exception e) {
						fireError(url, e, className);
						if (Boot.VERBOSE != Level.NONE) {
							System.err.println("ERROR while adding plugin: " + url + ":" + e.getMessage());
						}
					}
				}
			}
		} catch (Throwable t) {
			fireError(url, t, className);
			if (Boot.VERBOSE != Level.NONE) {
				System.err.println("ERROR while scanning for plugins at: " + url + ":");
				System.err.println("   in file :" + className);
				System.err.println("   " + t.getMessage());
				//t.printStackTrace();
			}
		}
		return isAnnotated ? className : null;
	}

	private void addPlugin(PluginDescriptorImpl pl) {
		PluginDescriptorImpl old = (PluginDescriptorImpl) plugins.put(pl.getID(), pl);

		if (old != null) {
			if (Boot.VERBOSE == Level.ALL) {
				System.out.println("Found new version of plugin: " + pl.getName() + " ....overwriting.");
			}
			for (Annotation annotation : old.getAnnotatedElement().getAnnotations()) {
				annotation2plugins.get(annotation.annotationType()).remove(old);
			}
		}

		for (Annotation annotation : pl.getAnnotatedElement().getAnnotations()) {
			Set<PluginDescriptor> pls = annotation2plugins.get(annotation.annotationType());
			if (pls == null) {
				pls = new TreeSet<PluginDescriptor>();
				annotation2plugins.put(annotation.annotationType(), pls);
			}
			pls.add(pl);

		}
		checkTypesAfterAdd(pl);

	}

	private void addPlugin(MacroPluginDescriptorImpl pl) {
		PluginDescriptor old = plugins.put(pl.getID(), pl);

		if (old != null) {
			if (Boot.VERBOSE == Level.ALL) {
				System.out.println("Found new version of plugin: " + pl.getName() + " ....overwriting.");
			}
			annotation2plugins.get(Plugin.class).remove(old);
		}

		Set<PluginDescriptor> pls = annotation2plugins.get(Plugin.class);
		if (pls == null) {
			pls = new TreeSet<PluginDescriptor>();
			annotation2plugins.put(Plugin.class, pls);
		}
		pls.add(pl);

		checkTypesAfterAdd(pl);
	}

	private void checkTypesAfterAdd(PluginDescriptor pl) {
		HashSet<Class<?>> newTypes = new HashSet<Class<?>>();
		for (List<Class<?>> parTypes : pl.getParameterTypes()) {
			newTypes.addAll(addKnownObjectTypes(parTypes));
		}
		newTypes.addAll(addKnownObjectTypes(pl.getReturnTypes()));

		firePluginAdded(pl, newTypes);
	}

	private Collection<Class<?>> addKnownObjectTypes(Collection<Class<?>> types) {
		List<Class<?>> newTypes = new ArrayList<Class<?>>();
		for (Class<?> type : types) {
			if (type.isArray()) {
				if (knownObjectTypes.add(type.getComponentType())) {
					newTypes.add(type.getComponentType());
				}
			}
			if (knownObjectTypes.add(type)) {
				newTypes.add(type);
			}
		}
		return newTypes;
	}

	private boolean isGoodPlugin(Class<?> type, Method[] methods) {
		try {
			if (!isRightlyAnnotated(type)) {
				return false;
			}
			String[] names = type.getAnnotation(Plugin.class).parameterLabels();
			Class<?>[] returnTypes = type.getAnnotation(Plugin.class).returnTypes();

			// Check if there is at least one method annotated with PluginVariant
			List<Method> pluginMethods = new ArrayList<Method>(methods.length);
			for (Method m : methods) {
				if (m.isAnnotationPresent(PluginVariant.class)) {
					pluginMethods.add(m);
				}
			}

			// Check if for all methods, the requiredTypes are set Correctly
			Iterator<Method> it = pluginMethods.iterator();
			loop: while (it.hasNext()) {
				Method m = it.next();
				int[] required = m.getAnnotation(PluginVariant.class).requiredParameterLabels();
				Set<Integer> set = new HashSet<Integer>();
				for (int i : required) {
					set.add(i);
					if ((i < 0) || (i >= names.length)) {
						if (Boot.VERBOSE != Level.NONE) {
							System.err
									.println("Method "
											+ m.toString()
											+ " could not be added as a plugin. At least one required parameter is not a valid index."
											+ "There is no parameterlabel at index " + i);
						}
						it.remove();
						continue loop;
					}
				}
				if (set.size() != required.length) {
					if (Boot.VERBOSE != Level.NONE) {
						System.err.println("Method " + m.toString()
								+ " could not be added as a plugin. Some required indices are duplicated.");
					}
					it.remove();
				}
			}

			// Check for corresponding contexts at first indes
			it = pluginMethods.iterator();
			loop: while (it.hasNext()) {
				Method m = it.next();
				if (!isCorrectPluginContextType(m)) {
					if (Boot.VERBOSE != Level.NONE) {
						System.err
								.println("Method "
										+ m.toString()
										+ " could not be added as a plugin. The context should be asked as first parameter and should be a the same, or a superclass of "
										+ pluginContextType.getName() + ".");
					}
					it.remove();
					continue loop;
				}
			}

			it = pluginMethods.iterator();
			loop: while (it.hasNext()) {
				Method m = it.next();
				if ((returnTypes.length > 1) && !Object[].class.isAssignableFrom(m.getReturnType())
						&& !Object.class.equals(m.getReturnType())) {
					if (Boot.VERBOSE != Level.NONE) {
						System.err
								.println("Method "
										+ m.toString()
										+ " could not be added as a plugin. The plugin should return an array of objects as specified in the context.");
					}
					it.remove();
					continue loop;
				}
			}

			if (pluginMethods.isEmpty()) {
				if (Boot.VERBOSE != Level.NONE) {
					System.err.println("Plugin " + type.toString()
							+ " could not be added as a plugin. At least one variant has to be specified.");
				}
				return false;
			}

			return true;

		} catch (NoClassDefFoundError e) {
			// required class not on classpath, cannot load as plugin
			return false;
		}
	}

	private boolean isCorrectPluginContextType(Method method) {
		if (method.getParameterTypes().length == 0) {
			return false;
		}
		if (!PluginContext.class.isAssignableFrom(method.getParameterTypes()[0])) {
			return false;
		}
		if (!method.getParameterTypes()[0].isAssignableFrom(pluginContextType)) {
			return false;
		}
		return true;
	}

	private boolean isRightlyAnnotated(AnnotatedElement element) {
		if (element.getAnnotation(Plugin.class).returnLabels().length != element.getAnnotation(Plugin.class)
				.returnTypes().length) {
			if (Boot.VERBOSE != Level.NONE) {
				System.err.println("Plugin " + element.toString() + " could not be added as a plugin, the number of "
						+ "return types and return labels do not match.");
			}
			return false;
		}
		return true;
	}

	private boolean isGoodPlugin(Method method) throws NoClassDefFoundError {
		try {
			if (!isRightlyAnnotated(method)) {
				return false;
			}

			if ((method.getAnnotation(Plugin.class).parameterLabels().length != 0)
					&& (method.getAnnotation(Plugin.class).parameterLabels().length != method.getParameterTypes().length - 1)) {
				if (Boot.VERBOSE != Level.NONE) {
					System.err.println("Plugin " + method.toString() + " could not be added as a plugin, the number of"
							+ " parameter labels does not match the number of parameters.");
				}
				return false;
			}

			//		if (void.class.equals(method.getReturnType())) {
			//			System.err.println("Method " + method.toGenericString()
			//					+ " could not be added as a plugin, as the resultType is void.");
			//			return false;
			//		}

			Class<?>[] returnTypes = method.getAnnotation(Plugin.class).returnTypes();
			if ((returnTypes.length > 1) && !Object[].class.isAssignableFrom(method.getReturnType())
					&& !Object.class.equals(method.getReturnType())) {
				if (Boot.VERBOSE != Level.NONE) {
					System.err.println("Method " + method.toString()
							+ " could not be added as a plugin. The plugin should return an "
							+ "array of objects as specified in the context.");
				}
				return false;
			}

			Class<?>[] pars = method.getParameterTypes();
			if (!isCorrectPluginContextType(method)) {
				if (!method.isAnnotationPresent(Bootable.class)) {
					if (Boot.VERBOSE != Level.NONE) {
						System.err.println("Method " + method.toGenericString()
								+ " could not be added as a plugin, the first parameter has to be a "
								+ "PluginContext and assignable from " + pluginContextType.getName() + ".");
					}
				}
				return false;
			}
			for (int i = 1; i < pars.length; i++) {
				Class<?> type = pars[i];
				if (PluginContext.class.isAssignableFrom(type)) {
					if (Boot.VERBOSE != Level.NONE) {
						System.err.println("Method " + method.toGenericString()
								+ " could not be added as a plugin, only one context can be requested.");
					}
					return false;
				}
			}
			for (int i = 0; i < pars.length; i++) {
				if (pars[i].getTypeParameters().length > 0) {
					if (Boot.VERBOSE != Level.NONE) {
						System.err.println("Method " + method.toGenericString()
								+ " could not be added as a plugin, as one of the parameters "
								+ "is derived from a Type using Generics");
					}
					return false;
				}
			}
			return true;
		} catch (NoClassDefFoundError e) {
			// required class not on classpath, cannot load as plugin
			return false;
		}
	}

	public Set<Pair<Integer, PluginParameterBinding>> find(Class<? extends Annotation> annotation, Class<?> resultType,
			Class<? extends PluginContext> contextType, boolean totalMatch, boolean orderedParameters,
			boolean mustBeUserVisible, Class<?>... parameters) {

		Set<Pair<Integer, PluginParameterBinding>> result = new TreeSet<Pair<Integer, PluginParameterBinding>>();
		Set<PluginDescriptor> pls = annotation2plugins.get(annotation);
		if (pls == null) {
			return result;
		}
		for (PluginDescriptor plugin : pls) {
			if (mustBeUserVisible && (!plugin.meetsQualityThreshold() || !plugin.meetsLevelThreshold())) {
				/*
				 * Plug-in does not meet some required threshold to do so.
				 * Ignore it.
				 */
				continue;
			}
			if (!mustBeUserVisible || plugin.isUserAccessible()) {
				int i = (resultType == null ? 0 : plugin.getReturnTypes().indexOf(resultType));
				if (i < 0) {
					// Check for returned subtypes of the requested type
					i = checkIfRequestedReturnTypeIsPresent(plugin, resultType);
				}
				if (i >= 0) {
					for (int j = 0; j < plugin.getParameterTypes().size(); j++) {
						if (!plugin.getContextType(j).isAssignableFrom(contextType)) {
							// Check context types
							continue;
						}

						List<PluginParameterBinding> list = PluginParameterBinding.Factory.tryToBind(this, plugin, j,
								totalMatch, orderedParameters, parameters);
						for (PluginParameterBinding binding : list) {

							result.add(new ComparablePair<Integer, PluginParameterBinding>(i, binding));
							//							// Quit the loop since only one binding is to be
							//							// found.
							//							j = plugin.getParameterTypes().size();
						}
					}
				}
			}
		}
		return result;
	}

	private int checkIfRequestedReturnTypeIsPresent(PluginDescriptor plugin, Class<?> resultType) {
		for (int i = 0; i < plugin.getReturnTypes().size(); i++) {
			if (isParameterAssignable(plugin.getReturnTypes().get(i), resultType)) {
				return i;
			}
		}

		return -1;
	}

	public Collection<PluginDescriptor> find(String pluginName) {
		List<PluginDescriptor> result = new ArrayList<PluginDescriptor>();
		for (PluginDescriptor plugin : plugins.values()) {
			if (plugin.getName().equals(pluginName)) {
				result.add(plugin);
			}
		}
		return result;
	}

	public PluginDescriptor getPlugin(PluginDescriptorID id) {
		return plugins.get(id);
	}

	public PluginDescriptor getPlugin(String id) {
		for (Map.Entry<PluginDescriptorID, PluginDescriptor> entry : plugins.entrySet()) {
			if (entry.getKey().toString().equals(id)) {
				return entry.getValue();
			}
		}
		return null;
	}

	public Set<PluginParameterBinding> getPluginsAcceptingAtLeast(Class<? extends PluginContext> contextType,
			boolean mustBeUserVisible, Class<?>... parameters) {
		Set<PluginParameterBinding> result = new TreeSet<PluginParameterBinding>();
		for (Pair<Integer, PluginParameterBinding> pair : find(Plugin.class, null, contextType, false, false,
				mustBeUserVisible, parameters)) {
			result.add(pair.getSecond());
		}
		return result;
	}

	public Set<PluginParameterBinding> getPluginsAcceptingInAnyOrder(Class<? extends PluginContext> contextType,
			boolean mustBeUserVisible, Class<?>... parameters) {
		Set<PluginParameterBinding> result = new TreeSet<PluginParameterBinding>();
		for (Pair<Integer, PluginParameterBinding> pair : find(Plugin.class, null, contextType, true, false,
				mustBeUserVisible, parameters)) {
			result.add(pair.getSecond());
		}
		return result;
	}

	public Set<PluginParameterBinding> getPluginsAcceptingOrdered(Class<? extends PluginContext> contextType,
			boolean mustBeUserVisible, Class<?>... parameters) {
		Set<PluginParameterBinding> result = new TreeSet<PluginParameterBinding>();
		for (Pair<Integer, PluginParameterBinding> pair : find(Plugin.class, null, contextType, true, true,
				mustBeUserVisible, parameters)) {
			result.add(pair.getSecond());
		}
		return result;
	}

	public Set<Pair<Integer, PluginDescriptor>> getPluginsResultingIn(Class<?> resultType,
			Class<? extends PluginContext> contextType, boolean mustBeUserVisible) {
		Set<Pair<Integer, PluginDescriptor>> result = new TreeSet<Pair<Integer, PluginDescriptor>>();
		for (Pair<Integer, PluginParameterBinding> pair : find(Plugin.class, resultType, contextType, false, false,
				mustBeUserVisible)) {
			result.add(new ComparablePair<Integer, PluginDescriptor>(pair.getFirst(), pair.getSecond().getPlugin()));
		}
		for (Pair<Integer, PluginParameterBinding> pair : find(Plugin.class, resultType, contextType, true, false,
				mustBeUserVisible)) {
			result.add(new ComparablePair<Integer, PluginDescriptor>(pair.getFirst(), pair.getSecond().getPlugin()));
		}
		return result;
	}

	private SortedSet<PluginDescriptor> getAllPluginsSorted(boolean canBeUserVisible, boolean mustBeUserVisible) {
		SortedSet<PluginDescriptor> result = new TreeSet<PluginDescriptor>();
		for (PluginDescriptor plugin : plugins.values()) {
			boolean visible = plugin.isUserAccessible();
			if (mustBeUserVisible && (!plugin.meetsQualityThreshold() || !plugin.meetsLevelThreshold())) {
				/*
				 * Plug-in can be user visible (that is, should end up in the
				 * GUI), but does not meet some required threshold. Ignore it.
				 */
				continue;
			}
			// Do not include, if:
			// mustBeUserVisible AND NOT visible, OR
			// visible AND NOT canBeUserVisible
			if (!((mustBeUserVisible && !visible) || (!canBeUserVisible && visible))) {
				result.add(plugin);
			}
		}
		return Collections.unmodifiableSortedSet(result);
	}

	public SortedSet<PluginDescriptor> getAllPlugins() {
		return getAllPluginsSorted(true, false);
	}

	public SortedSet<PluginDescriptor> getAllPlugins(boolean mustBeVisible) {
		return getAllPluginsSorted(mustBeVisible, mustBeVisible);
	}

	public boolean isParameterAssignable(Class<?> instanceType, Class<?> requestedType) {
		if (requestedType.isAssignableFrom(instanceType)) {
			return true;
		}
		if (requestedType.isArray() && requestedType.getComponentType().isAssignableFrom(instanceType)) {
			return true;
		}
		return false;
	}

	public Set<Class<?>> getKnownObjectTypes() {
		return Collections.unmodifiableSet(knownObjectTypes);
	}

}