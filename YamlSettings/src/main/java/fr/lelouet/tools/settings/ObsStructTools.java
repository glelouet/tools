package fr.lelouet.tools.settings;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/** observale structure tools */
public class ObsStructTools {

	/** copy the Observable fields from origin to dest */
	public static <T> void copy(T origin, T dest) {
		for (Method meth : dest.getClass().getMethods()) {
			if (meth.getName().startsWith("set")) {
				try {
					Method methGet = origin.getClass().getMethod(meth.getName().replaceFirst("set", "get"));
					if (methGet != null) {
						meth.invoke(dest, methGet.invoke(origin));
					}
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException
						| NoSuchMethodException e) {
					throw new UnsupportedOperationException("catch this", e);
				}
			}
		}
	}

}
