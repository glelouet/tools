package fr.lelouet.collectionholders.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fr.lelouet.collectionholders.interfaces.ObsCollectionHolder;
import fr.lelouet.collectionholders.interfaces.ObsListHolder;
import fr.lelouet.collectionholders.interfaces.ObsMapHolder;
import fr.lelouet.collectionholders.interfaces.ObsObjHolder;
import fr.lelouet.collectionholders.interfaces.ObsSetHolder;
import fr.lelouet.tools.synchronization.LockWatchDog;
import javafx.beans.Observable;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;

public class ObsMapHolderImpl<K, V> implements ObsMapHolder<K, V> {

	private ObservableMap<K, V> underlying;

	public ObsMapHolderImpl(ObservableMap<K, V> underlying) {
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
	public ObsMapHolderImpl(ObservableMap<K, V> underlying, boolean datareceived) {
		this.underlying = underlying;
		if (datareceived) {
			dataReceived();
		}
	}

	private CountDownLatch dataReceivedLatch = new CountDownLatch(1);

	private ArrayList<Consumer<Map<K, V>>> receiveListeners;

	@Override
	public void waitData() {
		try {
			dataReceivedLatch.await();
		} catch (InterruptedException e) {
			throw new UnsupportedOperationException("catch this", e);
		}
	}

	@Override
	public Map<K, V> copy() {
		waitData();
		return LockWatchDog.BARKER.syncExecute(underlying, () -> new HashMap<>(underlying));
	}

	@Override
	public V get(K key) {
		waitData();
		return underlying.get(key);
	}

	@Override
	public void follow(MapChangeListener<? super K, ? super V> listener) {
		LockWatchDog.BARKER.syncExecute(underlying, () -> {
			ObservableMap<K, V> othermap = FXCollections.observableHashMap();
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
	public void addReceivedListener(Consumer<Map<K, V>> callback) {
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
	public boolean remReceivedListener(Consumer<Map<K, V>> callback) {
		synchronized (underlying) {
			return receiveListeners.remove(callback);
		}
	}

	@Override
	public void dataReceived() {
		dataReceivedLatch.countDown();
		if (receiveListeners != null) {
			Map<K, V> consumed = underlying;
			for (Consumer<Map<K, V>> r : receiveListeners) {
				r.accept(consumed);
			}
		}
	}

	@Override
	public void unfollow(MapChangeListener<? super K, ? super V> change) {
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
	public static <K, V> ObsMapHolderImpl<K, V> toMap(ObsCollectionHolder<V, ?, ?> list, Function<V, K> keyExtractor) {
		return toMap(list, keyExtractor, o -> o);
	}

	/**
	 * transforms an observable list into a map, by extracting the key from the
	 * new elements and remaping them to a new type.
	 *
	 * @param list
	 * @param keyExtractor
	 *          function to create the new keys of the map
	 * @param remapper
	 *          function to create the new values of the map
	 * @return
	 */
	public static <K, V, L> ObsMapHolderImpl<K, L> toMap(ObsCollectionHolder<V, ?, ?> list, Function<V, K> keyExtractor,
			Function<V, L> remapper) {
		ObservableMap<K, L> internal = FXCollections.observableHashMap();
		ObsMapHolderImpl<K, L> ret = new ObsMapHolderImpl<>(internal);
		list.addReceivedListener(l -> {
			Map<K, L> newmap = l.stream().collect(Collectors.toMap(keyExtractor, remapper, (a, b) -> b));
			internal.keySet().retainAll(newmap.keySet());
			internal.putAll(newmap);
			ret.dataReceived();
		});
		return ret;
	}

	/**
	 * merge several maps together in bulk method (only when data is received)
	 * <p>
	 * The result map does not consider modifications from the merged map, only
	 * rebuilds itself once all merged maps have received data, and whenever one
	 * of them receives data afterwards. This is because otherwise the values of
	 * the result could be corrupted when data is moved from one merged map to
	 * another in an asynchronous way.
	 * </p>
	 *
	 * @param <K>
	 * @param <V>
	 * @param maps
	 * @return a new map that observes the merged maps and reacts to their
	 *         modifications.
	 */
	@SuppressWarnings("unchecked")
	@SafeVarargs
	public static <K, V> ObsMapHolder<K, V> merge(BinaryOperator<V> merger, ObsMapHolder<K, V> m1,
			ObsMapHolder<K, V>... maps) {
		ObsMapHolder<K, V>[] array = Stream
				.concat(m1 == null ? Stream.empty() : Stream.of(m1), maps == null ? Stream.empty() : Stream.of(maps))
				.filter(m -> m != null).toArray(ObsMapHolder[]::new);
		if (array.length == 1) {
			return array[0];
		}
		ObservableMap<K, V> internal = FXCollections.observableHashMap();
		ObsMapHolderImpl<K, V> ret = new ObsMapHolderImpl<>(internal);
		LinkedHashMap<ObsMapHolder<K, V>, Map<K, V>> alreadyreceived = new LinkedHashMap<>();
		for (ObsMapHolder<K, V> m : array) {
			m.addReceivedListener(map -> {
				synchronized (alreadyreceived) {
					alreadyreceived.remove(m);
					alreadyreceived.put(m, map);
					if (alreadyreceived.size() == array.length) {
						Map<K, V> newmap = alreadyreceived.values().stream().flatMap(m2 -> m2.entrySet().stream())
								.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(), merger));
						internal.keySet().retainAll(newmap.keySet());
						internal.putAll(newmap);
						ret.dataReceived();
					}
				}
			});
		}
		return ret;
	}

	@Override
	@SuppressWarnings("unchecked")
	public ObsMapHolder<K, V> merge(BinaryOperator<V> merger, ObsMapHolder<K, V>... maps) {
		return merge(merger, this, maps);
	}

	@Override
	public ObsObjHolder<V> at(ObsObjHolder<K> key) {
		SimpleObjectProperty<V> internal = new SimpleObjectProperty<>();
		ObsObjHolderImpl<V> ret = new ObsObjHolderImpl<>(internal);
		HashSet<Object> received = new HashSet<>();
		Runnable updateValue = () -> {
			if (received.size() == 2) {
				internal.set(get(key.get()));
			}
		};
		addReceivedListener(t -> {
			synchronized (received) {
				received.add(this);
				updateValue.run();
			}
		});
		key.follow((observable, oldValue, newValue) -> {
			synchronized (received) {
				received.add(key);
				updateValue.run();
			}
		});
		return ret;
	}

	@Override
	public ObsObjHolder<V> at(K key) {
		SimpleObjectProperty<V> internal = new SimpleObjectProperty<>();
		ObsObjHolderImpl<V> ret = new ObsObjHolderImpl<>(internal);
		addReceivedListener(t -> {
			internal.set(t.get(key));
		});
		return ret;
	}

	private ObsSetHolder<K> keys = null;

	@Override
	public ObsSetHolder<K> keys() {
		if (keys == null) {
			synchronized (this) {
				if (keys == null) {
					ObservableSet<K> internal = FXCollections.observableSet(new HashSet<>());
					ObsSetHolderImpl<K> ret = new ObsSetHolderImpl<>(internal);
					addReceivedListener(m -> {
						internal.clear();
						internal.addAll(m.keySet());
						ret.dataReceived();
					});
					keys = ret;
				}
			}
		}
		return keys;
	}

	private ObsListHolder<V> values = null;

	@Override
	public ObsCollectionHolder<V, ?, ?> values() {
		if (values == null) {
			synchronized (this) {
				if (values == null) {
					ObservableList<V> internal = FXCollections.observableArrayList();
					ObsListHolderImpl<V> ret = new ObsListHolderImpl<>(internal);
					addReceivedListener(m -> {
						internal.clear();
						internal.addAll(m.values());
						ret.dataReceived();
					});
					values = ret;
				}
			}
		}
		return values;
	}

}
