package org.processmining.framework.plugin;

import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Collection;
import java.util.EventListener;
import java.util.Set;
import java.util.SortedSet;

import org.processmining.framework.packages.PackageDescriptor;
import org.processmining.framework.util.Pair;

/**
 * The plugin manager manages plugins. It loads plugins from URLs and provides
 * access to them through search methods.
 * 
 * @author bfvdonge
 * 
 */
public interface PluginManager {

	/**
	 * Constant to denote the file extension for class files.
	 */
	static final String CLASS_EXTENSION = ".class";

	/**
	 * Constant to denote the file extension for macro files.
	 */
	static final String MCR_EXTENSION = ".mcr";

	/**
	 * Constant to denote the file extension for jar files.
	 */
	static final String JAR_EXTENSION = ".jar";

	/**
	 * Constant to denote the file protocol to be used in URLs provided to this
	 * plugin manager.
	 */
	static final String FILE_PROTOCOL = "file";

	/**
	 * This interface describes an error listener for the plugin manager. The method
	 * error should be called by a plugin manager while registering packages and
	 * scanning for plugins.
	 * 
	 * @author bfvdonge
	 * 
	 */
	interface PluginManagerListener extends EventListener {
		/**
		 * Listen for errors which occur while registering packages and scanning for
		 * plugins.
		 * 
		 * @param source
		 *            The URL which was being registered while this error occurred
		 * @param t
		 *            The error which occurred
		 * @param className
		 *            The class name of the class which was being registered, may be
		 *            null
		 */
		void error(URL source, Throwable t, String className);

		/**
		 * Listen for plugins that are registered to the manager. Every time a
		 * PluginDescriptor is added to the manager, an event is generated.
		 * 
		 * @param plugin
		 */
		void newPlugin(PluginDescriptor plugin, Collection<Class<?>> newTypes);
	}

	/**
	 * Adds a listener to the plugin manager.
	 * 
	 * @param listener
	 *            the listener to add.
	 */
	void addListener(PluginManagerListener listener);

	/**
	 * Removes the listener from the plugin manager.
	 * 
	 * @param listener
	 *            the listener to remove.
	 */
	void removeListener(PluginManagerListener listener);

	/**
	 * registers a URL to this plugin manager. If the URL uses the
	 * <code>FILE_PROTOCOl</code> protocol and denotes a directory, then this folder
	 * is recursively scanned for files with the <code>CLASS_EXTENSION</code>
	 * extension.
	 * 
	 * Otherwise, the URL is assumed to point to a jar file, of which the classes
	 * are scanned.
	 * 
	 * Each class file is scanned for classes and/or methods annotated with the
	 * <code>Plugin</code> annotation. If a class is annotated with this annotation,
	 * then its methods are scanned for the <code>PluginVariant</code> annotation.
	 * 
	 * For each plugin found, a <code>PluginDescriptor</code> object is
	 * instantiated. These plugin descriptors can later be used to invoke plugins.
	 * 
	 * @param url
	 *            The URL to register
	 * @param pack
	 *            The package that corresponds to the URL
	 */
	void register(URL url, PackageDescriptor pack);

	/**
	 * registers a URL to this plugin manager. If the URL uses the
	 * <code>FILE_PROTOCOl</code> protocol and denotes a directory, then this folder
	 * is recursively scanned for files with the <code>CLASS_EXTENSION</code>
	 * extension.
	 * 
	 * Otherwise, the URL is assumed to point to a jar file, of which the classes
	 * are scanned.
	 * 
	 * Each class file is scanned for classes and/or methods annotated with the
	 * <code>Plugin</code> annotation. If a class is annotated with this annotation,
	 * then its methods are scanned for the <code>PluginVariant</code> annotation.
	 * 
	 * For each plugin found, a <code>PluginDescriptor</code> object is
	 * instantiated. These plugin descriptors can later be used to invoke plugins.
	 * 
	 * @param url
	 *            The URL to register
	 * @param pack
	 *            The package that corresponds to the URL
	 * @param loader
	 *            The class loader used to load the jar files.
	 */
	void register(URL url, PackageDescriptor pack, ClassLoader loader);

