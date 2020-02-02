package fr.lelouet.collectionholders.interfaces.collections;

import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Predicate;

import fr.lelouet.collectionholders.interfaces.ObsObjHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsBoolHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsIntHolder;
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
	Map<K, V> get();

	/**
	 * synchronized call to the underlying map get, after the data is received.
	 *
	 * @param key
	 * @return
	 */
	V get(K key);

	V getOrDefault(K key, V defaultValue);

	/**
	 * create a new variable bound to the value mapped to a key
	 *
	 * @param key
	 * @param defaultValue
	 *          the value to be used in case the key is not present. Not null,
	 *          otherwise the returned holder would not be notified the value has
	 *          been set.
	 * @return a new variable
	 */
	ObsObjHolder<V> at(K key, V defaultValue);

	/**
	 * create a new variable bound to the value mapped to a key variable
	 *
	 * @param key
	 * @param devaultValue
	 *          the value to be used in case the key is not present. Not null,
	 *          otherwise the returned holder would not be notified the value has
	 *          been set.
	 * @return a new variable
	 */
	ObsObjHolder<V> at(ObsObjHolder<K> key, V devaultValue);

	/**
	 *
	 * get the variable for this size.
	 *
	 * @return an internally cached variable constrained to the size of the
	 *         internal map last time it received data.
	 */
	public ObsIntHolder size();

	public ObsBoolHolder isEmpty();

	/**
	 * apply all existing values to the change listener, and register it as a
	 * listener of the underlying map.
	 *
	 * @param change
	 */
	void followEntries(MapChangeListener<? super K, ? super V> change);

	void unfollowEntries(MapChangeListener<? super K, ? super V> change);

	void waitData();

	/**
	 * register a runnable to be run once, next time {@link #dataReceived()} is
	 * called. The call is made in a new thread.
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
	public void follow(Consumer<Map<K, V>> callback);

	/**
	 * remove a listener added through {@link #follow(Runnable)}
	 *
	 * @param callback
	 * @return true if the callback was added.
	 */
	public boolean unfollow(Consumer<Map<K, V>> callback);

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

	/**
	 * create a filtered map of this. The (key, val) couples are duplicated in the
	 * returned map unless keyfilter is not null and this predicate does not
	 * accept the key, or valuefilter is not null that predicate does not accept
	 * the value.
	 *
	 * @param keyFilter
	 *          a predicate to filter the keys, or null to accept all keys
	 * @param valueFilter
	 *          a predicate to filter the values, or null to accept all values
	 * @return a new obsmapholder, or this is both keyfilter and valuefilter are
	 *         null
	 */
	ObsMapHolder<K, V> filter(Predicate<K> keyFilter, Predicate<V> valueFilter);

	/**
	 * create a filtered map of this. The (key, val) couples are duplicated in the
	 * returned map, unless the key is not contained in the allowedKeys
	 * collection.
	 *
	 * @param allowedKeys
	 *          the observable collection holder on the allowed keys. can't be
	 *          null.
	 * @return a new Observable map holder.
	 */
	ObsMapHolder<K, V> filterKeys(ObsCollectionHolder<K, ?, ?> allowedKeys);

}
