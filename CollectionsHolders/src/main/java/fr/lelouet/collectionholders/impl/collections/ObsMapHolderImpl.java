package fr.lelouet.collectionholders.impl.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.lelouet.collectionholders.impl.AObsObjHolder;
import fr.lelouet.collectionholders.impl.ObsObjHolderSimple;
import fr.lelouet.collectionholders.impl.numbers.ObsBoolHolderImpl;
import fr.lelouet.collectionholders.impl.numbers.ObsDoubleHolderImpl;
import fr.lelouet.collectionholders.impl.numbers.ObsIntHolderImpl;
import fr.lelouet.collectionholders.impl.numbers.ObsLongHolderImpl;
import fr.lelouet.collectionholders.interfaces.ObsObjHolder;
import fr.lelouet.collectionholders.interfaces.collections.ObsCollectionHolder;
import fr.lelouet.collectionholders.interfaces.collections.ObsListHolder;
import fr.lelouet.collectionholders.interfaces.collections.ObsMapHolder;
import fr.lelouet.collectionholders.interfaces.collections.ObsSetHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsBoolHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsDoubleHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsIntHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsLongHolder;
import fr.lelouet.tools.lambdaref.HoldingRef;
import fr.lelouet.tools.synchronization.LockWatchDog;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;

public class ObsMapHolderImpl<K, V> implements ObsMapHolder<K, V> {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(ObsMapHolderImpl.class);

	/**
	 * create an unmodifiable map of items. Will fail if keyvals is not exactly an
	 * array of 2-items arrays.
	 *
	 * @param <K>
	 *          type of the keys
	 * @param <V>
	 *          type of the values
	 * @param keyvals
	 *          couples of (K,V) to map. Not requesting a varargar cause it can
	 *          lead to safevararg, and does not allow to create the array
	 *          dynamically (needs to be instantiated first)
	 * @return a new observable map
	 */
	public static <K, V> ObsMapHolderImpl<K, V> of(Object[][] keyvals) {
		@SuppressWarnings("unchecked")
		Map<K, V> mapped = Stream.of(keyvals).collect(Collectors.toMap(arr -> (K) arr[0], arr -> (V) arr[1]));
		return new ObsMapHolderImpl<>(FXCollections.observableMap(mapped), true);
	}

	private ObservableMap<K, V> underlying;

	public ObservableMap<K, V> underlying() {
		return underlying;
	}

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

	public ObsMapHolderImpl() {
		this(FXCollections.observableHashMap(), false);
	}

	/**
	 * is set to 0 once data is received.
	 */
	private CountDownLatch dataReceivedLatch = new CountDownLatch(1);

	private ArrayList<HoldingRef<Consumer<Map<K, V>>>> receiveListeners;

	@Override
	public void waitData() {
		try {
			dataReceivedLatch.await();
		} catch (InterruptedException e) {
			throw new UnsupportedOperationException("catch this", e);
		}
	}

	@Override
	public Map<K, V> get() {
		waitData();
		return LockWatchDog.BARKER.syncExecute(underlying, () -> Collections.unmodifiableMap(underlying));
	}

	@Override
	public V get(K key) {
		waitData();
		return underlying.get(key);
	}

	@Override
	public V getOrDefault(K key, V defaultValue) {
		waitData();
		return underlying.getOrDefault(key, defaultValue);
	}

