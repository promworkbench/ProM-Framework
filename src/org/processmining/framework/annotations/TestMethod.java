package org.processmining.framework.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * By adding this annotation to a method, the method is flagged as a test method
 * for ProM. A hudson server should, on each commit, find all methods annotated
 * with this annotation and exectute it.
 * 
 * The method should not require parameters and should be declared static. The
 * return type of the method should be: <code>String</code>.
 * 
 * The string returned by this method is compared to either the string defined
 * in the output field, or with the contents of the file indicated by
 * filename().
 * 
 * If both filename and output are specified, then only output is used and the
 * file is ignored.
 * 
 * @author bfvdonge
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface TestMethod {

	String filename() default "";

	String output() default "";
	
	boolean returnSystemOut() default false;

}
