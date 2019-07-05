package fr.lelouet.collectionholders.interfaces.collections;

import java.util.Collection;
import java.util.Comparator;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

import fr.lelouet.collectionholders.interfaces.ObsObjHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsDoubleHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsIntHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsLongHolder;

/**
 * common interface for set and list.
 *
 * @param <U>
 *          class of the item in the collection
 * @param <C>
 *          Collection type to hold the data (eg List&lt;U&gt;)
 * @param <L>
 *          Listener type passed to observe the internal data (eg
 *          ListEventListener&lt;U&gt;)
 */
public interface ObsCollectionHolder<U, C extends Collection<U>, L> extends ObsObjHolder<C> {


	/**
	 * iterate over all the elements in the collections, after it's been set, and
	 * apply a consumer on each
	 *
	 * @param cons
	 */
	public void apply(Consumer<U> cons);

	/**
	 *
	 * get the variable for this collection's size.
	 *
	 * @return an internally cached variable constrained to the size of the
	 *         internal collection last time it received data.
	 */
	public ObsIntHolder size();

	/**
	 * apply all existing values to the change listener, and register it as a
	 * listener of the underlying list.
	 *
	 * @param listener
	 */
	void followItems(L listener);

	void unfollowItems(L change);

	/**
	 * called by the data fetcher when data has been received. This specifies that
	 * the items stored are consistent and can be used as a bulk.
	 */
	void dataReceived();

	/**
	 * register a runnable to be run once {@link #dataReceived()} is called. The
	 * call is made in a new thread.
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
	 * create a filtered collection working on bulk process
	 *
	 * @param predicate
	 *          the predicate to select the items
	 * @return a new collection with same parametrized signature.
	 */
	public ObsCollectionHolder<U, C, L> filter(Predicate<? super U> predicate);

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
	public <K> ObsMapHolder<K, U> mapItems(Function<U, K> keyExtractor);

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
	public <K, V> ObsMapHolder<K, V> mapItems(Function<U, K> keyExtractor, Function<U, V> valExtractor);

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
		return reduce(l -> l.stream().map(mapper).collect(Collectors.reducing(neutral, joiner)));
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
		return reduce(l -> l.stream().collect(Collectors.reducing(neutral, joiner)));
	}

	/**
	 *
	 * @param <V>
	 *          type of returned holder
	 * @param collectionReducer
	 *          function that is applied to the collection whenever data is
	 *          received
	 * @return a new variable containing the reduced value when data is received.
	 */
	public <V> ObsObjHolder<V> reduce(Function<C, V> collectionReducer);

	/**
	 *
	 * @param collectionReducer
	 *          function that is applied to the collection whenever data is
	 *          received
	 * @return a new variable containing the reduced value when data is received.
	 */
	public ObsIntHolder reduceInt(ToIntFunction<C> collectionReducer);

	/**
	 * @param collectionReducer
	 *          function that is applied to the collection whenever data is
	 *          received
	 * @return a new variable containing the reduced value when data is received.
	 */
	public ObsDoubleHolder reduceDouble(ToDoubleFunction<C> collectionReducer);

	/**
	 * @param collectionReducer
	 *          function that is applied to the collection whenever data is
	 *          received
	 * @return a new variable containing the reduced value when data is received.
	 */
	public ObsLongHolder reduceLong(ToLongFunction<C> collectionReducer);

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
	public <V, O> ObsListHolder<O> prodList(ObsCollectionHolder<V, ?, ?> right, BiFunction<U, V, O> operand);

}
