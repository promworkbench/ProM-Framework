package org.processmining.framework.plugin.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface Plugin {

	/**
	 * Returns the name identifying this plugin in a human readable way.
	 * 
	 * @return
	 */
	String name();

	/**
	 * Returns an Array of strings, such that each String is an initial label
	 * for a returned object. Plugins can change this label during execution.
	 * 
	 * The length of the array should be the same as the length of the
	 * returnTypes array.
	 * 
	 * @return
	 */
	String[] returnLabels();

	/**
	 * Returns an Array of Class objects, such that each class object represents
	 * the type of the returned object at that index, i.e. the plugin should
	 * return as many objects as the length of this array, and each object
	 * should be of the type given in this array.
	 * 
	 * @return
	 */
	Class<?>[] returnTypes();

	/**
	 * Returns the labels of the parameters for this plugin.
	 * 
	 * If the Plugin annotation is used on a method, then the number of elements
	 * in this array should correspond to the number of parameters that the
	 * method requires, not counting the first parameter of type PluginContext.
	 * 
	 * If the plugin annotation is used on a class, then each variant should
	 * indicate which subset of parameters it requires. Each parameter should
	 * appear in at least one variant.
	 * 
	 * Note that the types of the parameters are not specified in the
	 * annotation. Instead they are derived from the method definitions that
	 * contain the logic of this plugin. Parameters can be overloaded, i.e. the
	 * same parameter can have multiple types.
	 * 
	 * @return
	 */
	String[] parameterLabels();

	/**
	 * Returns the help / description for this plugin.
	 * 
	 * @return Empty string as default
	 */
	String help() default "";

	/**
	 * Signals the framework to make this plugin user-accessible. If a plugin is
	 * not user-accessible, it does not show up in any UI
	 * 
	 * @return Defaults to true.
	 */
	boolean userAccessible() default true;

	/**
	 * Returns the index + 1 in the result array of the most significant result
	 * for this plugin, i.e. if the first element in the array is the most
	 * significant, then the value of this parameter should be 1.
	 * 
	 * If the plugin does not return anything interesting itself, but considers
	 * one of it's inputs to be the most significant one, then a negative value
	 * should be returned. I.e. if the first parameter is the most significant
	 * result, a value of -1 has to be returned. Note that this parameter should
	 * always be included in all variants
	 * 
	 * @return the index in the result array of the most significant result. If
	 *         no results are returned by this plugin, anything can be returned.
	 *         Defaults to 1
	 * 
	 */
	int mostSignificantResult() default 1;

	/**
	 * Whether this plugin handles cancel itself. If this is true, the plug-in
	 * will not be killed but is allowed to terminate itself by monitoring
	 * isCancelled.
	 * 
	 * @return
	 */
	boolean handlesCancel() default false;

	/**
	 * Categories define the 'type' of functionality the plugin provides.
	 */
	PluginCategory[] categories() default { PluginCategory.Analytics };

	/**
	 * Keywords / tags for the plugin (extra description)
	 */
	String[] keywords() default {};
	
	/**
	 * Indication of quality for plug-in.
	 * @return
	 */
	PluginQuality quality() default PluginQuality.VeryPoor;
	/**
	 * Indication of level for plug-in.
	 * @return
	 */
	PluginLevel level() default PluginLevel.NightlyBuild;
}
