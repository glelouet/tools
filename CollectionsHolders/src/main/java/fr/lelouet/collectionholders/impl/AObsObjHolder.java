package fr.lelouet.collectionholders.impl;

import java.util.HashSet;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import fr.lelouet.collectionholders.impl.collections.ObsListHolderImpl;
import fr.lelouet.collectionholders.impl.numbers.ObsBoolHolderImpl;
import fr.lelouet.collectionholders.impl.numbers.ObsDoubleHolderImpl;
import fr.lelouet.collectionholders.impl.numbers.ObsIntHolderImpl;
import fr.lelouet.collectionholders.impl.numbers.ObsLongHolderImpl;
import fr.lelouet.collectionholders.interfaces.ObsObjHolder;
import fr.lelouet.collectionholders.interfaces.collections.ObsListHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsBoolHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsDoubleHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsIntHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsLongHolder;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public abstract class AObsObjHolder<U> implements ObsObjHolder<U> {

	@Override
	public <V> ObsObjHolder<V> map(Function<U, V> mapper) {
		ObsObjHolderSimple<V> ret = new ObsObjHolderSimple<>();
		follow(v -> ret.set(mapper.apply(v)));
		return ret;
	}

	@Override
	public ObsBoolHolder test(Predicate<U> test) {
		SimpleObjectProperty<Boolean> underlying = new SimpleObjectProperty<>();
		ObsBoolHolder ret = new ObsBoolHolderImpl(underlying);
		follow((observable, oldValue, newValue) -> underlying.set(test.test(newValue)));
		return ret;
	}

	@Override
	public ObsIntHolder mapInt(ToIntFunction<U> mapper) {
		SimpleObjectProperty<Integer> underlying = new SimpleObjectProperty<>();
		ObsIntHolder ret = new ObsIntHolderImpl(underlying);
		follow((observable, oldValue, newValue) -> underlying.set(mapper.applyAsInt(newValue)));
		return ret;
	}

	@Override
	public ObsLongHolder mapLong(ToLongFunction<U> mapper) {
		SimpleObjectProperty<Long> underlying = new SimpleObjectProperty<>();
		ObsLongHolder ret = new ObsLongHolderImpl(underlying);
		follow((observable, oldValue, newValue) -> underlying.set(mapper.applyAsLong(newValue)));
		return ret;
	}

	@Override
	public ObsDoubleHolder mapDouble(ToDoubleFunction<U> mapper) {
		SimpleObjectProperty<Double> underlying = new SimpleObjectProperty<>();
		ObsDoubleHolder ret = new ObsDoubleHolderImpl(underlying);
		follow((observable, oldValue, newValue) -> underlying.set(mapper.applyAsDouble(newValue)));
		return ret;
	}

	@Override
	public <V> ObsListHolder<V> toList(Function<U, Iterable<V>> generator) {
		ObservableList<V> internal = FXCollections.observableArrayList();
		ObsListHolderImpl<V> ret = new ObsListHolderImpl<>(internal);
		follow((observable, oldValue, newValue) -> {
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
	 * @param <U>
	 *          type of the first object hold
	 * @param <V>
	 *          type of second object hold
	 * @param <W>
	 *          joined type
	 * @param <C>
	 *          collection type to hold the joined value
	 * @param a
	 * @param b
	 * @param creator
	 * @param joiner
	 * @return
	 */
	public static <U, V, W, C extends ObsObjHolder<W>> C join(ObsObjHolder<U> a, ObsObjHolder<V> b,
			Function<ObservableValue<W>, C> creator, BiFunction<U, V, W> joiner) {
		SimpleObjectProperty<W> internal = new SimpleObjectProperty<>();
		C ret = creator.apply(internal);
		HashSet<Object> received = new HashSet<>();
		Runnable update = () -> {
			if (received.size() == 2) {
				internal.set(joiner.apply(a.get(), b.get()));
			}
		};
		a.follow((observable, oldValue, newValue) -> {
			received.add(a);
			update.run();
		});
		b.follow((observable, oldValue, newValue) -> {
			received.add(b);
			update.run();
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
	public static <U, V, C extends ObsObjHolder<V>> C map(ObsObjHolder<U> from, Function<ObservableValue<V>, C> creator,
			Function<U, V> mapper) {
		SimpleObjectProperty<V> internal = new SimpleObjectProperty<>();
		C ret = creator.apply(internal);
		from.follow((observable, oldValue, newValue) -> {
			internal.set(mapper.apply(newValue));
		});
		return ret;
	}

}
