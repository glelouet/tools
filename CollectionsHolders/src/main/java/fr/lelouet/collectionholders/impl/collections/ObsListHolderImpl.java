package fr.lelouet.collectionholders.impl.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fr.lelouet.collectionholders.interfaces.collections.ObsListHolder;
import fr.lelouet.collectionholders.interfaces.collections.ObsMapHolder;
import fr.lelouet.collectionholders.interfaces.collections.ObsSetHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsBoolHolder;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 *
 * implementation of the writable List holder.
 * <p>
 * the {@link #set(List)} methods modifies the stored data : a null data is
 * translated to {@link Collections.#emptyList()}, while a non null map is
 * translated to {@link Collections.#unmodifiableList(List)}
 * </p>
 *
 * @param <U>
 */
public class ObsListHolderImpl<U> extends AObsCollectionHolder<U, List<U>> implements ObsListHolder<U> {

	public ObsListHolderImpl() {
	}

	public ObsListHolderImpl(List<U> list) {
		super(list);
	}

	@Override
	public synchronized void set(List<U> newitem) {
		super.set(newitem == null ? Collections.emptyList() : Collections.unmodifiableList(newitem));
	}

	@SuppressWarnings("unchecked")
	public synchronized void set(U... items) {
		super.set(items == null ? Collections.emptyList() : Arrays.asList(items));
	}

	/**
	 * create an unmodifiable list of items
	 *
	 * @param <U>
	 *          type of the items
	 * @param args
	 *          items to add
	 * @return a new list
	 */
	@SafeVarargs
	public static <U> ObsListHolderImpl<U> of(U... args) {
		return new ObsListHolderImpl<>(Arrays.asList(args));
	}

	@Override
	public ObsListHolderImpl<U> filter(Predicate<? super U> predicate) {
		ObsListHolderImpl<U> ret = new ObsListHolderImpl<>();
		follow((t) -> {
			List<U> filteredList = t.stream().filter(predicate).collect(Collectors.toList());
			ret.set(filteredList);
		});
		return ret;
	}

	@Override
	public ObsListHolderImpl<U> filterWhen(Function<? super U, ObsBoolHolder> filterer) {
		ObsListHolderImpl<U> ret = new ObsListHolderImpl<>();
		filterWhen(filteredStream -> {
			List<U> filteredList = filteredStream.collect(Collectors.toList());
			ret.set(filteredList);
		}, filterer);
		return ret;
	}

	@Getter(lazy = true)
	@Accessors(fluent = true)
	private final ObsSetHolder<U> distinct = makeDistinct();

	protected ObsSetHolder<U> makeDistinct() {
		ObsSetHolderImpl<U> ret = new ObsSetHolderImpl<>();
		follow((l) -> {
			ret.set(new HashSet<>(l));
		});
		return ret;
	}

	@Override
	public <K, V> ObsMapHolder<K, V> toMap(Function<U, K> keyExtractor, Function<U, V> valExtractor,
			BinaryOperator<V> collisionHandler) {
		return ObsMapHolderImpl.toMap(this, keyExtractor, valExtractor, collisionHandler);
	}

	private ObsListHolderImpl<U> reverse = null;

	@Override
	public ObsListHolderImpl<U> reverse() {
		if (reverse == null) {
			synchronized (this) {
				if (reverse == null) {
					ObsListHolderImpl<U> ret = new ObsListHolderImpl<>();
					follow((o) -> {
						List<U> reverseList = new ArrayList<>(o);
						Collections.reverse(reverseList);
						ret.set(reverseList);
					});
					ret.reverse = this;
					reverse = ret;
				}
			}
		}
		return reverse;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ObsListHolder<U> concat(ObsListHolder<? extends U>... lists) {
		if (lists == null || lists.length == 0) {
			return this;
		}
		ObsListHolder<U>[] array = Stream.concat(Stream.of(this), lists == null ? Stream.empty() : Stream.of(lists))
				.filter(m -> m != null).toArray(ObsListHolder[]::new);
		ObsListHolderImpl<U> ret = new ObsListHolderImpl<>();
		LinkedHashMap<ObsListHolder<U>, List<U>> alreadyreceived = new LinkedHashMap<>();
		for (ObsListHolder<U> m : array) {
			m.follow(list -> {
				synchronized (alreadyreceived) {
					alreadyreceived.remove(m);
					alreadyreceived.put(m, list);
					if (alreadyreceived.size() == array.length) {
						List<U> newList = alreadyreceived.values().stream().flatMap(m2 -> m2.stream()).collect(Collectors.toList());
						ret.set(newList);
					}
				}
			});
		}
		return ret;
	}

	@Override
	public ObsListHolderImpl<U> peek(Consumer<List<U>> observer) {
		follow(observer);
		return this;
	}

}