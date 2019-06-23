package fr.lelouet.collectionholders.interfaces;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import javafx.beans.Observable;

/**
 * common interface for set and list.
 *
 * @param <U>
 *          class of the item in the collection
 * @param <C>
 *          Collection type to hold the data (eg List&lt;U&gt;)
 * @param <L>
 *          Listener type passed to observe the internal data (eg
 *          ListEventListener&lt;U&gt;)
 */
public interface ObsCollectionHolder<U, C extends Collection<U>, L> {

	/**
	 * wait for at least one element to be added, then return a copy of the
	 * underlying list.
	 *
	 * @return
	 */
	C copy();

	/**
	 * iterate over all the elements in the collections, after it's been set, and
	 * apply a consumer on each
	 *
	 * @param cons
	 */
	public void apply(Consumer<U> cons);

	/**
	 *
	 * get the variable for this collection's size.
	 *
	 * @return an internally cached variable constrained to the size of the
	 *         internal collection last time it received data.
	 */
	public ObsObjHolder<Integer> size();

	/**
	 * apply all existing values to the change listener, and register it as a
	 * listener of the underlying list.
	 *
	 * @param listener
	 */
	void follow(L listener);

	void unfollow(L change);

	void waitData();

	/**
	 * called by the data fetcher when data has been received. This specifies that
	 * the items stored are consistent and can be used as a bulk.
	 */
	void dataReceived();

	/**
	 * add a callback that will be called every time the map received a
	 * dataReceived call. This is useful when you know the modifications are in
	 * batches and rather recompute the whole data instead of manage all the small
	 * modifications
	 */
	public void addReceivedListener(Consumer<C> callback);

	/**
	 * remove a listener added through {@link #addReceivedListener(Runnable)}
	 *
	 * @param callback
	 * @return true if the callback was added.
	 */
	public boolean remReceivedListener(Consumer<C> callback);

	/**
	 * register a runnable to be run once {@link #dataReceived()} is called. The
	 * call is made in a new thread.
	 *
	 * @param callback
	 *          the function to call once data is available. if data is already
	 *          available, this callback will be called at once.
	 */
	public default void onWaitEnd(Runnable callback) {
		new Thread(() -> {
			waitData();
			callback.run();
		}).start();
	}

	/**
	 * return an observable to be notified when values are changed. Typically
	 * returns the underlying observable collection
	 */
	Observable asObservable();

	/**
	 * create a filtered collection working on bulk process
	 *
	 * @param predicate
	 *          the predicate to select the items
	 * @return a new collection with same parametrized signature.
	 */
	public ObsCollectionHolder<U, C, L> filter(Predicate<? super U> predicate);

	/**
	 * map this collection to a new Map. In case of collision in the key function,
	 * only the last added elements are mapped.
	 *
	 * @param <K>
	 *          the key type of the map
	 * @param keyExtractor
	 *          the function to create a key from this list's values
	 * @return a new map that contains this list's items, with the corresponding
	 *         key.
	 */
	public <K> ObsMapHolder<K, U> map(Function<U, K> keyExtractor);

	/**
	 * map the items in this into a new Map. In case of collision in the key
	 * function, only the last added elements are mapped.
	 *
	 * @param <K>
	 *          the key type of the map
	 * @param <V>
	 *          the value type of the map
	 * @param keyExtractor
	 *          function to transform an element in the key
	 * @param valExtractor
	 *          function to transform an element in the value
	 * @return a new map.
	 */
	public <K, V> ObsMapHolder<K, V> map(Function<U, K> keyExtractor, Function<U, V> valExtractor);

}
