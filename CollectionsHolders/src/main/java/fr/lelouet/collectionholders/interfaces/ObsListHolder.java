package fr.lelouet.collectionholders.interfaces;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import fr.lelouet.collectionholders.impl.ObsListHolderImpl;
import javafx.beans.Observable;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

/**
 * holder on an observable list. All calls should be synchronized.
 * <p>
 * an {@link ObsListHolder} is a layer over a {@link ObservableList}. It
 * provides methods to
 * <ul>
 * <li>{@link #follow(ListChangeListener)} and
 * {@link #unfollow(ListChangeListener)} the events in the underlying list.
 * Already processed events are processed again, so existing items will trigger
 * an event</li>
 * <li>{@link #copy()} the internal data or {@link #apply(BiConsumer)} a
 * consumer.</li>
 * <li>{@link #waitData() wait} for data to be acquired at least once. Typically
 * a call to {@link #copy()} or to {@link #apply(BiConsumer)} results in a call
 * to this method, but {@link #follow(ListChangeListener)} does not, so a result
 * produced by using follow should be used after a call to this method.</li>
 * </ul>
 * <p>
 *
 */
public interface ObsListHolder<U> {

	/**
	 * wait for at least one element to be added, then return a copy of the
	 * underlying list.
	 *
	 * @return
	 */
	List<U> copy();

	/**
	 * wait for at least one element to be added, then apply the consumer to the
	 * (index,elements) couples.
	 *
	 * @param cons
	 */
	void apply(BiConsumer<Integer, U> cons);

	/**
	 * apply all existing values to the change listener, and register it as a
	 * listener of the underlying list.
	 *
	 * @param listener
	 */
	void follow(ListChangeListener<? super U> listener);

	void unfollow(ListChangeListener<? super U> change);

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
	public void addReceivedListener(Consumer<List<U>> callback);

	/**
	 * remove a listener added through {@link #addReceivedListener(Runnable)}
	 *
	 * @param callback
	 * @return true if the callback was added.
	 */
	public boolean remReceivedListener(Consumer<List<U>> callback);

	/** return an observable to be notified when values are changed */
	Observable asObservable();

	/**
	 * create a filtered list working on bulk process
	 *
	 * @param predicate
	 *          the predicate to select the items
	 * @return a new list
	 */
	ObsListHolderImpl<U> filter(Predicate<? super U> predicate);

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