	/**
	 * This method retuns a collection of <code>Pair</code>s of <code>Integer</code>
	 * and <code>PluginParameterBinding</code> objects, such that:
	 * <p>
	 * The method belonging to the plugin in the pluginParameterBinding carries the
	 * given annotation. If no specific annotation is required, the method should be
	 * called with <code>Plugin.class</code>. Note that the annotation required has
	 * to be on the same level as the <code>Plugin</code> annotation, i.e. either on
	 * a method or a class
	 * <p>
	 * When invoked, the PluginParameterBinding returns an array of objects, of
	 * which the object at the index given by the integer in the pair is of the
	 * required result type, i.e.
	 * <code>resultType.isAssignableFrom(p.getPlugin().getReturnTypes()[i])</code>
	 * If no specific return type is required, use <code>Object.class</code> for
	 * this parameter.
	 * <p>
	 * The PluginParameterBinding can be executed in a <code>PluginContext</code> of
	 * the given type.
	 * <p>
	 * If <code>totalMatch</code> is true, then the PluginParameterBinding binds all
	 * parameters of the plugin with all arguments. Otherwise, the
	 * PluginParameterBinding only requires a subset of the given arguments and is
	 * therefore not directly executable on the given set of arguments.
	 * <p>
	 * If <code>orderedParameters</code> is true, then the PluginParameterBinding
	 * binds the given arguments in the given order, i.e. no arguments are
	 * reordered.
	 * <p>
	 * If <code>mustBeUserVisible</code> is true, then the plugin must have the
	 * <code>isUserVisible</code> flag set to true.
	 * <p>
	 * The PluginParameterBinding can be executed on arguments of the given types.
	 * The list of arguments can be empty, in which case no arguments are required
	 * to invoke the PluginParameterBinding. Note that only types of arguments are
	 * required, not the values. For checking whether arguments can be assigned to
	 * parameters of the Plugin, the <code>isParameterAssignable</code> method is
	 * used. Any <code>ProMFuture</code>s should be unwrapped.
	 * 
	 * @param annotation
	 *            The annotation that should be present on the plugin (use
	 *            <code>Plugin.class</code> if none is required).
	 * @param resultType
	 *            The required result type (use <code>Object.class</code> if no
	 *            specific type is required).
	 * @param contextType
	 *            The context type in which this plugin should be executable. Note
	 *            that this type should be the contextType of the context from which
	 *            the find is called, or a supertype thereof.
	 * @param totalMatch
	 *            Whether or not all arguments should be used to execute this
	 *            plugin.
	 * @param orderedParameters
	 *            Whether or not the arguments are given in the right order.
	 * @param mustBeUserVisible
	 *            Whether or not all returned plugins should be user visible.
	 * @param args
	 *            The types of the arguments provided to the plugin. Can be empty.
	 * @return A collection of pluginparameterbindings. They are executable if
	 *         totalMatch is true.
	 */
	Set<Pair<Integer, PluginParameterBinding>> find(Class<? extends Annotation> annotation, Class<?> resultType,
			Class<? extends PluginContext> contextType, boolean totalMatch, boolean orderedParameters,
			boolean mustBeUserVisible, Class<?>... args);

