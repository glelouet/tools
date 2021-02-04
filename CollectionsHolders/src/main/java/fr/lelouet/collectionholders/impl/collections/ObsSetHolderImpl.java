package fr.lelouet.collectionholders.impl.collections;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fr.lelouet.collectionholders.impl.numbers.ObsBoolHolderImpl;
import fr.lelouet.collectionholders.interfaces.ObsObjHolder;
import fr.lelouet.collectionholders.interfaces.collections.ObsMapHolder;
import fr.lelouet.collectionholders.interfaces.collections.ObsSetHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsBoolHolder;

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
public class ObsSetHolderImpl<U> extends AObsCollectionHolder<U, Set<U>> implements ObsSetHolder<U> {

	public ObsSetHolderImpl(Set<U> set) {
		super(set);
	}

	public ObsSetHolderImpl() {
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

	@Override
	public void setEmpty() {
		super.set(Collections.emptySet());
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
	public static <U> ObsSetHolderImpl<U> of(U... items) {
		return new ObsSetHolderImpl<>(new HashSet<>(Arrays.asList(items)));
	}

	@Override
	public ObsSetHolderImpl<U> filter(Predicate<? super U> predicate) {
		ObsSetHolderImpl<U> ret = new ObsSetHolderImpl<>();
		follow((t) -> {
			Set<U> filteredSet = t.stream().filter(predicate).collect(Collectors.toSet());
			ret.set(filteredSet);
		}, ret);
		return ret;
	}

	@Override
	public ObsSetHolderImpl<U> filterWhen(Function<? super U, ObsBoolHolder> filterer) {
		ObsSetHolderImpl<U> ret = new ObsSetHolderImpl<>();
		filterWhen(filteredStream -> {
			Set<U> filteredSet = filteredStream.collect(Collectors.toSet());
			ret.set(filteredSet);
		}, filterer, ret);
		return ret;
	}

	@Override
	public <K, V> ObsMapHolder<K, V> toMap(Function<U, K> keyExtractor, Function<U, V> valExtractor,
			BinaryOperator<V> collisionHandler) {
		return ObsMapHolderImpl.toMap(this, keyExtractor, valExtractor, collisionHandler);
	}

	@Override
	public ObsBoolHolderImpl contains(U value) {
		ObsBoolHolderImpl ret = new ObsBoolHolderImpl();
		follow((t) -> {
			ret.set(t.contains(value));
		}, ret);
		return ret;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ObsBoolHolderImpl contains(ObsObjHolder<U> value) {
		ObsBoolHolderImpl ret = new ObsBoolHolderImpl();
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
	public ObsSetHolder<U> distinct() {
		return this;
	}

}
