package fr.lelouet.tools.lambdaref.withstore;

/**
 * Interface for classes that can store references in order to avoid a lambda to
 * be garbage collected.
 *
 * @author
 *
 */
public interface RefStore {

	/**
	 * request to keep a reference to an object. That object won't become weak
	 * referenced until this becomes also weak referenced.
	 *
	 * @param o
	 */
	public void store(Object o);

}
