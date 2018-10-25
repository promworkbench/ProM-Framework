package org.processmining.framework.plugin;

import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;

import org.processmining.framework.packages.PackageDescriptor;

public interface PluginDescriptor extends Comparable<PluginDescriptor> {

	/**
	 * Check if this plugin carries the given annotation. Implementing classes
	 * can return false at their discretion, i.e. they are not required to
	 * return true for all annotations carried by the plugin.
	 * 
	 * @param annotationClass
	 * @return
	 */
	boolean hasAnnotation(Class<? extends Annotation> annotationClass);

	/**
	 * Check if the method at the given index carries the given annotation.
	 * Implementing classes can return false at their discretion, i.e. they are
	 * not required to return true for all annotations carried by the plugin.
	 * 
	 * @param annotationClass
	 * @return
	 */
	boolean hasAnnotation(Class<? extends Annotation> annotationClass, int methodIndex);

	/**
	 * Return the annotation of the given class carried by this plugin. Note
	 * that hasAnnotationClass(annotationClass) is assumed.
	 * 
	 * This method should not be used to access any annotation elements carried
	 * by Plugin.class. Implementing classes are allows to return null when
	 * called with Plugin.class.
	 * 
	 * @param <T>
	 * @param annotationClass
	 * @return
	 */
	<T extends Annotation> T getAnnotation(Class<T> annotationClass);

	/**
	 * Return the annotation of the method at the given index. Note that
	 * hasAnnotationClass(annotationClass, methodIndex) is assumed.
	 * 
	 * This method should not be used to access any annotation elements carried
	 * by Plugin.class. Implementing classes are allows to return null when
	 * called with Plugin.class.
	 * 
	 * @param <T>
	 * @param annotationClass
	 * @param methodIndex
	 * @return
	 */
	<T extends Annotation> T getAnnotation(Class<T> annotationClass, int methodIndex);

	/**
	 * Return the package where the plug-in resides.
	 * 
	 * @return
	 */
	PackageDescriptor getPackage();

	/**
	 * Return the name of the plugin. This name is not unique within ProM.
	 * 
	 * @return
	 */
	String getName();

	/**
	 * return hashcode
	 * 
	 * @return
	 */
	int hashCode();

	/**
	 * return equality of plugins. The ID is used for equality.
	 * 
	 * @param other
	 * @return
	 */
	boolean equals(Object other);

	/**
	 * Returns a String representation of the plugin
	 * 
	 * @return
	 */
	String toString();

	/**
	 * Return the number of methods in this plugin. There's always at least 1.
	 * 
	 * @return
	 */
	int getNumberOfMethods();

	/**
	 * Asynchronously invokes a method of this plugin. The methodIndex should
	 * refer to an existing method, i.e. 0 <= methodindex <
	 * getNumberOfMethods();
	 * 
	 * Note that the callers of this plugin should notify the lifeCycleListners
	 * of the given context of the creation of this plugin, i.e. they should
	 * call:
	 * <code>context.getParentContext().getPluginLifeCycleEventListeners().firePluginCreated(context);</code>
	 * 
	 * @param methodIndex
	 *            The index of the method to be invoked
	 * @param context
	 *            the context in which this plugin should be invoked. The plugin
	 *            may assume that this context is meant only for its execution.
	 * @param args
	 *            the objects to be passed to the plugin. These objects should
	 *            be in the right order. However, if <code>ProMFuture</code>
	 *            objects are provided, the plugin will synchronize on them.
	 *            This syncrhonization is performed in a fast-fail fashion, i.e.
	 *            the first future that results in an exception during execution
	 *            will result in an exception while invoking this plugin. This
	 *            exception is caught and the context is signaled about it.
	 * @return a PluginExecutionResult holding pointers to the future outcome of
	 *         this invokation. If synchrounous execution is required, the
	 *         calling method should synchronize on this result
	 */
	PluginExecutionResult invoke(int methodIndex, PluginContext context, Object... args);

	/**
	 * returns the types of the objects returned if this plugin is invoked, in
	 * the order in which they are returned
	 * 
	 * @return
	 */
	List<Class<?>> getReturnTypes();

	/**
	 * returns the labels of the objects returned if this plugin is invoked, in
	 * the order in which they are returned. These are the labels provided by
	 * the plugin definition, not the labels of the objects resulting from a
	 * specific invokation, as these can be obtained through the
	 * <code>PluginExecutionResult</code> object.
	 * 
	 * @return
	 */
	List<String> getReturnNames();

	/**
	 * returns a list of size <code>getNumberOfMethods()</code>, such that
	 * element at index <code>i</code> of the list equals
	 * <code>getParameterTypes(i)</code>
	 * 
	 * @return
	 */
	List<List<Class<?>>> getParameterTypes();

	/**
	 * Returns a list of types that represent the required parameters of the
	 * method at the given index. In other words, the method at index
	 * <code>methodIndex</code> requires exectly the parameter types as provided
	 * in the returned list, in that order.
	 * 
	 * @param methodIndex
	 * @return
	 */
	List<Class<?>> getParameterTypes(int methodIndex);

	/**
	 * Returns a list of labels that represent the possible parameters of this
	 * plugin. Each method required a subset of these labels, while preserving
	 * their order.
	 * 
	 * @return
	 */
	List<String> getParameterNames();

	/**
	 * Returns a list of labels that represent the required parameters of the
	 * method at the given index. In other words, the method at index
	 * <code>methodIndex</code> requires exactly the parameter labels as
	 * provided in the returned list, in that order.
	 * 
	 * @param methodIndex
	 * @return
	 */
	List<String> getParameterNames(int methodIndex);

