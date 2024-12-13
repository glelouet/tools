package fr.lelouet.tools.holders.impl.collections;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fr.lelouet.tools.holders.impl.numbers.BoolHolderImpl;
import fr.lelouet.tools.holders.interfaces.ObjHolder;
import fr.lelouet.tools.holders.interfaces.collections.MapHolder;
import fr.lelouet.tools.holders.interfaces.collections.SetHolder;
import fr.lelouet.tools.holders.interfaces.numbers.BoolHolder;

/**
 *
 * /** implementation of the writable Set holder.
 * <p>
 * the set methods modifies the stored data : a null data is translated to
 * {@link Collections.#emptySet()}, while a non null map is translated to
 * {@link Collections.#unmodifiableSet(Set)}.
 * </p>
 *
 * @param <U>
 */
public class SetHolderImpl<U> extends ACollectionHolder<U, Set<U>> implements SetHolder<U> {

	public SetHolderImpl(Set<U> set) {
		super(set);
	}

	public SetHolderImpl() {
	}

	@Override
	public void set(Set<U> newitem) {
		super.set(newitem == null ? Collections.emptySet() : Collections.unmodifiableSet(newitem));
	}

	@SuppressWarnings("unchecked")
	public void set(U item1, U... items) {
		super.set(
				Stream.concat(Stream.of(item1), items == null ? Stream.empty() : Stream.of(items)).collect(Collectors.toSet()));
	}

	/**
	 * create a unmodifiable observable set of items
	 *
	 * @param <U>
	 *          type of the items
	 * @param items
	 *          items to add.
	 * @return a new observable set
	 */
	@SafeVarargs
	public static <U> SetHolderImpl<U> of(U... items) {
		return new SetHolderImpl<>(new HashSet<>(Arrays.asList(items)));
	}

	@SafeVarargs
	public static <U> SetHolderImpl<U> union(SetHolder<U>... items) {
		if (items == null || items.length == 0) {
			return new SetHolderImpl<>();
		}
		return ObjHolder.reduce(List.of(items),
		    () -> new SetHolderImpl<>(),
		    sets -> {
			    Set<U> union = new HashSet<>();
			    for (Set<U> set : sets) {
				    union.addAll(set);
			    }
			    return union;
		    });
	}

	@Override
	public void setEmpty() {
		super.set(Collections.emptySet());
	}

	@Override
	public SetHolderImpl<U> filter(Predicate<? super U> predicate) {
		SetHolderImpl<U> ret = new SetHolderImpl<>();
		follow((t) -> {
			Set<U> filteredSet = t.stream().filter(predicate).collect(Collectors.toSet());
			ret.set(filteredSet);
		}, ret);
		return ret;
	}

	@Override
	public SetHolderImpl<U> filterWhen(Function<? super U, BoolHolder> filterer) {
		SetHolderImpl<U> ret = new SetHolderImpl<>();
		filterWhen(filteredStream -> {
			Set<U> filteredSet = filteredStream.collect(Collectors.toSet());
			ret.set(filteredSet);
		}, filterer, ret);
		return ret;
	}

	@Override
	public <K, V> MapHolder<K, V> toMap(Function<U, K> keyExtractor, Function<U, V> valExtractor,
			BinaryOperator<V> collisionHandler) {
		return MapHolderImpl.toMap(this, keyExtractor, valExtractor, collisionHandler);
	}

	@Override
	public BoolHolderImpl contains(U value) {
		BoolHolderImpl ret = new BoolHolderImpl();
		follow((t) -> {
			ret.set(t.contains(value));
		}, ret);
		return ret;
	}

	@SuppressWarnings("unchecked")
	@Override
	public BoolHolderImpl contains(ObjHolder<U> value) {
		BoolHolderImpl ret = new BoolHolderImpl();
		Object[] recVal = new Object[1];
		Object[] recSet = new Object[1];
		boolean[] receipt = new boolean[] { false, false };
		Runnable update = () -> {
			if (receipt[0] && receipt[1]) {
				U val = (U) recVal[0];
				Set<U> set = (Set<U>) recSet[0];
				ret.set(set.contains(val));
			}
		};
		follow((t) -> {
			synchronized (receipt) {
				receipt[0] = true;
				recSet[0] = t;
				update.run();
			}
		}, ret);
		value.follow((newValue) -> {
			synchronized (receipt) {
				receipt[1] = true;
				recVal[0] = newValue;
				update.run();
			}
		}, ret);
		return ret;
	}

	@Override
	public SetHolder<U> distinct() {
		return this;
	}

}
