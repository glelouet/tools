package fr.lelouet.collectionholders.interfaces;

/**
 * a {@link ObjHolder} that can be set its value.
 * 
 * @param <U>
 *          the value it holds.
 */
public interface RWObjHolder<U> extends ObjHolder<U> {

	/**
	 * set the value to hold. This should send the value to all the followers.
	 * 
	 * @param item
	 *          value to set.
	 */
	public void set(U item);

}
