package fr.lelouet.tools.lambdaref;

import java.lang.ref.WeakReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GCManage {

	private static final Logger logger = LoggerFactory.getLogger(GCManage.class);

	/**
	 * force the garbage collector to perform cleaning on weak references. This is
	 * done by creating a weak reference on a new object, then allocating arrays
	 * of arbitrary size until the reference returns null.
	 */
	public static void force() {
		WeakReference<Object> wr = new WeakReference<>(new Object());
		int nb = 0;
		int arraySize = (int) (Math.min(Runtime.getRuntime().freeMemory() / 32 / 8, Integer.MAX_VALUE - 5) / 10);
		while (wr.get() != null) {
			@SuppressWarnings("unused")
			Object[] trash = new Object[arraySize];
			System.gc();
			nb++;
		}
		boolean debug = false;
		if (debug) {
			logger.debug("gc after " + nb + " buffers");
		}
	}

}
