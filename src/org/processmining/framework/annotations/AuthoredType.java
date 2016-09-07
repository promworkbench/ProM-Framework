package org.processmining.framework.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.TYPE })
public @interface AuthoredType {

	public final static String TUE = "Eindhoven University of Technology";

	/**
	 * Specifies the user-readable type for this class
	 * 
	 * @return
	 */
	String typeName();

	/**
	 * Specifies the affiliation of the author
	 * 
	 * @return
	 */
	String affiliation();

	/**
	 * specifies the e-mail address of the author
	 * 
	 * @return
	 */
	String email();

	/**
	 * Specifies the name of the author
	 * 
	 * @return
	 */
	String author();

	/**
	 * Specifies the website of the author (note that this should be URL style,
	 * i.e. with http://)
	 * 
	 * @return
	 */
	String website() default "http://www.processmining.org";

}
