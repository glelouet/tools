package fr.lelouet.tools.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Mark an attribute as being a cache for a method.
 * </p>
 * <p>
 * An attribute cached is supposed to be set to a default value when cleaning
 * the cache of an object. The introspectional clean cache actually searches for
 * attributes marked as cached and set them to the default value.
 * </p>
 * <p>
 * The default value is specified as a method to call. If the method is not
 * found, or does not return the correct class, or crashes, then the default
 * value of null is used instead.<br />
 * Such a method should not use any parameter, be public, and return an Object.
 * eg, if the class implements the function Object defaultVar(), then the value
 * can be "defaultVar"<br />
 * </p>
 * 
 * @see UseCache the method that uses this annotation
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface Cached {

	public static final String FAKENULL = "USELESS STRING BECAUSE ANNOTATIONS CANNOT USE NULL VALUE";

	/** @return the level of the cache. */
	String value() default FAKENULL;

}
