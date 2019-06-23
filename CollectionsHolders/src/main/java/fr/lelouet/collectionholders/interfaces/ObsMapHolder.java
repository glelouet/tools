package fr.lelouet.collectionholders.interfaces;

import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;

import javafx.beans.Observable;
import javafx.collections.MapChangeListener;

/**
 * Holder on an underlying observable map. All calls should be synchronized on
 * the underlying observable map.
 *
 * @param <K>
 * @param <V>
 */
public interface ObsMapHolder<K, V> {

	/**
	 * wait for at least one data to be received, then returns a copy of the
	 * underlying map
	 *
	 * @return
	 */
	Map<K, V> copy();

	/**
	 * synchronized call to the underlying map get, after the data is received.
	 *
	 * @param key
	 * @return
	 */
	V get(K key);

	/**
	 * apply all existing values to the change listener, and register it as a
	 * listener of the underlying map.
	 *
	 * @param change
	 */
	void follow(MapChangeListener<? super K, ? super V> change);

	void unfollow(MapChangeListener<? super K, ? super V> change);

	void waitData();

	/**
	 * called by the data fetcher when data has been received. This has use only
	 * when the data received is empty, otherwise the put() methods should already
	 * call this method. This should usually be called within a synchronization
	 * call to the underlying data.
	 */
	void dataReceived();

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
	 * add a callback that will be called everytime the map received a
	 * dataReceived call. This is useful when you know the modifications are in
	 * batches and rather recompute the whole data instead of manage all the small
	 * modifications
	 */
	public void addReceivedListener(Consumer<Map<K, V>> callback);

	/**
	 * remove a listener added through {@link #addReceivedListener(Runnable)}
	 *
	 * @param callback
	 * @return true if the callback was added.
	 */
	public boolean remReceivedListener(Consumer<Map<K, V>> callback);

	/** return an observable to be notified when values are changed */
	Observable asObservable();

	/**
	 * merge this map with other. In case of collision, use the value from the map
	 * which has received data the last. Operates in bulk mode.
	 *
	 * @param m1
	 * @param maps
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public default ObsMapHolder<K, V> merge(ObsMapHolder<K, V>... maps) {
		return merge((v1, v2) -> v2, maps);
	}

	/**
	 * merge this map with several others
	 *
	 * @param merger
	 *          the function to merge several values together when they have the
	 *          same key
	 * @param maps
	 *          the maps to merge with this.
	 * @return a new map if needed, containing all the merged key-val couples of
	 *         this and the maps
	 */
	@SuppressWarnings("unchecked")
	public ObsMapHolder<K, V> merge(BinaryOperator<V> merger, ObsMapHolder<K, V>... maps);

	/**
	 * create a new variable bound to the value mapped to an index
	 *
	 * @param key
	 * @return
	 */
	ObsObjHolder<V> at(K key);

	/**
	 * create a new variable bound to the value mapped to a variable index
	 *
	 * @param key
	 * @return
	 */
	ObsObjHolder<V> at(ObsObjHolder<K> key);

	/**
	 *
	 * @return a set that contains all keys used in the internal collection.
	 */
	ObsSetHolder<K> keys();

	/**
	 *
	 * @return a collection holder that contains all the values contained in the
	 *         internal collections
	 */
	ObsCollectionHolder<V, ?, ?> values();

}