	/**
	 * This method retuns a collection of <code>Pair</code>s of <code>Integer</code>
	 * and <code>PluginParameterBinding</code> objects, such that:
	 * <p>
	 * The method belonging to the plugin in the pluginParameterBinding carries the
	 * given annotation. If no specific annotation is required, the method should be
	 * called with <code>Plugin.class</code>. Note that the annotation required has
	 * to be on the same level as the <code>Plugin</code> annotation, i.e. either on
	 * a method or a class
	 * <p>
	 * When invoked, the PluginParameterBinding returns an array of objects, of
	 * which the object at the index given by the integer in the pair is of the
	 * required result type as specified in the input list, i.e. for all
	 * <code>i</code>
	 * <code>resultTypes[i].isAssignableFrom(p.getPlugin().getReturnTypes()[i])</code>
	 * If no specific return type is required, use the other <code>find</code>
	 * method.
	 * <p>
	 * The PluginParameterBinding can be executed in a <code>PluginContext</code> of
	 * the given type.
	 * <p>
	 * If <code>totalMatch</code> is true, then the PluginParameterBinding binds all
	 * parameters of the plugin with all arguments. Otherwise, the
	 * PluginParameterBinding only requires a subset of the given arguments and is
	 * therefore not directly executable on the given set of arguments.
	 * <p>
	 * If <code>orderedParameters</code> is true, then the PluginParameterBinding
	 * binds the given arguments in the given order, i.e. no arguments are
	 * reordered.
	 * <p>
	 * If <code>mustBeUserVisible</code> is true, then the plugin must have the
	 * <code>isUserVisible</code> flag set to true.
	 * <p>
	 * The PluginParameterBinding can be executed on arguments of the given types.
	 * The list of arguments can be empty, in which case no arguments are required
	 * to invoke the PluginParameterBinding. Note that only types of arguments are
	 * required, not the values. For checking whether arguments can be assigned to
	 * parameters of the Plugin, the <code>isParameterAssignable</code> method is
	 * used. Any <code>ProMFuture</code>s should be unwrapped.
	 * 
	 * @param annotation
	 *            The annotation that should be present on the plugin (use
	 *            <code>Plugin.class</code> if none is required).
	 * @param resultTypes
	 *            The exact, sorted list of required result types. If not specific
	 *            type is requested, this find method should not be used.
	 * @param contextType
	 *            The context type in which this plugin should be executable. Note
	 *            that this type should be the contextType of the context from which
	 *            the find is called, or a supertype thereof.
	 * @param totalMatch
	 *            Whether or not all arguments should be used to execute this
	 *            plugin.
	 * @param orderedParameters
	 *            Whether or not the arguments are given in the right order.
	 * @param mustBeUserVisible
	 *            Whether or not all returned plugins should be user visible.
	 * @param args
	 *            The types of the arguments provided to the plugin. Can be empty.
	 * @return A collection of pluginparameterbindings. They are executable if
	 *         totalMatch is true.
	 */
	public Set<Pair<Integer, PluginParameterBinding>> find(Class<? extends Annotation> annotation,
			Class<?>[] resultTypes, Class<? extends PluginContext> contextType, boolean totalMatch,
			boolean orderedParameters, boolean mustBeUserVisible, Class<?>... parameters);

	/**
	 * Find the plugins resulting in the given type. The result are pairs of
	 * integers and plugins, such that for each pair (i,p) holds that
	 * resultType.isAssignableFrom(p.getReturnTypes()[i])
	 * 
	 * @param resultType
	 *            Can be null. if null, then any type is considered.
	 * @param mustBeUserVisible
	 *            Whether or not all returned plugins should be user visible.
	 * @return A collection of pairs of integers and plugins, such that for each
	 *         pair (i,p) holds that
	 *         resultType.isAssignableFrom(p.getReturnTypes()[i])
	 */
	Set<Pair<Integer, PluginDescriptor>> getPluginsResultingIn(Class<? extends Object> resultType,
			Class<? extends PluginContext> contextType, boolean mustBeUserVisible);

	/**
	 * Returns executable PluginParameterBindings, which can be invoked in the given
	 * context on the given parameter types. Note that the PluginParameterBindings
	 * are executable.
	 * 
	 * @param contextType
	 *            The type of the context in which the binding is to be invoked.
	 * @param mustBeUserVisible
	 *            Whether or not the plugin should be user visible.
	 * @param parameters
	 *            The types of the arguments passed to the plugins. They are
	 *            accepted by the plugin in the order in which they are provided.
	 * 
	 * @return a list of executable bindings
	 */
	Set<PluginParameterBinding> getPluginsAcceptingOrdered(Class<? extends PluginContext> contextType,
			boolean mustBeUserVisible, Class<?>... parameters);

