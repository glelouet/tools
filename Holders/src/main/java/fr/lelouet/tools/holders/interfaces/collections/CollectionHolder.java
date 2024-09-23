package fr.lelouet.tools.holders.interfaces.collections;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

import fr.lelouet.tools.holders.interfaces.ObjHolder;
import fr.lelouet.tools.holders.interfaces.numbers.BoolHolder;
import fr.lelouet.tools.holders.interfaces.numbers.IntHolder;

/**
 * common interface for set and list.
 *
 * @param <U>
 *          class of the item in the collection
 * @param <C>
 *          Collection type to hold the data (eg List&lt;U&gt;)
 */
public interface CollectionHolder<U, C extends Collection<U>> extends ObjHolder<C> {

	@Override
	default CollectionHolder<U, C> follow(Consumer<C> listener) {
		ObjHolder.super.follow(listener);
		return this;
	}

	/**
	 *
	 * get the variable for this collection's size.
	 *
	 * @return an internally cached variable constrained to the size of the
	 *         internal collection last time it received data.
	 */
	public IntHolder size();

	public BoolHolder isEmpty();

	/**
	 * create a filtered collection working on bulk process
	 *
	 * @param predicate
	 *          the predicate to select the items
	 * @return a new collection with same parameterized signature.
	 */
	public CollectionHolder<U, C> filter(Predicate<? super U> predicate);

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
	public CollectionHolder<U, C> filterWhen(Function<? super U, BoolHolder> filterer);

	/**
	 * map each item in this to a new item in another collection
	 *
	 * @return a new collection that contains the mapping this data through the
	 *         mapper
	 */
	public <K> CollectionHolder<K, ?> mapItems(Function<U, K> mapper);

	public default <K> MapHolder<K, List<U>> grouping(Function<U, K> indexer) {
		return mapMap(coll -> coll.stream().collect(Collectors.groupingBy(indexer)));
	}

	/**
	 * For each item in this, create a holder using a mapper, and update a
	 * collection with the data of the holders. The returned collection is updated
	 * when
	 *
	 * @param unpacker
	 * @return a new collection that contains the mapping this data through the
	 *         mapper
	 */
	public <K> CollectionHolder<K, ?> unpackItems(Function<U, ObjHolder<K>> unpacker);

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
	public default <K> MapHolder<K, U> toMap(Function<U, K> keyExtractor) {
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
	public default <K, V> MapHolder<K, V> toMap(Function<U, K> keyExtractor, Function<U, V> valExtractor) {
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
	public <K, V> MapHolder<K, V> toMap(Function<U, K> keyExtractor, Function<U, V> valExtractor,
			BinaryOperator<V> collisionHandler);

	/**
	 * join the items in this using a mapper and a joiner.<br />
	 * example : if this is a collections of the strings "my", "cat", then joining
	 * it with the mapper String::size, the joiner Integer::sum and the neutral 0
	 * will result in the sum of the sizes of the strings 0+2+3 = 5
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
	 * @return a new holder containing the joining of the elements of this
	 *         collection.
	 */
	public default <V> ObjHolder<V> reduce(Function<U, V> mapper, BinaryOperator<V> joiner, V neutral) {
		return map(l -> l.stream().map(mapper).collect(Collectors.reducing(neutral, joiner)));
	}

	/**
	 * join the items in this using a joiner.<br />
	 * Example : if this is a collection of integers 1,2,3 , then joining them
	 * with "+" and the neutral "0" will result in the sum 0+1+2+3 = 6.
	 *
	 * @param joiner
	 *          joins items into one.
	 * @param neutral
	 *          the neutral value for joining. if not data is present, this value
	 *          is returned.
	 * @return a new holder containing the joining on the elements of this
	 *         collection.
	 */
	public default ObjHolder<U> reduce(BinaryOperator<U> joiner, U neutral) {
		return map(l -> l.stream().collect(Collectors.reducing(neutral, joiner)));
	}

	public default IntHolder reduceInt(ToIntFunction<U> mapper, IntBinaryOperator joiner, int neutral) {
		return mapInt(l -> l.stream().mapToInt(mapper).reduce(neutral, joiner));
	}

	/**
	 *
	 * @return a set containing all the elements of this collection in a single
	 *         occurrence.
	 */
	public SetHolder<U> distinct();

	/**
	 * @param comparator
	 *          to compare elements one to another
	 * @return a list containing the elements of this, sorted using comparator
	 */
	public ListHolder<U> sorted(Comparator<U> comparator);

	public default <T extends Comparable<? super T>> ListHolder<U> sorted(Function<? super U, ? extends T> keyExtractor) {
		return sorted(Comparator.comparing(keyExtractor));
	}

	/**
	 * make the product List of this collection elements with another one. Example
	 * if this is the collection of chars 'a', 'b' and the other collection
	 * contains the ints '1', '2', if the operand is the concatentaion then this
	 * will return the collection containing 'a1', 'a2', 'b1', 'b2'.
	 *
	 * @param <V>
	 * @param <O>
	 *          the elements of the returned list
	 * @param right
	 *          the other collection
	 * @param operand
	 *          the operation to apply to each couple (left, right) from this
	 *          collection × the other collection
	 * @return a new list
	 */
	public <V, O> CollectionHolder<O, ?> prodList(CollectionHolder<V, ?> right, BiFunction<U, V, O> operand);

	/**
	 * flatten this by converting all the elements to {@link CollectionHolder}
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
	public <V, C2 extends Collection<V>> ListHolder<V> flatten(Function<U, CollectionHolder<V, C2>> mapper);

}
