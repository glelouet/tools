package fr.lelouet.collectionholders.impl;

import java.util.HashSet;
import java.util.concurrent.CountDownLatch;
import java.util.function.BiFunction;
import java.util.function.Function;

import fr.lelouet.collectionholders.impl.collections.ObsListHolderImpl;
import fr.lelouet.collectionholders.interfaces.ObsObjHolder;
import fr.lelouet.collectionholders.interfaces.collections.ObsListHolder;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ObsObjHolderImpl<U> implements ObsObjHolder<U> {

	protected ObservableValue<U> underlying;

	public ObsObjHolderImpl(ObservableValue<U> underlying) {
		this.underlying = underlying;
		underlying.addListener(this::objchangelisten);
		if (underlying.getValue() != null) {
			objchangelisten(underlying, null, underlying.getValue());
		}
	}

	CountDownLatch waitLatch = new CountDownLatch(1);

	@Override
	public void waitData() {
		try {
			waitLatch.await();
		} catch (InterruptedException e) {
			throw new UnsupportedOperationException("catch this", e);
		}
	}

	protected void objchangelisten(Object o, U old, U now) {
		waitLatch.countDown();
	}

	@Override
	public U get() {
		waitData();
		return underlying.getValue();
	}

	@Override
	public void follow(ChangeListener<U> change) {
		synchronized (underlying) {
			if (waitLatch.getCount() <= 0) {
				change.changed(underlying, null, underlying.getValue());
			}
			underlying.addListener(change);
		}
	}

	@Override
	public ObservableValue<U> asObservable() {
		return underlying;
	}

	@Override
	public void unfollow(ChangeListener<U> change) {
		synchronized (underlying) {
			underlying.removeListener(change);
		}
	}

	@Override
	public <V> ObsObjHolder<V> map(Function<U, V> mapper) {
		SimpleObjectProperty<V> underlying = new SimpleObjectProperty<>();
		ObsObjHolderImpl<V> ret = new ObsObjHolderImpl<>(underlying);
		follow((observable, oldValue, newValue) -> underlying.set(mapper.apply(newValue)));
		return ret;
	}

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

}
