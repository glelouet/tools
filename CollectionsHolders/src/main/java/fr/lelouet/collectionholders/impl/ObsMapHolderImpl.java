package fr.lelouet.collectionholders.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import java.util.function.Function;

import fr.lelouet.collectionholders.interfaces.ObsListHolder;
import fr.lelouet.collectionholders.interfaces.ObsMapHolder;
import fr.lelouet.tools.synchronization.LockWatchDog;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;

public class ObsMapHolderImpl<K, U> implements ObsMapHolder<K, U> {

	private ObservableMap<K, U> underlying;

	public ObsMapHolderImpl(ObservableMap<K, U> underlying) {
		this(underlying, false);
	}

	/**
	 * crate a new {@link ObsMapHolderImpl} backing on an underlying
	 * {@link ObservableMap}
	 *
	 * @param underlying
	 *          the map to back to
	 * @param datareceived
	 *          whether the map already contains all the information possible. if
	 *          not, call to synchronized method will wait until the data is
	 *          received
	 */
	public ObsMapHolderImpl(ObservableMap<K, U> underlying, boolean datareceived) {
		this.underlying = underlying;
		if (datareceived) {
			dataReceived();
		}
	}

	private CountDownLatch dataReceivedLatch = new CountDownLatch(1);

	private ArrayList<Consumer<Map<K, U>>> receiveListeners;

	@Override
	public void waitData() {
		try {
			dataReceivedLatch.await();
		} catch (InterruptedException e) {
			throw new UnsupportedOperationException("catch this", e);
		}
	}

	@Override
	public Map<K, U> copy() {
		waitData();
		return LockWatchDog.BARKER.syncExecute(underlying, () -> new HashMap<>(underlying));
	}

	@Override
	public U get(K key) {
		waitData();
		return underlying.get(key);
	}

	@Override
	public void follow(MapChangeListener<? super K, ? super U> listener) {
		LockWatchDog.BARKER.syncExecute(underlying, () -> {
			ObservableMap<K, U> othermap = FXCollections.observableHashMap();
			othermap.addListener(listener);
			othermap.putAll(underlying);
			underlying.addListener(listener);
		});
	}

	@Override
	public Observable asObservable() {
		return underlying;
	}

	@Override
	public void addReceivedListener(Consumer<Map<K, U>> callback) {
		synchronized (underlying) {
			if (receiveListeners == null) {
				receiveListeners = new ArrayList<>();
			}
			receiveListeners.add(callback);
			if (isDataReceived()) {
				callback.accept(underlying);
			}
		}
	}

	@Override
	public boolean remReceivedListener(Consumer<Map<K, U>> callback) {
		synchronized (underlying) {
			return receiveListeners.remove(callback);
		}
	}

	@Override
	public void dataReceived() {
		dataReceivedLatch.countDown();
		if (receiveListeners != null) {
			Map<K, U> consumed = underlying;
			for (Consumer<Map<K, U>> r : receiveListeners) {
				r.accept(consumed);
			}
		}
	}

	@Override
	public void unfollow(MapChangeListener<? super K, ? super U> change) {
		LockWatchDog.BARKER.syncExecute(underlying, () -> {
			underlying.removeListener(change);
		});
	}

	public boolean isDataReceived() {
		return dataReceivedLatch.getCount() == 0;
	}

	//
	// tools
	//

	/**
	 * create a new observableMap that map each entry in the source to an entry in
	 * the ret. creation and deletion of key are mappecd accordingly.
	 *
	 * @param source
	 * @param mapping
	 * @return
	 */
	public static <K, S, T> ObsMapHolderImpl<K, T> map(ObsMapHolder<K, S> source, Function<S, T> mapping) {
		ObservableMap<K, T> containedTarget = FXCollections.observableHashMap();
		ObsMapHolderImpl<K, T> ret = new ObsMapHolderImpl<>(containedTarget);
		source.follow(c -> {
			if (c.wasRemoved() && !c.wasAdded()) {
				synchronized (containedTarget) {
					containedTarget.remove(c.getKey());
				}
			} else {
				synchronized (containedTarget) {
					containedTarget.put(c.getKey(), mapping.apply(c.getValueAdded()));
				}
			}
		});
		source.addReceivedListener(l -> ret.dataReceived());
		return ret;
	}

