package fr.lelouet.collectionholders.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

import fr.lelouet.collectionholders.impl.collections.ObsListHolderImpl;
import fr.lelouet.collectionholders.impl.numbers.ObsBoolHolderImpl;
import fr.lelouet.collectionholders.impl.numbers.ObsDoubleHolderImpl;
import fr.lelouet.collectionholders.impl.numbers.ObsIntHolderImpl;
import fr.lelouet.collectionholders.impl.numbers.ObsLongHolderImpl;
import fr.lelouet.collectionholders.interfaces.ObsObjHolder;
import fr.lelouet.collectionholders.interfaces.RWObsObjHolder;
import fr.lelouet.collectionholders.interfaces.collections.ObsListHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsBoolHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsDoubleHolder;
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
		follow(v -> ret.set(mapper.apply(v)));
		return ret;
	}

	@Override
	public ObsBoolHolder test(Predicate<U> test) {
		ObsBoolHolderImpl ret = new ObsBoolHolderImpl();
		follow((newValue) -> ret.set(test.test(newValue)));
		return ret;
	}

	@Override
	public ObsIntHolder mapInt(ToIntFunction<U> mapper) {
		ObsIntHolderImpl ret = new ObsIntHolderImpl();
		follow(newValue -> {
			ret.set(mapper.applyAsInt(newValue));
		});
		return ret;
	}

	@Override
	public ObsLongHolder mapLong(ToLongFunction<U> mapper) {
		ObsLongHolderImpl ret = new ObsLongHolderImpl();
		follow((newValue) -> ret.set(mapper.applyAsLong(newValue)));
		return ret;
	}

	@Override
	public ObsDoubleHolder mapDouble(ToDoubleFunction<U> mapper) {
		ObsDoubleHolderImpl ret = new ObsDoubleHolderImpl();
		follow((newValue) -> ret.set(mapper.applyAsDouble(newValue)));
		return ret;
	}

	@Override
	public <V> ObsListHolder<V> toList(Function<U, Iterable<V>> generator) {
		ObsListHolderImpl<V> ret = new ObsListHolderImpl<>();
		follow((newValue) -> {
			ret.underlying().clear();
			if (newValue != null) {
				for (V v : generator.apply(newValue)) {
					ret.underlying().add(v);
				}
			}
			ret.dataReceived();
		});
		return ret;
	}

	//
	// utility generic methods
	//

	/**
	 * join two observable object holder into a third one
	 *
	 * @param <AType>
	 *          type of the first object hold
	 * @param <Btype>
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
	public static <AType, Btype, ResType, HolderType extends RWObsObjHolder<ResType>> HolderType join(
			ObsObjHolder<AType> a, ObsObjHolder<Btype> b, Supplier<HolderType> creator,
			BiFunction<AType, Btype, ResType> joiner) {
		HolderType ret = creator.get();
		AType[] ah = (AType[]) new Object[1];
		Btype[] bh = (Btype[]) new Object[1];
		HashSet<Object> received = new HashSet<>();
		Runnable update = () -> {
			if (received.size() == 2) {
				ResType joined = joiner.apply(ah[0], bh[0]);
				ret.set(joined);
			}
		};
		a.follow((newValue) -> {
			synchronized (received) {
				received.add(a);
				ah[0] = newValue;
				update.run();
			}
		});
		b.follow((newValue) -> {
			synchronized (received) {
				received.add(b);
				bh[0] = newValue;
				update.run();
			}
		});
		return ret;
	}

	/**
	 * join two observable object holder into a third one
	 *
	 * @param <AType>
	 *          type of the first object hold
	 * @param <BType>
	 *          type of second object hold
	 * @param <ResType>
	 *          joined type
	 * @param <HolderType>
	 *          holder implementation type to hold the joined value
	 * @param creator
	 *          function to create a holder on the result
	 * @param joiner
	 *          function to be called to join an array of the types. The types are
	 *          objects and must be cast. The array will be
	 * @param holders
	 *          the holders we want to join.
	 * @return a new variable bound to the application of the joiner on a and b.
	 */
	@SafeVarargs
	public static <ResType, HolderType extends RWObsObjHolder<ResType>, JoinerType> HolderType join(
			Supplier<HolderType> creator, Function<List<JoinerType>, ResType> joiner,
			ObsObjHolder<? extends JoinerType>... holders) {
		if (holders == null) {
			return null;
		}
		HolderType ret = creator.get();
		ArrayList<JoinerType> lastReceived = new ArrayList<>(
				IntStream.rangeClosed(1, holders.length).mapToObj(i -> (JoinerType) null).collect(Collectors.toList()));
		HashSet<Object> holderReceived = new HashSet<>();
		for (int i = 0; i < holders.length; i++) {
			int index = i;
			ObsObjHolder<? extends JoinerType> h = holders[i];
			h.follow((newValue) -> {
				synchronized (holderReceived) {
					holderReceived.add(h);
					lastReceived.set(index, newValue);
					if (holderReceived.size() == holders.length) {
						ResType joined = joiner.apply(lastReceived);
						ret.set(joined);
					}
				}
			});
		}
		return ret;
	}

	/**
	 * map an obs object with a specific constructor.
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
	public static <U, V, C extends RWObsObjHolder<V>> C map(ObsObjHolder<U> from, Supplier<C> creator,
			Function<U, V> mapper) {
		C ret = creator.get();
		from.follow((newValue) -> {
			ret.set(mapper.apply(newValue));
		});
		return ret;
	}

	public static <V, W> ObsObjHolder<W> unPack(ObsObjHolder<V> target, Function<V, ObsObjHolder<W>> unpacker) {
		ObsObjHolderSimple<W> ret = new ObsObjHolderSimple<>();
		@SuppressWarnings("unchecked")
		ObsObjHolder<W>[] last = new ObsObjHolder[1];
		Consumer<W> cons = v -> ret.set(v);
		target.follow(u -> {
			if (last[0] != null) {
				last[0].unfollow(cons);
			}
			last[0] = unpacker.apply(u);
			last[0].follow(cons);
		});
		return ret;
	}

	@Override
	public <V> ObsObjHolder<V> unPack(Function<U, ObsObjHolder<V>> unpacker) {
		return unPack(this, unpacker);
	}

	// public static <V, W> ObsCollectionHolder<W, ?, ?> unPackCol(ObsObjHolder<V>
	// target,
	// Function<V, ObsCollectionHolder<W, ?, ?>> unpacker) {
	// ObsListHolderImpl<W> ret = new ObsListHolderImpl<>();
	// @SuppressWarnings("unchecked")
	// ObsCollectionHolder<W, ?, ?>[] last = new ObsCollectionHolder[1];
	// Consumer<Collection<W>> cons = v -> ret.underlying().setAll(v);
	// target.follow(u -> {
	// if (last[0] != null) {
	// last[0].unfollow(cons);
	// }
	// last[0] = unpacker.apply(u);
	// last[0].follow(cons);
	// });
	//
	// return ret;
	// }

}
