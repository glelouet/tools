package fr.lelouet.tools.holders.impl.collections;

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

import fr.lelouet.tools.holders.interfaces.ObjHolder;
import fr.lelouet.tools.holders.interfaces.collections.ListHolder;
import fr.lelouet.tools.holders.interfaces.collections.MapHolder;
import fr.lelouet.tools.holders.interfaces.collections.SetHolder;
import fr.lelouet.tools.holders.interfaces.numbers.BoolHolder;
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
public class ListHolderImpl<U> extends ACollectionHolder<U, List<U>> implements ListHolder<U> {

	public ListHolderImpl() {
	}

	public ListHolderImpl(List<U> list) {
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
	public static <U> ListHolderImpl<U> of(U... args) {
		return new ListHolderImpl<>(Arrays.asList(args));
	}

	@Override
	public ListHolderImpl<U> filter(Predicate<? super U> predicate) {
		ListHolderImpl<U> ret = new ListHolderImpl<>();
		follow((t) -> {
			List<U> filteredList = t.stream().filter(predicate).collect(Collectors.toList());
			ret.set(filteredList);
		}, ret);
		return ret;
	}

	@Override
	public ListHolderImpl<U> filterWhen(Function<? super U, BoolHolder> filterer) {
		ListHolderImpl<U> ret = new ListHolderImpl<>();
		filterWhen(filteredStream -> {
			List<U> filteredList = filteredStream.collect(Collectors.toList());
			ret.set(filteredList);
		}, filterer, ret);
		return ret;
	}

	@Getter(lazy = true)
	@Accessors(fluent = true)
	private final SetHolder<U> distinct = makeDistinct();

	protected SetHolder<U> makeDistinct() {
		SetHolderImpl<U> ret = new SetHolderImpl<>();
		follow((l) -> {
			ret.set(new HashSet<>(l));
		}, ret);
		return ret;
	}

	@Override
	public <K, V> MapHolder<K, V> toMap(Function<U, K> keyExtractor, Function<U, V> valExtractor,
			BinaryOperator<V> collisionHandler) {
		return MapHolderImpl.toMap(this, keyExtractor, valExtractor, collisionHandler);
	}

	private ListHolderImpl<U> reverse = null;

	@Override
	public ListHolderImpl<U> reverse() {
		if (reverse == null) {
			synchronized (this) {
				if (reverse == null) {
					ListHolderImpl<U> ret = new ListHolderImpl<>();
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
	public ListHolderImpl<U> concat(ListHolder<? extends U> first, ListHolder<? extends U>... lists) {
		List<ListHolder<? extends U>> wholeList = Stream
				.concat(Stream.of(this, first), lists == null ? Stream.empty() : Stream.of(lists)).filter(m -> m != null)
				.collect(Collectors.toList());
		Function<List<? extends List<? extends U>>, List<U>> reducer = newLists -> newLists.stream()
				.flatMap(l -> l.stream()).collect(Collectors.toList());
		return ObjHolder.reduce(wholeList, ListHolderImpl::new, reducer);
	}

}