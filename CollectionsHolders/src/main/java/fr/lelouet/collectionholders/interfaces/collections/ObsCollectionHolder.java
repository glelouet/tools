package fr.lelouet.collectionholders.interfaces.collections;

import java.util.Collection;
import java.util.Comparator;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

import fr.lelouet.collectionholders.interfaces.ObsObjHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsBoolHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsIntHolder;

/**
 * common interface for set and list.
 *
 * @param <U>
 *          class of the item in the collection
 * @param <C>
 *          Collection type to hold the data (eg List&lt;U&gt;)
 */
public interface ObsCollectionHolder<U, C extends Collection<U>> extends ObsObjHolder<C> {

	/**
	 *
	 * get the variable for this collection's size.
	 *
	 * @return an internally cached variable constrained to the size of the
	 *         internal collection last time it received data.
	 */
	public ObsIntHolder size();

	public ObsBoolHolder isEmpty();

	/**
	 * create a filtered collection working on bulk process
	 *
	 * @param predicate
	 *          the predicate to select the items
	 * @return a new collection with same parameterized signature.
	 */
	public ObsCollectionHolder<U, C> filter(Predicate<? super U> predicate);

	/**
	 * create a filtered collection on an observable predicate.
	 * <p>
	 * Formally, it ensure that the returned collection only holds and hold all,
	 * the elements for which the filterer made a observable boolean holder that
	 * contains true.
	 * </p>
	 * <p>
	 * An example would be to filter a list of map, by checking if those maps are
	 * not empty.<br />
	 * <code>listofmaps.filterWhen(m->m.isEmpty().not())</code>
	 * </p>
	 *
	 * @param filterer
	 *          the mapping of items to their observable filter
	 * @return a new collection with same parameterized signature.
	 */
	public ObsCollectionHolder<U, C> filterWhen(Function<? super U, ObsBoolHolder> filterer);

	/**
	 * map each item in this to a new item in another collection
	 *
	 * @return a new collection that contains the mapping this data through the
	 *         mapper
	 */
	public <K> ObsCollectionHolder<K, ?> mapItems(Function<U, K> mapper);

	/**
	 * For each item in this, create a holder using a mapper, and update a
	 * collection with the data of the holders. The returned collection is updated
	 * when
	 *
	 * @param unpacker
	 * @return a new collection that contains the mapping this data through the
	 *         mapper
	 */
	public <K> ObsCollectionHolder<K, ?> unpackItems(Function<U, ObsObjHolder<K>> unpacker);

	/**
	 * map this collection to a new Map. In case of collision in the key function,
	 * only the last added elements are mapped.
	 *
	 * @param <K>
	 *          the key type of the map
	 * @param keyExtractor
	 *          the function to create a key from this list's values
	 * @return a new map that contains this list's items, with the corresponding
	 *         key.
	 */
	public default <K> ObsMapHolder<K, U> toMap(Function<U, K> keyExtractor) {
		return toMap(keyExtractor, v -> v, (a, b) -> b);
	}

	/**
	 * map the items in this into a new Map. In case of collision in the key
	 * function, only the last added elements are mapped.
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
	public default <K, V> ObsMapHolder<K, V> toMap(Function<U, K> keyExtractor, Function<U, V> valExtractor) {
		return toMap(keyExtractor, valExtractor, (a, b) -> b);
	}

	/**
	 * map the items in this into a new Map.
	 *
	 * @param <K>
	 *          the key type of the map
	 * @param <V>
	 *          the value type of the map
	 * @param keyExtractor
	 *          function to transform an element in the key
	 * @param valExtractor
	 *          function to transform an element in the value
	 * @param collisionHandler
	 *          the function to handle the case where to values are mapped to the
	 *          same key.
	 * @return a new map.
	 */
	public <K, V> ObsMapHolder<K, V> toMap(Function<U, K> keyExtractor, Function<U, V> valExtractor,
			BinaryOperator<V> collisionHandler);

	/**
	 * join the items in this using a mapper and a joiner
	 *
	 * @param <V>
	 *          Conversion class
	 * @param mapper
	 *          converter. cannot be null
	 * @param joiner
	 *          joins mapped items into one.
	 * @param neutral
	 *          the neutral value for joining. if not data is present, this value
	 *          is returned.
	 * @return a new variable V, only modified when this receives data.
	 */
	public default <V> ObsObjHolder<V> reduce(Function<U, V> mapper, BinaryOperator<V> joiner, V neutral) {
		return map(l -> l.stream().map(mapper).collect(Collectors.reducing(neutral, joiner)));
	}

	/**
	 * join the items in this using a joiner
	 *
	 * @param joiner
	 *          joins items into one.
	 * @param neutral
	 *          the neutral value for joining. if not data is present, this value
	 *          is returned.
	 * @return a new variable U, only modified when this receives data.
	 */
	public default ObsObjHolder<U> reduce(BinaryOperator<U> joiner, U neutral) {
		return map(l -> l.stream().collect(Collectors.reducing(neutral, joiner)));
	}

	public default ObsIntHolder reduceInt(ToIntFunction<U> mapper, IntBinaryOperator joiner, int neutral) {
		return mapInt(l -> l.stream().mapToInt(mapper).reduce(neutral, joiner));
	}

	/**
	 *
	 * @return a set containing all the elements of this collection in a single
	 *         occurrence.
	 */
	public ObsSetHolder<U> distinct();

	/**
	 * @param comparator
	 *          to compare elements one to another
	 * @return a list containing the elements of this, sorted using comparator
	 */
	public ObsListHolder<U> sorted(Comparator<U> comparator);

	/**
	 * make the product List of this collection elements with another one
	 *
	 * @param <V>
	 * @param <O>
	 *          the elemnts of the returned list
	 * @param right
	 *          the other collection
	 * @param operand
	 *          the operation to apply to each couple (left, right) from this
	 *          collection × the other collection
	 * @return a new list
	 */
	public <V, O> ObsListHolder<O> prodList(ObsCollectionHolder<V, ?> right, BiFunction<U, V, O> operand);

	/**
	 * flatten this by converting all the elements to {@link ObsCollectionHolder}
	 * and merging them. The mapped collections are listened to, whenever a new
	 * one is to be created.
	 *
	 * @param <V>
	 *          type of the items hold in the sub collections
	 * @param mapper
	 *          the function to convert each element in the collection, to a
	 *          collection of V.
	 * @return a new collection holder that contains the items holds in the sub
	 *         collections.
	 */
	public <V, C2 extends Collection<V>> ObsListHolder<V> flatten(Function<U, ObsCollectionHolder<V, C2>> mapper);

}
