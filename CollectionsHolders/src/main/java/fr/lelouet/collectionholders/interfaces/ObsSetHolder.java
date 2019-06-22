package fr.lelouet.collectionholders.interfaces;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import javafx.beans.Observable;
import javafx.collections.SetChangeListener;

public interface ObsSetHolder<U> {

	Set<U> copy();

	/**
	 * wait for at least one element to be added, then apply the consumer to the
	 * (index,elements) couples.
	 *
	 * @param cons
	 */
	void apply(Consumer<U> cons);

	/**
	 * apply all existing values to the change listener, and register it as a
	 * listener of the underlying list.
	 *
	 * @param listener
	 */
	void follow(SetChangeListener<? super U> listener);

	void unfollow(SetChangeListener<? super U> change);

	void waitData();

	/**
	 * called by the data fetcher when data has been received. This has use only
	 * when the data received is empty, otherwise the add() methods should already
	 * call this method. Typically this should never be used by the user.
	 */
	void dataReceived();

	/**
	 * add a callback that will be called every time the map received a
	 * dataReceived call. This is useful when you know the modifications are in
	 * batches and rather recompute the whole data instead of manage all the small
	 * modifications
	 */
	public void addReceivedListener(Consumer<Set<U>> callback);

	/**
	 * remove a listener added through {@link #addReceivedListener(Runnable)}
	 *
	 * @param callback
	 * @return true if the callback was added.
	 */
	public boolean remReceivedListener(Consumer<Set<U>> callback);

	/** return an observable to be notified when values are changed */
	Observable asObservable();

	/**
	 * create a filtered list working on bulk process
	 *
	 * @param predicate
	 *          the predicate to select the items
	 * @return a new list
	 */
	ObsSetHolder<U> filter(Predicate<? super U> predicate);

	/**
	 * map this list to a new Map. In case of collision in the key function, only
	 * the last added elements are mapped.
	 *
	 * @param <K>
	 *          the key type of the map
	 * @param keyExtractor
	 *          the function to create a key from this list's values
	 * @return a new map that contains this list's items, with the corresponding
	 *         key.
	 */
	<K> ObsMapHolder<K, U> map(Function<U, K> keyExtractor);

	/**
	 * map this list to a new Map. In case of collision in the key function, only
	 * the last added elements are mapped.
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
	<K, V> ObsMapHolder<K, V> map(Function<U, K> keyExtractor, Function<U, V> valExtractor);

}
