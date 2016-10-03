package fr.lelouet.tools.cache;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * tooling for cache cleaning. Provides a default method to be inherited, or a
 * static method to call.
 * </p>
 * <p>
 * This class can be inherited to provide the {@link #cleanCache()} method that
 * Introspect into the class and set every attribute marked with {@link Cached}
 * to the default value, defaulting to null.
 * </p>
 */
public abstract class UseCache {

	private static final Logger logger = LoggerFactory
			.getLogger(UseCache.class);

	/** we stop getting the superclass when we reach this one. */
	private static final Class<Object> FINAL_CLASS = Object.class;

	/**
	 * set every field of the object that is annotated with {@link Cached} to
	 * null, in this object's class or it super classes. No check is done on the
	 * presence of the {@link Cached} marker on the classes.
	 */
	public static void cleanCache(Object target) {
		logger.debug("cleanning cache of {}", target);
		for (Class<?> c = target.getClass(); c != FINAL_CLASS
				&& !c.isInterface(); c = c.getSuperclass()) {
			cleanClassLevelCache(target, c);
		}
	}

	public static final Object[] NOARGS = new Object[] {};
	public static final Class<?>[] NOPARAMS = new Class[] {};

	/** clean the cache */
	public static void cleanClassLevelCache(Object target, Class<?> scopeClass) {
		for (Field f : scopeClass.getDeclaredFields()) {
			Cached cache = f.getAnnotation(Cached.class);
			if (cache != null) {
				try {
					Object defaultVal = null;
					String methodName = cache.value();
					if (methodName != null
							&& !Cached.FAKENULL.equals(methodName)) {
						Method m = target.getClass().getMethod(methodName,
								NOPARAMS);
						if (m != null) {
							try {
								defaultVal = m.invoke(target, new Object[] {});
								logger.debug(
										"defaulting value to {} with method {}",
										new Object[] { defaultVal, m });
							} catch (Exception e) {
								logger.debug(
										"while retrieving default value for method "
												+ m, e);
							}
						} else {
							logger.debug(
									"could not find method {} to call on object {}; available methods are {}",
									new Object[] {
											methodName,
											target.getClass()
													.getCanonicalName(),
											target.getClass().getMethods() });
						}
					}
					f.setAccessible(true);
					if (defaultVal == null) {
						defaultVal = getDefaultVal(f.getType());
					}
					logger.debug("setting {}.{} = {}",
							new Object[] { target, f.getName(), defaultVal });
					f.set(target, defaultVal);
				} catch (Exception e) {
					logger.error(
							"while erasing ((" + scopeClass.getCanonicalName()
									+ ")" + target + ")." + f.getName() + " : ",
							e);
				}
			} else {
				// logger.debug("skipping {}", f);
			}
		}
	}

	public static enum DefaultValues {
		INT("int", 0), LONG("long", 0), DOUBLE("double", 0.0), BOOLEAN(
				"boolean", false);

		protected final String classType;
		protected final Object defaultVal;

		private DefaultValues(String javaclassType, Object defaultValue) {
			classType = javaclassType;
			defaultVal = defaultValue;
		}

		public boolean accept(Class<?> targetClass) {
			return targetClass.getCanonicalName().equals(classType);
		}
	}

	protected static Object getDefaultVal(Class<?> targetClass) {
		for (DefaultValues dv : DefaultValues.values()) {
			if (dv.accept(targetClass)) {
				return dv.defaultVal;
			}
		}
		return null;
	}

	public void cleanCache() {
		cleanCache(this);
	}

}
