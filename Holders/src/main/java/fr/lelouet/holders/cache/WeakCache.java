package fr.lelouet.holders.cache;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Function;

/**
 * A cache that present a single get(Key) method and returns a Value by caching
 * the result of a Function<Key, Value> generator. The method
 * {@link WeakCache#of} provides easy encapsulation of a function to make it a
 * cached one.
 * <p>
 * This class supports both key and value set to null - at least as long as the
 * generator function handles null correctly.
 * </p>
 * <p>
 * It implements Function<Key, Value> to propose the default Function methods eg
 * {@link Function#compose}
 * </p>
 * <p>
 * The cache is using an internal Map<Key, Weakreference<Value>> and the
 * {@link #get(Object)} calls are synchronized after a first failed check ( see
 * Double-checked locking )
 * </p>
 * <p>
 * The {@link #removeEmptyReferences()} is only called internally when a
 * modification is called. This method is very fast and can be called regularly
 * by an external manager to ensure a minimal memory footprint ; however this
 * should not have real impact on the memory unless the cached elements are very
 * small AND they are created in bulk, then the cache is never used again.
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
	private final ReferenceQueue<Object> queue = new ReferenceQueue<>();

	private final Function<Key, Value> generator;

	/**
	 * create a new WeakCache based on a function
	 *
	 * @param generator
	 *          the function to generate items that will be cached. Not null.
	 */
	public WeakCache(Function<Key, Value> generator) {
		this.generator = generator;
		Objects.requireNonNull(generator);
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
		removeEmptyReferences();
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
	 * <p>
	 * checks if the queue contains a value, in which case it empties it in a set,
	 * and then remove the values from the cached map.
	 * </p>
	 * <p>
	 * This only removes the existing dereferenced ; between this invocation and
	 * the next instruction, there can still be more GC cycles done, so we can't
	 * assume when exiting that the cache does not contain weakreferences set to
	 * null. However this is good for freeing a bit of memory.
	 * </p>
	 */
	@SuppressWarnings("unchecked")
	public void removeEmptyReferences() {
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
		removeEmptyReferences();
		return cache.size();
	}

}
