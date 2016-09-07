package org.processmining.framework.plugin.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PluginVariant {

	/**
	 * Each plugin variant specifies the required parameter labels in this
	 * Array. The array should be at most as long as the parameterLabels array
	 * of the Plugin annotation. Furthermore, all elements of the array should
	 * be indices in the parametersLabels array of the Plugin annotation and the
	 * elements should be ordered.
	 * 
	 * @return
	 */
	int[] requiredParameterLabels();

	/**
	 * This String represents the label of the plugin variant. This label is
	 * used to identify different variants of the same plugin, specifically when
	 * multiple variants accept the same parameters of the same types.
	 */
	String variantLabel() default "";

	/**
	 * Returns the help / description for this plugin variant.
	 * 
	 * @return Empty string as default
	 */
	String help() default "";

}
