package fr.lelouet.tools.lambdaref.references;

/**
 * a ref on a non null item. Used for mixing of weak reference( {@link WeakRef}
 * ) and strong reference(called {@link UsualRef} ). Only provide a
 * {@link #get()} method which should either return the refered item or null if
 * that item was garbage colected.
 *
 * @param <U>
 *          type of the object ot refer to.
 */
public interface IRef<U> {

	/**
	 * @return the refered item, or null if it's been collected.
	 */
	public U get();

	public static <V> IRef<V> weak(V item) {
		return new WeakRef<>(item);
	}

	public static <V> IRef<V> strong(V item) {
		return new UsualRef<>(item);
	}

	public static <V> IRef<V> hard(V item) {
		return new HardRef<>(item);
	}

}