	@Override
	public void followEntries(MapChangeListener<? super K, ? super V> listener) {
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
	public void follow(Consumer<Map<K, V>> callback, Object holder) {
		HoldingRef<Consumer<Map<K, V>>> ref = new HoldingRef<>(callback, holder);
		synchronized (underlying) {
			if (receiveListeners == null) {
				receiveListeners = new ArrayList<>();
			}
			receiveListeners.add(ref);
			if (isDataReceived()) {
				callback.accept(underlying);
			}
		}
	}

	@Override
	public void unfollow(Consumer<Map<K, V>> callback) {
		synchronized (underlying) {
			receiveListeners.removeIf(h -> {
				Consumer<Map<K, V>> ref = h.get();
				return ref == null || ref == callback;
			});
		}
	}

	public void dataReceived() {
		dataReceivedLatch.countDown();
		if (receiveListeners != null) {
			Map<K, V> consumed = underlying;
			for (Iterator<HoldingRef<Consumer<Map<K, V>>>> it = receiveListeners.iterator(); it.hasNext();) {
				Consumer<Map<K, V>> ref = it.next().get();
				if (ref == null) {
					it.remove();
				} else {
					ref.accept(consumed);
				}
			}
		}
	}

	@Override
	public void unfollowEntries(MapChangeListener<? super K, ? super V> change) {
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
		source.followEntries(c -> {
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
		source.follow(l -> ret.dataReceived());
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
		return toMap(list, keyExtractor, o -> o, (a, b) -> b);
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
			Function<V, L> remapper, BinaryOperator<L> mergeFunction) {
		ObservableMap<K, L> internal = FXCollections.observableHashMap();
		ObsMapHolderImpl<K, L> ret = new ObsMapHolderImpl<>(internal);
		list.follow((l) -> {
			Map<K, L> newmap = l.stream().collect(Collectors.toMap(keyExtractor, remapper, mergeFunction));
			synchronized (internal) {
				internal.keySet().retainAll(newmap.keySet());
				internal.putAll(newmap);
			}
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
	 * @param m1
	 *          another map holder
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
		LinkedHashMap<Integer, Map<K, V>> alreadyreceived = new LinkedHashMap<>();
		for (int i = 0; i < array.length; i++) {
			ObsMapHolder<K, V> m = array[i];
			int index = i;
			m.follow(map -> {
				synchronized (alreadyreceived) {
					alreadyreceived.remove(index);
					alreadyreceived.put(index, map);
					if (alreadyreceived.size() == array.length) {
						Map<K, V> newmap = alreadyreceived.values().stream().flatMap(m2 -> m2.entrySet().stream())
								.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(), merger));
						synchronized (internal) {
							internal.keySet().retainAll(newmap.keySet());
							internal.putAll(newmap);
						}
						ret.dataReceived();
					} else {
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
	public ObsObjHolder<V> at(ObsObjHolder<K> key, V defaultValue) {
		ObsObjHolderSimple<V> ret = new ObsObjHolderSimple<>();
		HashSet<Object> received = new HashSet<>();
		Runnable updateValue = () -> {
			if (received.size() == 2) {
				ret.set(getOrDefault(key.get(), defaultValue));
			}
		};
		follow(t -> {
			synchronized (received) {
				received.add(this);
				updateValue.run();
			}
		});
		key.follow((newValue) -> {
			synchronized (received) {
				received.add(key);
				updateValue.run();
			}
		});
		return ret;
	}

	@Override
	public ObsObjHolder<V> at(K key, V defaultValue) {
		ObsObjHolderSimple<V> ret = new ObsObjHolderSimple<>();
		follow(t -> {
			ret.set(t.getOrDefault(key, defaultValue));
		});
		return ret;
	}

	private ObsIntHolderImpl size = null;

	@Override
	public ObsIntHolder size() {
		if (size == null) {
			synchronized (this) {
				if (size == null) {
					ObsIntHolderImpl ret = new ObsIntHolderImpl();
					follow(c -> ret.set(c.size()));
					size = ret;
				}
			}
		}
		return size;
	}

	private ObsBoolHolder isEmpty = null;

	@Override
	public ObsBoolHolder isEmpty() {
		if (isEmpty == null) {
			ObsIntHolder msize = size();
			synchronized (this) {
				if (isEmpty == null) {
					isEmpty = msize.eq(0);
				}
			}
		}
		return isEmpty;
	}

	private ObsSetHolder<K> keys = null;

	@Override
	public ObsSetHolder<K> keys() {
		if (keys == null) {
			synchronized (this) {
				if (keys == null) {
					ObservableSet<K> internal = FXCollections.observableSet(new HashSet<>());
					ObsSetHolderImpl<K> ret = new ObsSetHolderImpl<>(internal);
					follow(m -> {
						synchronized (internal) {
							internal.retainAll(m.keySet());
							internal.addAll(m.keySet());
						}
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
					follow(m -> {
						synchronized (internal) {
							internal.clear();
							internal.addAll(m.values());
						}
						ret.dataReceived();
					});
					values = ret;
				}
			}
		}
		return values;
	}

	ObsListHolderImpl<Entry<K, V>> entries = null;

	@Override
	public ObsCollectionHolder<Entry<K, V>, ?, ?> entries() {
		if (entries == null) {
			synchronized (this) {
				if (entries == null) {
					ObservableList<Entry<K, V>> internal = FXCollections.observableArrayList();
					ObsListHolderImpl<Entry<K, V>> ret = new ObsListHolderImpl<>(internal);
					follow(m -> {
						synchronized (internal) {
							internal.clear();
							internal.addAll(m.entrySet());
						}
						ret.dataReceived();
					});
					entries = ret;
				}
			}
		}
		return entries;
	}

	@Override
	public ObsMapHolder<K, V> filter(Predicate<K> keyFilter, Predicate<V> valueFilter) {
		if (keyFilter == null && valueFilter == null) {
			return this;
		}
		ObservableMap<K, V> internal = FXCollections.observableHashMap();
		ObsMapHolderImpl<K, V> ret = new ObsMapHolderImpl<>(internal);
		followEntries(change -> {
			if (change.wasRemoved()) {
				synchronized (internal) {
					internal.remove(change.getKey());
				}
			}
			if (change.wasAdded()) {
				if (keyFilter != null) {
					if (!keyFilter.test(change.getKey())) {
						return;
					}
				}
				if (valueFilter != null) {
					if (!valueFilter.test(change.getValueAdded())) {
						return;
					}
				}
				synchronized (internal) {
					internal.put(change.getKey(), change.getValueAdded());
				}
			}
		});
		follow(m -> {
			ret.dataReceived();
		});
		return ret;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ObsMapHolder<K, V> filterKeys(ObsCollectionHolder<K, ?, ?> allowedKeys) {
		ObservableMap<K, V> internal = FXCollections.observableHashMap();
		ObsMapHolderImpl<K, V> ret = new ObsMapHolderImpl<>(internal);
		Map<K, V>[] lastMap = new Map[1];
		Collection<K>[] lastCol = new Collection[1];
		Runnable update = () -> {
			synchronized (lastMap) {
				if (lastMap[0] != null && lastCol[0] != null) {
					Map<K, V> newMap = new HashMap<>(lastMap[0]);
					newMap.keySet().retainAll(lastCol[0]);
					synchronized (internal) {
						internal.keySet().retainAll(newMap.keySet());
						internal.putAll(newMap);
					}
					ret.dataReceived();
				}
			}
		};

		follow(m -> {
			synchronized (lastMap) {
				lastMap[0] = m;
			}
			update.run();
		});
		allowedKeys.follow(l -> {
			synchronized (lastMap) {
				lastCol[0] = l;
			}
			update.run();
		});
		return ret;
	}

	@Override
	public int hashCode() {
		return dataReceivedLatch.getCount() == 0 ? 0 : underlying.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj.getClass() == this.getClass()) {
			ObsMapHolderImpl<?, ?> other = (ObsMapHolderImpl<?, ?>) obj;
			// equals if same status of data received AND same data received, if
			// received.
			return dataReceivedLatch.getCount() != 0 && other.dataReceivedLatch.getCount() != 0
					|| dataReceivedLatch.getCount() == 0 && other.dataReceivedLatch.getCount() == 0
					&& underlying.equals(other.underlying);
		}
		return false;
	}

	@Override
	public <U> ObsObjHolder<U> map(Function<Map<K, V>, U> mapper) {
		ObsObjHolderSimple<U> ret = new ObsObjHolderSimple<>();
		follow(m -> ret.set(mapper.apply(m)));
		return ret;
	}

	@Override
	public ObsIntHolder mapInt(ToIntFunction<Map<K, V>> mapper) {
		ObsIntHolderImpl ret = new ObsIntHolderImpl();
		follow(m -> ret.set(mapper.applyAsInt(m)));
		return ret;
	}

	@Override
	public ObsLongHolder mapLong(ToLongFunction<Map<K, V>> mapper) {
		ObsLongHolderImpl ret = new ObsLongHolderImpl();
		follow(m -> ret.set(mapper.applyAsLong(m)));
		return ret;
	}

	@Override
	public ObsDoubleHolder mapDouble(ToDoubleFunction<Map<K, V>> mapper) {
		ObsDoubleHolderImpl ret = new ObsDoubleHolderImpl();
		follow(m -> ret.set(mapper.applyAsDouble(m)));
		return ret;
	}

	@Override
	public ObsBoolHolder test(Predicate<Map<K, V>> test) {
		ObsBoolHolderImpl ret = new ObsBoolHolderImpl();
		follow(m -> ret.set(test.test(m)));
		return ret;
	}

	@Override
	public <U> ObsListHolder<U> toList(Function<Map<K, V>, Iterable<U>> generator) {
		ObsListHolderImpl<U> ret = new ObsListHolderImpl<>();
		follow((newValue) -> {
			ret.underlying().clear();
			if (newValue != null) {
				for (U v : generator.apply(newValue)) {
					ret.underlying().add(v);
				}
			}
			ret.dataReceived();
		});
		return ret;
	}

	@Override
	public ObsMapHolderImpl<K, V> peek(Consumer<Map<K, V>> observer) {
		follow(observer);
		return this;
	}

	@Override
	public <T> ObsObjHolder<T> unPack(Function<Map<K, V>, ObsObjHolder<T>> unpacker) {
		return AObsObjHolder.unPack(this, unpacker);
	}

}
