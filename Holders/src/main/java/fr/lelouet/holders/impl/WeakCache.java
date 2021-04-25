package fr.lelouet.holders.impl;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Function;

/**
 * A cache that present a single get(Key) method and returns a Value by caching
 * the result of a Function<Key, Value> generator.
 * <p>
 * The cache is using internal Map<Key, Weakreference<Value>> and each call is
 * synchronized after a first failed existence-check
 * </p>
 * <p>
 * This class supports both key and value set to null - at least as long as the
 * generator function handles null correctly.
 * </p>
 * <p>
 * Most code is based on the {@link WeakHashMap} class
 * </p>
 *
 * @author glelouet
 *
 */
public class WeakCache<Key, Value> implements Function<Key, Value> {

	protected static final WeakReference<Object> NULLREF = new WeakReference<>(null);

	protected final HashMap<Key, WeakReference<Value>> cache = new HashMap<>();

	/**
	 * Reference queue for cleared WeakValues
	 */
	protected final ReferenceQueue<Object> queue = new ReferenceQueue<>();

	protected final Function<Key, Value> generator;

	public WeakCache(Function<Key, Value> generator) {
		this.generator = generator;
	}

	/**
	 * static reference to the constructor, for readability.<br />
	 * It's more readable to have {@code
	 * WeakCache.of(gen)} than {@code new WeakCache(gen)}, and more readable to
	 * use {@code WeakCache::of} than {@code WeakCache::new}
	 *
	 * @param <U>
	 *          Key type
	 * @param <V>
	 *          Value type
	 * @param generator
	 *          the function to generate a missing value.
	 * @return a new cache.
	 */
	public static <U, V> WeakCache<U, V> of(Function<U, V> generator) {
		return new WeakCache<>(generator);
	}

	@SuppressWarnings("unchecked")
	public Value get(Key key) {
		removeEmptyRef();
		WeakReference<Value> ref = cache.get(key);
		if (ref == NULLREF) {
			return null;
		}
		Value ret = ref != null ? ref.get() : null;
		if (ret == null) {
			synchronized (cache) {
				ref = cache.get(key);
				if (ref == NULLREF) {
					return null;
				}
				ret = ref != null ? ref.get() : null;
				if (ret == null) {
					ret = generator.apply(key);
					ref = ret == null ? (WeakReference<Value>) NULLREF : new WeakReference<>(ret, queue);
					cache.put(key, ref);
				}
			}
		}
		return ret;
	}

	/**
	 * checks if the queue contains a value, in which case it empties it in a set,
	 * and then remove the values from the cached map.
	 */
	@SuppressWarnings("unchecked")
	private void removeEmptyRef() {
		Object item = queue.poll();
		if (item != null) {
			synchronized (cache) {
				Set<WeakReference<Value>> remove = new HashSet<>();
				remove.add((WeakReference<Value>) item);
				for (Object x; (x = queue.poll()) != null;) {
					remove.add((WeakReference<Value>) x);
				}
				cache.values().removeAll(remove);
			}
		}
	}

	@Override
	public Value apply(Key t) {
		return get(t);
	}

	/**
	 * synchronized call to internal map.
	 *
	 * @param k
	 */
	public void remove(Key k) {
		synchronized (cache) {
			cache.remove(k);
		}
	}

	/**
	 *
	 * @return the number of entries stored, after removing the empty references.
	 */
	public int size() {
		removeEmptyRef();
		return cache.size();
	}

}
