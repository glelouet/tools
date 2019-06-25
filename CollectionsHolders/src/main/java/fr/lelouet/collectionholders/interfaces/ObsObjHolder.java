package fr.lelouet.collectionholders.interfaces;

import java.util.function.Function;

import fr.lelouet.collectionholders.interfaces.collections.ObsListHolder;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;

/**
 * holder on a single object. call should be synchronized.
 *
 * @param <U>
 */
public interface ObsObjHolder<U> {

	/** return the internal object once it's retrieved */
	public U get();

	/**
	 * add a listener on this value. The listener is called at once if the value
	 * is already set, and will be called whenever the value is set later on.
	 */
	public void follow(ChangeListener<U> cons);

	/**
	 * remove a listener on this value
	 *
	 * @param change
	 */
	void unfollow(ChangeListener<U> change);

	void waitData();

	/** return an observable to be notified when values are changed */
	Observable asObservable();

	/**
	 * create a new obsObjHolder that contains this value, mapped
	 *
	 * @param <V>
	 *          the type fo the value contained
	 * @param mapper
	 *          the mapper from U to V
	 * @return a new object.
	 */
	<V> ObsObjHolder<V> map(Function<U, V> mapper);

	/**
	 * generate an observable list from thie item hold
	 * 
	 * @param <V>
	 * @param generator
	 * @return
	 */
	<V> ObsListHolder<V> toList(Function<U, Iterable<V>> generator);
}
