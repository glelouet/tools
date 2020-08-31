package fr.lelouet.tools.solver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * index items in a deterministic way. Being deterministic implies a comparator
 * on the items, them being not null and no couple of item being equal wrt the
 * comparator to another one after unique items filtering.
 *
 * <p>
 * Allows to iterate through them
 * <ul>
 * <li>with {@link #size()} and {@link #item(int)}</li>
 * <li>with an {@link #iterator()}</li>
 * <li>as a {@link #stream()}</li>
 * </ul>
 * </p>
 * <p>
 * Can be directly created by providing a comparator, or with the static
 * {@link #of(Collection)} and {@link #of(Comparable...)} if the type is
 * {@link Comparable}.<br />
 * In any case, items are removed the null values and the construction will fail
 * if two different items return a comparison value of 0.
 * </p>
 *
 * @author glelouet
 *
 */
public class Indexer<T> implements Iterable<T> {

	private final Object[] items;
	private final Map<T, Integer> positions;

	/**
	 * constructor from sorted lit of items.
	 *
	 * @param items
	 */
	private Indexer(List<T> items) {
		this.items = items.toArray(Object[]::new);
		this.positions = IntStream.range(0, items.size()).boxed().collect(Collectors.toMap(items::get, i -> i));
	}

	@SafeVarargs
	public Indexer(Comparator<T> comp, T... items) {
		this(comp, Arrays.asList(items));
	}

	public Indexer(Comparator<T> comp, Collection<T> items) {
		this(sort(comp, items));
	}

	/**
	 * create a sorted list from a collection and a comparator
	 *
	 * @param <U>
	 * @param comp
	 * @param items
	 * @return the list of items, removed from the null values, and removed from
	 *         multiple items
	 */
	private static <U> List<U> sort(Comparator<U> comp, Collection<U> items) {
		List<U> ret = new ArrayList<>(items.stream().filter(it -> it != null).collect(Collectors.toSet()));
		Collections.sort(ret, comp);
		for (int i = 1; i < ret.size(); i++) {
			if (comp.compare(ret.get(i), ret.get(i - 1)) == 0) {
				throw new UnsupportedOperationException(
						"items " + ret.get(i) + " and " + ret.get(i - 1) + " have comparison of 0");
			}
		}
		return ret;
	}

	@SafeVarargs
	public static <U extends Comparable<U>> Indexer<U> of(U... items) {
		return new Indexer<>(Comparator.naturalOrder(), items);
	}

	public static <U extends Comparable<U>> Indexer<U> of(Collection<U> items) {
		return new Indexer<>(Comparator.naturalOrder(), items);
	}

	/**
	 * get the item at given position
	 *
	 * @param position
	 *          position
	 * @return the item at that position
	 */
	@SuppressWarnings("unchecked")
	public T item(int position) {
		return (T) items[position];
	}

	/**
	 * get the position of given item
	 *
	 * @param item
	 *          an item
	 * @return the position of that item, or -1
	 */
	public int position(T item) {
		return positions.getOrDefault(item, -1);
	}

	public int size() {
		return items.length;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<T> iterator() {
		return (Iterator<T>) Collections.unmodifiableCollection(Arrays.asList(items)).iterator();
	}

	@SuppressWarnings("unchecked")
	public Stream<T> stream() {
		return (Stream<T>) Arrays.asList(items).stream();
	}

}
