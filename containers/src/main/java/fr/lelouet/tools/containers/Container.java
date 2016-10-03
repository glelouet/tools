package fr.lelouet.tools.containers;

/**
 * contains one element, and allow to get and set it. Not synchronized.
 * <p>
 * the access and modification of the element, through {@link #get()} and
 * {@link #set(Object)}, calls the {@link #afterGet(Object)} and
 * {@link #onReplace(Object, Object)}, respectively.
 * </p>
 * 
 * @param <E>
 *          the class of the elements to contain.
 */
public class Container<E> implements TemplateBean<E> {

	private E item;

	/** creates a container containing a null Object */
	public Container() {
		item = null;
	}

	/**
	 * create a container with one object. This will not call
	 * {@link #onReplace(Object, Object)}, as the container is created as
	 * "starting with" this element.
	 * 
	 * @param contained
	 *          the element to contain
	 */
	public Container(E contained) {
		item = contained;
	}

	/**
	 * get the contained element. This will call the {@link #afterGet(Object)}
	 * 
	 * @return the contained element
	 */
	@Override
	public final E get() {
		beforeGet(item);
		E ret = item;
		return ret;
	}

	/**
	 * set the element to contain. This will call
	 * {@link #onReplace(Object, Object)}
	 * 
	 * @param contained
	 *          the element to contain.
	 */
	@Override
	public final void set(E contained) {
		E old = item;
		item = contained;
		onReplace(old, item);
	}

	/**
	 * direct accessor for sub classes that need to provide an internal access
	 * without method overhead
	 * 
	 * @return the contained element.
	 */
	protected final E directGet() {
		return item;
	}

	/** to Override to set behavior before the acces of the element */
	public void beforeGet(E accessed) {

	}

	/**
	 * To Override to set behavior on modifications. Called just after the
	 * elements have effectively been modified
	 */
	public void onReplace(E before, E after) {

	}

}
