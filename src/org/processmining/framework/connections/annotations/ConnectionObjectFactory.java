package org.processmining.framework.connections.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used on plugins to signal that they can construct a Connection on
 * objects.
 * 
 * The type of connection produced is given by the return type of the plugin, of
 * which there should be only 1, which is assignable from Connection.class.
 * 
 * To avoid lifelocks such a plugin should not request any connection on its
 * inputs. If no connection can be made, <code>null</code> should be returned.
 * 
 * @author bfvdonge
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.METHOD, ElementType.TYPE })
public @interface ConnectionObjectFactory {

}
