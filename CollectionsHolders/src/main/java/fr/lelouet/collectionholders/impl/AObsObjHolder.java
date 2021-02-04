package fr.lelouet.collectionholders.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import fr.lelouet.collectionholders.impl.collections.ObsListHolderImpl;
import fr.lelouet.collectionholders.impl.collections.ObsMapHolderImpl;
import fr.lelouet.collectionholders.impl.collections.ObsSetHolderImpl;
import fr.lelouet.collectionholders.impl.numbers.ObsBoolHolderImpl;
import fr.lelouet.collectionholders.impl.numbers.ObsDoubleHolderImpl;
import fr.lelouet.collectionholders.impl.numbers.ObsFloatHolderImpl;
import fr.lelouet.collectionholders.impl.numbers.ObsIntHolderImpl;
import fr.lelouet.collectionholders.impl.numbers.ObsLongHolderImpl;
import fr.lelouet.collectionholders.interfaces.ObsObjHolder;
import fr.lelouet.collectionholders.interfaces.collections.ObsListHolder;
import fr.lelouet.collectionholders.interfaces.collections.ObsMapHolder;
import fr.lelouet.collectionholders.interfaces.collections.ObsSetHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsBoolHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsDoubleHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsFloatHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsIntHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsLongHolder;

/**
 * basic abstract methods that do not depend on the implementation.
 *
 * @param <U>
 */
public abstract class AObsObjHolder<U> implements ObsObjHolder<U> {

	@Override
	public <V> ObsObjHolder<V> map(Function<U, V> mapper) {
		ObsObjHolderSimple<V> ret = new ObsObjHolderSimple<>();
		follow(v -> ret.set(mapper.apply(v)), ret);
		return ret;
	}

	@Override
	public ObsBoolHolder test(Predicate<U> test) {
		ObsBoolHolderImpl ret = new ObsBoolHolderImpl();
		follow((newValue) -> ret.set(test.test(newValue)), ret);
		return ret;
	}

	@Override
	public ObsIntHolder mapInt(ToIntFunction<U> mapper) {
		ObsIntHolderImpl ret = new ObsIntHolderImpl();
		follow(newValue -> ret.set(mapper.applyAsInt(newValue)), ret);
		return ret;
	}

	@Override
	public ObsLongHolder mapLong(ToLongFunction<U> mapper) {
		ObsLongHolderImpl ret = new ObsLongHolderImpl();
		follow((newValue) -> ret.set(mapper.applyAsLong(newValue)), ret);
		return ret;
	}

	@Override
	public ObsFloatHolder mapFloat(ToDoubleFunction<U> mapper) {
		ObsFloatHolderImpl ret = new ObsFloatHolderImpl();
		follow((newValue) -> ret.set((float) mapper.applyAsDouble(newValue)), ret);
		return ret;
	}

	@Override
	public ObsDoubleHolder mapDouble(ToDoubleFunction<U> mapper) {
		ObsDoubleHolderImpl ret = new ObsDoubleHolderImpl();
		follow((newValue) -> ret.set(mapper.applyAsDouble(newValue)), ret);
		return ret;
	}

	@Override
	public <K> ObsListHolder<K> mapList(Function<U, List<K>> mapper) {
		ObsListHolderImpl<K> ret = new ObsListHolderImpl<>();
		follow((newValue) -> {
			List<K> newlist = mapper.apply(newValue);
			ret.set(newlist);
		}, ret);
		return ret;
	}

	@Override
	public <K, V> ObsMapHolder<K, V> mapMap(Function<U, Map<K, V>> mapper) {
		ObsMapHolderImpl<K, V> ret = new ObsMapHolderImpl<>();
		follow((newValue) -> {
			Map<K, V> newlist = mapper.apply(newValue);
			ret.set(newlist);
		}, ret);
		return ret;
	}

	@Override
	public <V> ObsListHolder<V> toList(Function<U, Iterable<V>> generator) {
		ObsListHolderImpl<V> ret = new ObsListHolderImpl<>();
		follow((newValue) -> {
			List<V> newlist = StreamSupport.stream(generator.apply(newValue).spliterator(), false)
					.collect(Collectors.toList());
			ret.set(newlist);
		}, ret);
		return ret;
	}

	@Override
	public <V> ObsSetHolder<V> toSet(Function<U, Iterable<V>> generator) {
		ObsSetHolderImpl<V> ret = new ObsSetHolderImpl<>();
		follow((newValue) -> {
			Set<V> newlist = StreamSupport.stream(generator.apply(newValue).spliterator(), false).collect(Collectors.toSet());
			ret.set(newlist);
		}, ret);
		return ret;
	}

	@Override
	public <V, R> ObsObjHolder<R> combine(ObsObjHolder<V> other, BiFunction<U, V, R> mapper) {
		return ObsObjHolder.combine(this, other, ObsObjHolderSimple::new, mapper);
	}

	@Override
	public <V> ObsObjHolder<V> unPack(Function<U, ObsObjHolder<V>> unpacker) {
		return ObsObjHolder.unPack(this, ObsObjHolderSimple::new, unpacker);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V> ObsObjHolderSimple<V> reduce(Function<List<? extends U>, V> reducer,
			ObsObjHolder<? extends U> first, ObsObjHolder<? extends U>... others) {
		List<ObsObjHolder<? extends U>> list = Stream
				.concat(Stream.of(this, first), others == null ? Stream.empty() : Stream.of(others))
				.collect(Collectors.toList());
		return ObsObjHolder.reduce(list, ObsObjHolderSimple<V>::new, reducer);
	}

}