	/**
	 * transforms an observable list into a map, by extracting the key from the
	 * new elements.
	 *
	 * @param list
	 * @param keyExtractor
	 * @return
	 */
	public static <K, V> ObsMapHolderImpl<K, V> toMap(ObsListHolder<V> list, Function<V, K> keyExtractor) {
		return toMap(list, keyExtractor, o -> o);
	}

	/**
	 * transforms an observable list into a map, by extracting the key from the
	 * new elements and remapping them to a new type.
	 *
	 * @param list
	 * @param keyExtractor
	 *          function to create the new keys of the map
	 * @param remapper
	 *          function to create the new values of the map
	 * @return
	 */
	public static <K, V, L> ObsMapHolderImpl<K, L> toMap(ObsListHolder<V> list, Function<V, K> keyExtractor,
			Function<V, L> remapper) {
		ObservableMap<K, L> internal = FXCollections.observableHashMap();
		ObsMapHolderImpl<K, L> ret = new ObsMapHolderImpl<>(internal);
		list.follow(c -> {
			while (c.next()) {
				synchronized (internal) {
					if (c.wasRemoved()) {
						for (V removed : c.getRemoved()) {
							internal.remove(keyExtractor.apply(removed));
						}
					}
					if (c.wasAdded()) {
						for (V added : c.getAddedSubList()) {
							internal.put(keyExtractor.apply(added), remapper.apply(added));
						}
					}
				}
			}
		});
		list.addReceivedListener(l -> {
			ret.dataReceived();
		});
		return ret;
	}

	/**
	 * merge several maps together
	 * <p>
	 * The result map does not consider modifications from the merged map, only
	 * rebuilds itself once all merged maps have received data, and whenever one
	 * of them receives data afterwards. This is because otherwise the values of
	 * the result could be corrupted when data is moved from one merged map to
	 * another in an asynchronous way.
	 * </p>
	 * <p>
	 * The listener remember the order the merged maps received data, so that a
	 * more recent key-value will override an older one. typically if two maps m1
	 * and m2 are merged in a map res, and key:v1 is stored in m1 while key:v2 is
	 * stored in m2, if m1 then m2 received data, in the map res there will only
	 * be key:m2.
	 * </p>
	 *
	 * @param <K>
	 * @param <V>
	 * @param maps
	 * @return anew map that observes the merged maps and reacts to their
	 *         modifications.
	 */
	@SafeVarargs
	public static <K, V> ObsMapHolderImpl<K, V> merge(ObsMapHolder<K, V>... maps) {
		ObservableMap<K, V> internal = FXCollections.observableHashMap();
		ObsMapHolderImpl<K, V> ret = new ObsMapHolderImpl<>(internal);
		if (maps != null && maps.length > 0) {
			LinkedHashMap<ObsMapHolder<K, V>, Map<K, V>> alreadyreceived = new LinkedHashMap<>();
			for (ObsMapHolder<K, V> m : maps) {
				m.addReceivedListener(map -> {
					synchronized (alreadyreceived) {
						alreadyreceived.remove(m);
						alreadyreceived.put(m, map);
						if (alreadyreceived.size() == maps.length) {
							Map<K, V> newmap = new HashMap<>();
							for (Map<K, V> madd : alreadyreceived.values()) {
								newmap.putAll(madd);
							}
							internal.keySet().retainAll(newmap.keySet());
							internal.putAll(newmap);
							ret.dataReceived();
						}
					}
				});
			}
		}
		return ret;
	}

}
