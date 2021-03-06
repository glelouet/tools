package fr.lelouet.tools.holders.impl.collections;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fr.lelouet.tools.holders.impl.ObjHolderSimple;
import fr.lelouet.tools.holders.interfaces.ObjHolder;
import fr.lelouet.tools.holders.interfaces.collections.CollectionHolder;
import fr.lelouet.tools.holders.interfaces.collections.ListHolder;
import fr.lelouet.tools.holders.interfaces.collections.MapHolder;
import fr.lelouet.tools.holders.interfaces.collections.SetHolder;
import fr.lelouet.tools.holders.interfaces.numbers.BoolHolder;
import fr.lelouet.tools.holders.interfaces.numbers.IntHolder;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * implementation of the writable Map holder.
 * <p>
 * the set methods modifies the stored data : a null data is translated to
 * {@link Collections.#emptyMap()}, while a non null map is translated to
 * {@link Collections.#unmodifiableMap(Map)}.
 * </p>
 *
 * @param <K>
 * @param <V>
 */
public class MapHolderImpl<K, V> extends ObjHolderSimple<Map<K, V>> implements MapHolder<K, V> {

	public MapHolderImpl(Map<K, V> map) {
		super(map);
	}

	public MapHolderImpl() {
	}

	@Override
	public synchronized void set(Map<K, V> newitem) {
		super.set(newitem == null ? Collections.emptyMap() : Collections.unmodifiableMap(newitem));
	}

	//
	// tools
	//

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
	public static <K, V> MapHolderImpl<K, V> of(Object[][] keyvals) {
		@SuppressWarnings("unchecked")
		Map<K, V> mapped = Stream.of(keyvals).collect(Collectors.toMap(arr -> (K) arr[0], arr -> (V) arr[1]));
		return new MapHolderImpl<>(mapped);
	}

	/**
	 * transforms an observable list into a map, by extracting the key from the
	 * new elements.
	 *
	 * @param collection
	 * @param keyExtractor
	 * @return
	 */
	public static <K, V> MapHolderImpl<K, V> toMap(CollectionHolder<V, ?> collection, Function<V, K> keyExtractor) {
		return toMap(collection, keyExtractor, o -> o, (a, b) -> b);
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
	public static <K, V, L> MapHolderImpl<K, L> toMap(CollectionHolder<V, ?> list, Function<V, K> keyExtractor,
			Function<V, L> remapper, BinaryOperator<L> mergeFunction) {
		MapHolderImpl<K, L> ret = new MapHolderImpl<>();
		list.follow((l) -> {
			Map<K, L> newmap = l.stream().collect(Collectors.toMap(keyExtractor, remapper, mergeFunction));
			ret.set(newmap);
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
	public static <K, V> MapHolder<K, V> merge(BinaryOperator<V> merger, MapHolder<K, V> m1,
			MapHolder<K, V>... maps) {
		MapHolder<K, V>[] array = Stream
				.concat(m1 == null ? Stream.empty() : Stream.of(m1), maps == null ? Stream.empty() : Stream.of(maps))
				.filter(m -> m != null).toArray(MapHolder[]::new);
		if (array.length == 1) {
			return array[0];
		}
		MapHolderImpl<K, V> ret = new MapHolderImpl<>();
		LinkedHashMap<Integer, Map<K, V>> alreadyreceived = new LinkedHashMap<>();
		for (int i = 0; i < array.length; i++) {
			MapHolder<K, V> m = array[i];
			int index = i;
			m.follow(map -> {
				synchronized (alreadyreceived) {
					alreadyreceived.remove(index);
					alreadyreceived.put(index, map);
					if (alreadyreceived.size() == array.length) {
						Map<K, V> newmap = alreadyreceived.values().stream().flatMap(m2 -> m2.entrySet().stream())
								.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(), merger));
						ret.set(newmap);
					} else {
					}
				}
			}, ret);
		}
		return ret;
	}

	@Override
	@SuppressWarnings("unchecked")
	public MapHolder<K, V> merge(BinaryOperator<V> merger, MapHolder<K, V>... maps) {
		return merge(merger, this, maps);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ObjHolder<V> at(ObjHolder<K> key, V defaultValue) {
		ObjHolderSimple<V> ret = new ObjHolderSimple<>();
		boolean[] receipt = new boolean[] { false, false };
		Object[] receivedKey = new Object[1];
		Object[] receivedMap = new Object[1];
		Runnable updateValue = () -> {
			if (receipt[0] && receipt[1]) {
				K rkey = (K) receivedKey[0];
				Map<K, V> rmap = (Map<K, V>) receivedMap[0];
				V newval = rmap.getOrDefault(rkey, defaultValue);
				ret.set(newval);
			}
		};
		follow(t -> {
			synchronized (receipt) {
				receipt[0] = true;
				receivedMap[0] = t;
				updateValue.run();
			}
		}, ret);
		key.follow((newValue) -> {
			synchronized (receipt) {
				receipt[1] = true;
				receivedKey[0] = newValue;
				updateValue.run();
			}
		}, ret);
		return ret;
	}

	@Override
	public ObjHolder<V> at(K key, V defaultValue) {
		ObjHolderSimple<V> ret = new ObjHolderSimple<>();
		follow(t -> {
			ret.set(t.getOrDefault(key, defaultValue));
		}, ret);
		return ret;
	}

	@Getter(lazy = true)
	@Accessors(fluent = true)
	private final IntHolder size = mapInt(Map::size);

	@Getter(lazy = true)
	@Accessors(fluent = true)
	private final BoolHolder isEmpty = test(Map::isEmpty);

	@Getter(lazy = true)
	@Accessors(fluent = true)
	private final SetHolder<K> keys = toSet(Map::keySet);

	@Getter(lazy = true)
	@Accessors(fluent = true)
	private final ListHolder<V> values = toList(Map::values);

	@Getter(lazy = true)
	@Accessors(fluent = true)
	private final SetHolder<Entry<K, V>> entries = toSet(Map::entrySet);

	@Override
	public MapHolder<K, V> filter(Predicate<K> keyFilter, Predicate<V> valueFilter) {
		if (keyFilter == null && valueFilter == null) {
			return this;
		}
		MapHolderImpl<K, V> ret = new MapHolderImpl<>();
		follow(map -> {
			Map<K, V> newMap = new HashMap<>();
			for (Entry<K, V> e : map.entrySet()) {
				K k = e.getKey();
				V v = e.getValue();
				if ((keyFilter == null || keyFilter.test(k)) && (valueFilter == null || valueFilter.test(v))) {
					newMap.put(k, v);
				}
			}
			ret.set(newMap);
		}, ret);
		return ret;
	}

	@SuppressWarnings("unchecked")
	@Override
	public MapHolder<K, V> filterKeys(CollectionHolder<K, ?> allowedKeys) {
		MapHolderImpl<K, V> ret = new MapHolderImpl<>();
		Map<K, V>[] lastMap = new Map[1];
		Collection<K>[] lastCol = new Collection[1];
		Runnable update = () -> {
			synchronized (lastMap) {
				if (lastMap[0] != null && lastCol[0] != null) {
					Map<K, V> newMap = new HashMap<>(lastMap[0]);
					newMap.keySet().retainAll(lastCol[0]);
					ret.set(newMap);
				}
			}
		};

		follow(m -> {
			synchronized (lastMap) {
				lastMap[0] = m;
			}
			update.run();
		}, ret);
		allowedKeys.follow(l -> {
			synchronized (lastMap) {
				lastCol[0] = l;
			}
			update.run();
		}, ret);
		return ret;
	}

}