	/**
	 * Returns PluginParameterBindings, which can be invoked in the given context on
	 * the given parameter types. Note that the PluginParameterBindings are not
	 * necessarily executable. However, they accept all given arguments as
	 * parameters.
	 * 
	 * @param contextType
	 *            The type of the context in which the binding is to be invoked.
	 * @param mustBeUserVisible
	 *            Whether or not the plugin should be user visible.
	 * @param parameters
	 *            The types of the arguments passed to the plugins. They are
	 *            accepted by the returned plugins, but not necessarily in this
	 *            order.
	 * @return a list of bindings
	 */
	Set<PluginParameterBinding> getPluginsAcceptingAtLeast(Class<? extends PluginContext> contextType,
			boolean mustBeUserVisible, Class<?>... parameters);

	/**
	 * Returns executable PluginParameterBindings, which can be invoked in the given
	 * context on the given parameter types. Note that the PluginParameterBindings
	 * are executable.
	 * 
	 * @param contextType
	 *            The type of the context in which the binding is to be invoked.
	 * @param mustBeUserVisible
	 *            Whether or not the plugin should be user visible.
	 * @param parameters
	 *            The types of the arguments passed to the plugins. They are
	 *            accepted by the returned plugins, but not necessarily in this
	 *            order.
	 * @return a list of executable bindings
	 */
	Set<PluginParameterBinding> getPluginsAcceptingInAnyOrder(Class<? extends PluginContext> contextType,
			boolean mustBeUserVisible, Class<?>... parameters);

	/**
	 * Returns a PluginDescriptor with the given id. Note that plugin IDs are
	 * persistent between runs.
	 * 
	 * @param id
	 *            the id of the plugin to get
	 * @return the plugin with the given id.
	 */
	PluginDescriptor getPlugin(PluginDescriptorID id);

	/**
	 * Returns a PluginDescriptor of which the toString() of its id equals the given
	 * id. Note that plugin IDs are persistent between runs.
	 * 
	 * @param id
	 *            the String representation of the id of the plugin to get
	 * @return the plugin with an id of which the String representation equals the
	 *         given id.
	 */
	PluginDescriptor getPlugin(String id);

	/**
	 * Returns all plugin descriptors
	 * 
	 * @return all plugin descriptors known to the plugin manager.
	 */
	SortedSet<PluginDescriptor> getAllPlugins();

	/**
	 * Returns all plugin descriptors known to the plugin manager. If set, only
	 * those plugins which are user visible are returned.
	 * 
	 * @param mustBeVisible
	 *            wether or not the returned plugins should be user visible.
	 * @return the plugin descriptors.
	 */
	SortedSet<PluginDescriptor> getAllPlugins(boolean mustBeVisible);

	/**
	 * Returns true if the instance type can be cast to the requested type, or if
	 * the requested type is an array and the instance type can be cast to the
	 * component type of the requested type.
	 * 
	 * @param instanceType
	 *            the type that has to be cast to the requested type.
	 * @param requestedType
	 *            the requested type
	 * @return true if a cast can be made, i.e. if an object of type instanceType
	 *         can be assigned to a parameter of type requestedType of a plugin.
	 */
	boolean isParameterAssignable(Class<?> instanceType, Class<?> requestedType);

	/**
	 * Returns the set of types that is known to the plugin manager. Basically, this
	 * set contains all types that are ever used as a parameter or a return type of
	 * a plugin.
	 * 
	 * @return a set of types.
	 */
	Set<Class<?>> getKnownObjectTypes();

	/**
	 * Returns all known classes annotated with a certain annotationType. Not all of
	 * these classes are plugins! Note that only classes are available that carry
	 * the @KeepInProMCache annotation
	 * 
	 * @param annotationType
	 *            the type of annotation to be found
	 * @return a (possibly empty) set of classes (not null)
	 */
	Set<Class<?>> getKnownClassesAnnotatedWith(Class<? extends Annotation> annotationType);

}