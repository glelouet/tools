package fr.lelouet.holders.interfaces.collections;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import fr.lelouet.holders.interfaces.ObjHolder;
import fr.lelouet.holders.interfaces.numbers.BoolHolder;
import fr.lelouet.holders.interfaces.numbers.IntHolder;

/**
 * Holder on an underlying observable map.
 *
 * @param <K>
 * @param <V>
 */
public interface MapHolder<K, V> extends ObjHolder<Map<K, V>> {

	@Override
	default MapHolder<K, V> follow(Consumer<Map<K, V>> listener) {
		ObjHolder.super.follow(listener);
		return this;
	}

	@Override
	Map<K, V> get();

	/**
	 * create a new variable bound to the value mapped to a key
	 *
	 * @param key
	 * @param defaultValue
	 *          the value to be used in case the key is not present.
	 * @return a new variable
	 */
	ObjHolder<V> at(K key, V defaultValue);

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
	ObjHolder<V> at(ObjHolder<K> key, V devaultValue);

	/**
	 *
	 * get the variable for this size.
	 *
	 * @return an internally cached variable constrained to the size of the
	 *         internal map last time it received data.
	 */
	public IntHolder size();

	/**
	 *
	 * @return an internal cached variable constraiend to be true when the data is
	 *         an empyt list, false otherwise.
	 */
	public BoolHolder isEmpty();

	/**
	 * merge this map with other. In case of collision, use the value from the map
	 * which has received data the last.
	 *
	 * @param m1
	 * @param maps
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public default MapHolder<K, V> merge(MapHolder<K, V>... maps) {
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
	public MapHolder<K, V> merge(BinaryOperator<V> merger, MapHolder<K, V>... maps);

	/**
	 *
	 * @return a set that contains all keys used in the internal collection.
	 */
	SetHolder<K> keys();

	default BoolHolder containsKey(K key) {
		return keys().contains(key);
	}

	/**
	 *
	 * @return a collection holder that contains all the values contained in the
	 *         internal collections
	 */
	CollectionHolder<V, ?> values();

	/**
	 *
	 * @return a collection holder that contains all the entries of the internal
	 *         Map.
	 */
	CollectionHolder<Entry<K, V>, ?> entries();

	/**
	 * remap the values.
	 *
	 * @param <U>
	 * @param mapper
	 *          remapper
	 * @return a new remapped map.
	 */
	default <U> MapHolder<K, U> mapValues(Function<V, U> mapper) {
		return entries().toMap(Entry::getKey, e -> mapper.apply(e.getValue()));
	}

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
	MapHolder<K, V> filter(Predicate<K> keyFilter, Predicate<V> valueFilter);

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
	MapHolder<K, V> filterKeys(CollectionHolder<K, ?> allowedKeys);


}
