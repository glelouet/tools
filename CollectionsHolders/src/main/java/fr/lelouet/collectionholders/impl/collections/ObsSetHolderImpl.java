package fr.lelouet.collectionholders.impl.collections;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import fr.lelouet.collectionholders.impl.numbers.ObsBoolHolderImpl;
import fr.lelouet.collectionholders.interfaces.ObsObjHolder;
import fr.lelouet.collectionholders.interfaces.collections.ObsMapHolder;
import fr.lelouet.collectionholders.interfaces.collections.ObsSetHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsBoolHolder;

public class ObsSetHolderImpl<U> extends AObsCollectionHolder<U, Set<U>>
implements ObsSetHolder<U> {

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

	public ObsSetHolderImpl(Set<U> set) {
		super(set);
	}

	public ObsSetHolderImpl() {
	}

	@Override
	public ObsSetHolderImpl<U> filter(Predicate<? super U> predicate) {
		ObsSetHolderImpl<U> ret = new ObsSetHolderImpl<>();
		follow((t) -> {
			Set<U> filteredSet = t.stream().filter(predicate).collect(Collectors.toSet());
			ret.set(filteredSet);
		});
		return ret;
	}

	@Override
	public ObsSetHolderImpl<U> filterWhen(Function<? super U, ObsBoolHolder> filterer) {
		ObsSetHolderImpl<U> ret = new ObsSetHolderImpl<>();
		filterWhen(filteredStream -> {
			Set<U> filteredSet = filteredStream.collect(Collectors.toSet());
			ret.set(filteredSet);
		}, filterer);
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
		});
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
		});
		value.follow((newValue) -> {
			synchronized (receipt) {
				receipt[1] = true;
				recVal[0] = newValue;
				update.run();
			}
		});
		return ret;
	}

	@Override
	public ObsSetHolder<U> distinct() {
		return this;
	}

}
