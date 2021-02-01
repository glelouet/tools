package fr.lelouet.collectionholders.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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
import fr.lelouet.collectionholders.interfaces.RWObsObjHolder;
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

	@SuppressWarnings("unchecked")
	@Override
	public <V> ObsObjHolder<V> map(Function<U, V> mapper) {
		ObsObjHolderSimple<V> ret = new ObsObjHolderSimple<>();
		follow(v -> ret.set(mapper.apply(v)), ret);
		return ret;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ObsBoolHolder test(Predicate<U> test) {
		ObsBoolHolderImpl ret = new ObsBoolHolderImpl();
		follow((newValue) -> ret.set(test.test(newValue)), ret);
		return ret;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ObsIntHolder mapInt(ToIntFunction<U> mapper) {
		ObsIntHolderImpl ret = new ObsIntHolderImpl();
		follow(newValue -> ret.set(mapper.applyAsInt(newValue)), ret);
		return ret;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ObsLongHolder mapLong(ToLongFunction<U> mapper) {
		ObsLongHolderImpl ret = new ObsLongHolderImpl();
		follow((newValue) -> ret.set(mapper.applyAsLong(newValue)), ret);
		return ret;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ObsFloatHolder mapFloat(ToDoubleFunction<U> mapper) {
		ObsFloatHolderImpl ret = new ObsFloatHolderImpl();
		follow((newValue) -> ret.set((float) mapper.applyAsDouble(newValue)), ret);
		return ret;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ObsDoubleHolder mapDouble(ToDoubleFunction<U> mapper) {
		ObsDoubleHolderImpl ret = new ObsDoubleHolderImpl();
		follow((newValue) -> ret.set(mapper.applyAsDouble(newValue)), ret);
		return ret;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <K> ObsListHolder<K> mapList(Function<U, List<K>> mapper) {
		ObsListHolderImpl<K> ret = new ObsListHolderImpl<>();
		follow((newValue) -> {
			List<K> newlist = mapper.apply(newValue);
			ret.set(newlist);
		}, ret);
		return ret;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <K, V> ObsMapHolder<K, V> mapMap(Function<U, Map<K, V>> mapper) {
		ObsMapHolderImpl<K, V> ret = new ObsMapHolderImpl<>();
		follow((newValue) -> {
			Map<K, V> newlist = mapper.apply(newValue);
			ret.set(newlist);
		}, ret);
		return ret;
	}

	@SuppressWarnings("unchecked")
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

	@SuppressWarnings("unchecked")
	@Override
	public <V> ObsSetHolder<V> toSet(Function<U, Iterable<V>> generator) {
		ObsSetHolderImpl<V> ret = new ObsSetHolderImpl<>();
		follow((newValue) -> {
			Set<V> newlist = StreamSupport.stream(generator.apply(newValue).spliterator(), false).collect(Collectors.toSet());
			ret.set(newlist);
		}, ret);
		return ret;
	}

	//
	// utility generic methods
	//

	/**
	 * reduce two different observable type holder into a third one.
	 *
	 * @param <AType>
	 *          type of the first object hold
	 * @param <BType>
	 *          type of second object hold
	 * @param <ResType>
	 *          joined type
	 * @param <HolderType>
	 *          holder implementation type to hold the joined value
	 * @param a
	 *          first object to listen
	 * @param b
	 *          second object to listen
	 * @param creator
	 *          function to create a holder on the result
	 * @param joiner
	 *          function to be called to join an Atype and a BType into a ResType.
	 * @return a new variable bound to the application of the joiner on a and b.
	 */
	@SuppressWarnings("unchecked")
	public static <AType, BType, ResType, HolderType extends RWObsObjHolder<ResType> & Consumer<Object>> HolderType reduce(
			ObsObjHolder<AType> a, ObsObjHolder<BType> b, Supplier<HolderType> creator,
			BiFunction<AType, BType, ResType> joiner) {
		HolderType ret = creator.get();
		boolean[] receipt = new boolean[] { false, false };
		HashMap<Integer, Object> received = new HashMap<>();
		Runnable update = () -> {
			if (receipt[0] && receipt[1]) {
				ResType joined = joiner.apply((AType) received.get(0), (BType) received.get(1));
				ret.set(joined);
			}
		};
		a.follow(newa -> {
			synchronized (received) {
				received.put(0, newa);
				receipt[0] = true;
				update.run();
			}
		}, ret);
		b.follow(newb -> {
			synchronized (received) {
				received.put(1, newb);
				receipt[1] = true;
				update.run();
			}
		}, ret);
		return ret;
	}

	@Override
	public <V, R> ObsObjHolder<R> with(ObsObjHolder<V> other, BiFunction<U, V, R> mapper) {
		return reduce(this, other, ObsObjHolderSimple::new, mapper);
	}

	/**
	 * map into an obs object with a specific constructor.
	 *
	 * @param <U>
	 *          The type of the object hold
	 * @param <V>
	 *          The type of the object mapped
	 * @param <C>
	 *          The Object holder type on V
	 * @param from
	 *          the original object holder
	 * @param creator
	 *          the function to create a C for a ObservableValue of V. Typically
	 *          the constructor.
	 * @param mapper
	 *          the function to translate a U into a V
	 * @return a new constrained variable.
	 */
	@SuppressWarnings("unchecked")
	public static <U, V, C extends RWObsObjHolder<V> & Consumer<Object>> C map(ObsObjHolder<U> from, Supplier<C> creator,
			Function<U, V> mapper) {
		C ret = creator.get();
		from.follow((newValue) -> {
			ret.set(mapper.apply(newValue));
		}, ret);
		return ret;
	}

	@SuppressWarnings("unchecked")
	public static <V, W> ObsObjHolder<W> unPack(ObsObjHolder<V> target, Function<V, ObsObjHolder<W>> unpacker) {
		ObsObjHolderSimple<W> ret = new ObsObjHolderSimple<>();
		ObsObjHolder<W>[] storeHolder = new ObsObjHolder[1];
		Consumer<W> cons = v -> ret.set(v);
		target.follow(u -> {
			if (storeHolder[0] != null) {
				storeHolder[0].unfollow(cons);
			}
			storeHolder[0] = unpacker.apply(u);
			if (storeHolder[0] != null) {
				storeHolder[0].follow(cons, ret);
			}
		}, ret);
		return ret;
	}

	@Override
	public <V> ObsObjHolder<V> unPack(Function<U, ObsObjHolder<V>> unpacker) {
		return unPack(this, unpacker);
	}


	/**
	 * join several observable object holders of the same type, into another type
	 * holder
	 *
	 */
	@SuppressWarnings("unchecked")
	public static <U, V, HolderType extends RWObsObjHolder<V> & Consumer<Object>> HolderType reduce(
			Supplier<HolderType> creator, Function<List<? extends U>, V> joiner, List<ObsObjHolder<? extends U>> vars) {
		HolderType ret = creator.get();
		if (vars == null || vars.isEmpty()) {
			ret.set(null);
			return ret;
		}
		ObsObjHolder<U>[] holders = vars.toArray(ObsObjHolder[]::new);
		HashMap<Integer, U> received = new HashMap<>();
		for (int i = 0; i < holders.length; i++) {
			int index = i;
			ObsObjHolder<U> h = holders[i];
			h.follow((newValue) -> {
				synchronized (received) {
					received.put(index, newValue);
					if (received.size() == holders.length) {
						V joined = joiner
								.apply(IntStream.range(0, received.size()).mapToObj(received::get).collect(Collectors.toList()));
						ret.set(joined);
					}
				}
			}, ret);
		}
		return ret;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V> ObsObjHolderSimple<V> reduce(Function<List<? extends U>, V> reducer,
			ObsObjHolder<? extends U> first, ObsObjHolder<? extends U>... others) {
		List<ObsObjHolder<? extends U>> list = Stream
				.concat(Stream.of(this, first), others == null ? Stream.empty() : Stream.of(others))
				.collect(Collectors.toList());
		return reduce(ObsObjHolderSimple<V>::new, reducer, list);
	}

}