	/**
	 * Return the type of the parameter at index <code>parameterIndex</code> of
	 * the method at <code>methodIndex</code>.
	 * 
	 * @param methodIndex
	 *            <code>0 <= methodIndex < getNumberOfMethods()</code>
	 * @param parameterIndex
	 *            <code>0 <= parameterIndex < getParameterTypes().size()</code>
	 * @return may return null if the parameter is not used by the given method
	 */
	Class<?> getPluginParameterType(int methodIndex, int parameterIndex);

	/**
	 * Return the label of the parameter at index <code>parameterIndex</code> of
	 * the method at <code>methodIndex</code>.
	 * 
	 * @param methodIndex
	 *            <code>0 <= methodIndex < getNumberOfMethods()</code>
	 * @param parameterIndex
	 *            <code>0 <= parameterIndex < getParameterTypes(methodIndex).size()</code>
	 * @return may return null if the parameter is not used by the given method
	 */
	String getPluginParameterName(int methodIndex, int parameterIndex);

	/**
	 * returns the ID of this Plugin. Provided that the code of a plugin does
	 * not change, these IDs are persistent between executions of ProM.
	 * 
	 * @return
	 */
	PluginDescriptorID getID();

	/**
	 * Returns the context type required by the method at index methodIndex.
	 * 
	 * @param methodIndex
	 *            <code>0 <= methodIndex < getNumberOfMethods()</code>
	 * @return
	 */
	Class<? extends PluginContext> getContextType(int methodIndex);

	/**
	 * Returns a list of types that can be accepted for the parameter at index
	 * <code>globalParameterIndex</code>. The parameter index is global, i.e.
	 * not method specific. Instead, for each type <code>t</code> in the set
	 * returned by this method it is guaranteed that there is at least one
	 * method (suppose at index <code>i</code>), such that
	 * <code>getParameterType(i, getIndexInMethod(i, globalParameterIndex)) == t</code>
	 * 
	 * @param globalParameterIndex
	 *            <code>0 <= globalParameterIndex < getParameterNames().size()</code>
	 * @return
	 */
	Set<Class<?>> getTypesAtParameterIndex(int globalParameterIndex);

	/**
	 * Returns the index of the method parameter of the given method in the list
	 * of global parameters. Each method of a plugin requires a subset of the
	 * global set of parameters that the plugin defines. This method can be used
	 * to map the index of each method parameter to the index of the global
	 * parameter.
	 * 
	 * @param methodIndex
	 *            <code>0 <= methodIndex < getNumberOfMethods()</code>
	 * @param methodParameterIndex
	 *            <code>0 <= methodParameterIndex < getParameterNames(methodIndex).size()</code>
	 * @return <code>0 <= return < getParameterNames().size()</code>
	 */
	int getIndexInParameterNames(int methodIndex, int methodParameterIndex);

	/**
	 * Returns the index of the global parameter to the index of that parameter
	 * in the given method. Each method of a plugin requires a subset of the
	 * global set of parameters that the plugin defines. This method can be used
	 * to map the index of each global parameter to the index of the method
	 * parameter. Note that if the given method does not require the global
	 * parameter, -1 is returned.
	 * 
	 * @param methodIndex
	 *            <code>0 <= methodIndex < getNumberOfMethods()</code>
	 * @param globalParameterIndex
	 *            <code>0 <= globalParameterIndex < getParameterNames().size()</code>
	 * @return <code>-1 <= return < getParameterNames(methodIndex).size()</code>
	 */
	int getIndexInMethod(int methodIndex, int globalParameterIndex);

	/**
	 * Return the label of the given method, if specified. If no label was
	 * specified, the name of the plugin is returned.
	 * 
	 * @param methodIndex
	 * @return
	 */
	String getMethodLabel(int methodIndex);

	/**
	 * Compares this plugin with another plugins. Uses the
	 * <code>getName().toLowerCase()</code> for comparing. Only if names are
	 * equal, then the IDs are used for comparison. Comparison should be
	 * name-based between different implementing classes.
	 */
	int compareTo(PluginDescriptor plugin);

	/**
	 * Returns true if this plugin can be used by the end-users. If this is set
	 * to false, such a plugin should not be presented to the end user by any
	 * context.
	 * 
	 * @return
	 */
	boolean isUserAccessible();

	/**
	 * Returns whether the plugin handles termination itself.
	 * 
	 * @return
	 */
	boolean handlesCancel();

	/**
	 * Returns the index in the result array of the most significant result for
	 * this plugin
	 * 
	 * @return the index in the result array of the most significant result. If
	 *         no results are returned by this plugin, anything can be returned.
	 */
	int getMostSignificantResult();

	/**
	 * Return the help / description of the plugin.
	 * 
	 * @return
	 */
	String getHelp();

	/**
	 * Return the help / description of the given method, if specified.
	 * 
	 * @param methodIndex
	 * @return
	 */
	String getMethodHelp(int methodIndex);
	
	
	/**
	 * Return the set of keywords.
	 * 
	 * @return a set of keywords
	 */
	String[] getKeywords();
	
	/**
	 * Return the set of categories.
	 * 
	 * @return a set of categories
	 */
	String[] getCategories();

	/**
	 * Return whether this plug-in meets the quality threshold.
	 * 
	 * @return whether this plug-in meets the quality threshold
	 */
	boolean meetsQualityThreshold();
	/**
	 * Return whether this plug-in meets the level threshold.
	 * 
	 * @return whether this plug-in meets the level threshold
	 */
	boolean meetsLevelThreshold();
	
	public ImageIcon getIcon();
	
	public URL getURL();

}
