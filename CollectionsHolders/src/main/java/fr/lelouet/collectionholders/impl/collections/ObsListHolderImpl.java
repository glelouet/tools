package fr.lelouet.collectionholders.impl.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fr.lelouet.collectionholders.interfaces.ObsObjHolder;
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
	public void set(List<U> newitem) {
		super.set(newitem == null ? Collections.emptyList() : Collections.unmodifiableList(newitem));
	}

	@SuppressWarnings("unchecked")
	public void set(U item1, U... items) {
		super.set(Stream.concat(Stream.of(item1), items == null ? Stream.empty() : Stream.of(items))
				.collect(Collectors.toList()));
	}

	@Override
	public void setEmpty() {
		super.set(Collections.emptyList());
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
		}, ret);
		return ret;
	}

	@Override
	public ObsListHolderImpl<U> filterWhen(Function<? super U, ObsBoolHolder> filterer) {
		ObsListHolderImpl<U> ret = new ObsListHolderImpl<>();
		filterWhen(filteredStream -> {
			List<U> filteredList = filteredStream.collect(Collectors.toList());
			ret.set(filteredList);
		}, filterer, ret);
		return ret;
	}

	@Getter(lazy = true)
	@Accessors(fluent = true)
	private final ObsSetHolder<U> distinct = makeDistinct();

	protected ObsSetHolder<U> makeDistinct() {
		ObsSetHolderImpl<U> ret = new ObsSetHolderImpl<>();
		follow((l) -> {
			ret.set(new HashSet<>(l));
		}, ret);
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
					}, ret);
					ret.reverse = this;
					reverse = ret;
				}
			}
		}
		return reverse;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ObsListHolderImpl<U> concat(ObsListHolder<? extends U> first, ObsListHolder<? extends U>... lists) {
		List<ObsListHolder<? extends U>> wholeList = Stream
				.concat(Stream.of(this, first), lists == null ? Stream.empty() : Stream.of(lists)).filter(m -> m != null)
				.collect(Collectors.toList());
		Function<List<? extends List<? extends U>>, List<U>> reducer = newLists -> newLists.stream()
				.flatMap(l -> l.stream()).collect(Collectors.toList());
		return ObsObjHolder.reduce(wholeList, ObsListHolderImpl::new, reducer);
	}

}