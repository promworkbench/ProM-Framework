package org.processmining.framework.plugin.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that is used as a base annotation for all classes that need to be
 * kept in cache by the ProM framework. When a class carries this annotation,
 * the ProM PluginManager will scan the class during first boot and store the
 * class in the cache for future reference.
 * 
 * This allows ProM to build a mapping from annotations to annotated classes
 * during boot.
 * 
 * @author bfvdonge
 * 
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface KeepInProMCache {

}
