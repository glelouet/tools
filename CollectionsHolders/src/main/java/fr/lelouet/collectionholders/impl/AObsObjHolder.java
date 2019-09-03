package fr.lelouet.collectionholders.impl;

import java.util.HashSet;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

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
		ObservableList<V> internal = FXCollections.observableArrayList();
		ObsListHolderImpl<V> ret = new ObsListHolderImpl<>(internal);
		follow((newValue) -> {
			internal.clear();
			if (newValue != null) {
				for (V v : generator.apply(newValue)) {
					internal.add(v);
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
	 * @param <ColType>
	 *          collection type to hold the joined value
	 * @param a
	 * @param b
	 * @param creator
	 * @param joiner
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <AType, Btype, ResType, ColType extends RWObsObjHolder<ResType>> ColType join(ObsObjHolder<AType> a,
			ObsObjHolder<Btype> b, Supplier<ColType> creator, BiFunction<AType, Btype, ResType> joiner) {
		ColType ret = creator.get();
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
	 *          the function to create a C from a ObservableValue of V. Typically
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

}
