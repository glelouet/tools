package fr.lelouet.tools.lambdaref.withstore.references;

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

}