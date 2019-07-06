package fr.lelouet.collectionholders.impl.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.CountDownLatch;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import fr.lelouet.collectionholders.impl.ObsObjHolderImpl;
import fr.lelouet.collectionholders.impl.numbers.ObsBoolHolderImpl;
import fr.lelouet.collectionholders.impl.numbers.ObsDoubleHolderImpl;
import fr.lelouet.collectionholders.impl.numbers.ObsIntHolderImpl;
import fr.lelouet.collectionholders.impl.numbers.ObsLongHolderImpl;
import fr.lelouet.collectionholders.interfaces.ObsObjHolder;
import fr.lelouet.collectionholders.interfaces.collections.ObsCollectionHolder;
import fr.lelouet.collectionholders.interfaces.collections.ObsListHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsBoolHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsDoubleHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsIntHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsLongHolder;
import fr.lelouet.tools.synchronization.LockWatchDog;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public abstract class AObsCollectionHolder<U, C extends Collection<U>, OC extends C, L>
implements ObsCollectionHolder<U, C, L> {

	protected OC underlying;

	public AObsCollectionHolder(OC underlying) {
		this.underlying = underlying;
	}

	CountDownLatch waitLatch = new CountDownLatch(1);

	private ArrayList<ChangeListener<C>> receiveListeners;

	@Override
	public void waitData() {
		try {
			waitLatch.await();
		} catch (InterruptedException e) {
			throw new UnsupportedOperationException("catch this", e);
		}
	}

	@Override
	public void apply(Consumer<U> cons) {
		waitData();
		LockWatchDog.BARKER.syncExecute(underlying, () -> {
			for (U u : underlying) {
				cons.accept(u);
			}
		});
	}

	private ObsIntHolder size = null;

	@Override
	public ObsIntHolder size() {
		if (size == null) {
			synchronized (this) {
				if (size == null) {
					size = mapInt(c -> c.size());
				}
			}
		}
		return size;
	}

	@Override
	public void follow(ChangeListener<C> callback) {
		LockWatchDog.BARKER.syncExecute(underlying, () -> {
			if (receiveListeners == null) {
				receiveListeners = new ArrayList<>();
			}
			receiveListeners.add(callback);
			if (waitLatch.getCount() == 0) {
				callback.changed(null, null, underlying);
			}
		});
	}

	@Override
	public void unfollow(ChangeListener<C> callback) {
		synchronized (underlying) {
			receiveListeners.remove(callback);
		}
	}

	/**
	 * called by the data fetcher when data has been received. This specifies that
	 * the items stored are consistent and can be used as a bulk.
	 */
	@Override
	public void dataReceived() {
		LockWatchDog.BARKER.syncExecute(underlying, () -> {
			waitLatch.countDown();
			if (receiveListeners != null) {
				C consumed = underlying;
				for (ChangeListener<C> r : receiveListeners) {
					r.changed(null, null, consumed);
				}
			}
		});
	}

	@Override
	public ObsListHolder<U> sorted(Comparator<U> comparator) {
		ObservableList<U> internal = FXCollections.observableArrayList();
		ObsListHolderImpl<U> ret = new ObsListHolderImpl<>(internal);
		follow((a, b, o) -> {
			ArrayList<U> modified = new  ArrayList<>(o);
			Collections.sort(modified, comparator);
			internal.clear();
			internal.addAll(modified);
			ret.dataReceived();
		});
		return ret;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V, O> ObsListHolder<O> prodList(ObsCollectionHolder<V, ?, ?> right, BiFunction<U, V, O> operand) {
		ObservableList<O> internal = FXCollections.observableArrayList();
		ObsListHolderImpl<O> ret = new ObsListHolderImpl<>(internal);
		Collection<U>[] leftCol = new Collection[1];
		Collection<V>[] rightCol = new Collection[1];

		Runnable update = () -> {
			if (leftCol[0] != null && rightCol[0] != null) {
				internal.clear();
				for (U u : leftCol[0]) {
					for (V v : rightCol[0]) {
						internal.add(operand.apply(u, v));
					}
				}
				ret.dataReceived();
			}
		};
		follow((a, old, o) -> {
			leftCol[0] = o;
			update.run();
		});
		right.follow((a, b, o) -> {
			rightCol[0] = o;
			update.run();
		});
		return ret;
	}

	@Override
	public <V> ObsObjHolder<V> reduce(Function<C, V> collectionReducer) {
		SimpleObjectProperty<V> internal = new SimpleObjectProperty<>();
		ObsObjHolder<V> ret = new ObsObjHolderImpl<>(internal);
		follow((a, b, l) -> internal.set(collectionReducer.apply(l)));
		return ret;
	}

	@Override
	public ObsIntHolderImpl reduceInt(ToIntFunction<C> collectionReducer) {
		SimpleObjectProperty<Integer> internal = new SimpleObjectProperty<>();
		ObsIntHolderImpl ret = new ObsIntHolderImpl(internal);
		follow((a, b, l) -> internal.set(collectionReducer.applyAsInt(l)));
		return ret;
	}

	@Override
	public ObsDoubleHolder reduceDouble(ToDoubleFunction<C> collectionReducer) {
		SimpleObjectProperty<Double> internal = new SimpleObjectProperty<>();
		ObsDoubleHolderImpl ret = new ObsDoubleHolderImpl(internal);
		follow((a, b, l) -> internal.set(collectionReducer.applyAsDouble(l)));
		return ret;
	}

	@Override
	public ObsLongHolder reduceLong(ToLongFunction<C> collectionReducer) {
		SimpleObjectProperty<Long> internal = new SimpleObjectProperty<>();
		ObsLongHolderImpl ret = new ObsLongHolderImpl(internal);
		follow((a, b, l) -> internal.set(collectionReducer.applyAsLong(l)));
		return ret;
	}

	@Override
	public <V> ObsObjHolder<V> map(Function<C, V> mapper) {
		SimpleObjectProperty<V> internal = new SimpleObjectProperty<>();
		ObsObjHolderImpl<V> ret = new ObsObjHolderImpl<>(internal);
		follow((a, b, l) -> internal.set(mapper.apply(l)));
		return ret;
	}

	@Override
	public ObsBoolHolder test(Predicate<C> test) {
		SimpleObjectProperty<Boolean> underlying = new SimpleObjectProperty<>();
		ObsBoolHolder ret = new ObsBoolHolderImpl(underlying);
		follow((observable, oldValue, newValue) -> underlying.set(test.test(newValue)));
		return ret;
	}

	@Override
	public ObsIntHolder mapInt(ToIntFunction<C> mapper) {
		SimpleObjectProperty<Integer> underlying = new SimpleObjectProperty<>();
		ObsIntHolder ret = new ObsIntHolderImpl(underlying);
		follow((observable, oldValue, newValue) -> underlying.set(mapper.applyAsInt(newValue)));
		return ret;
	}

	@Override
	public ObsLongHolder mapLong(ToLongFunction<C> mapper) {
		SimpleObjectProperty<Long> underlying = new SimpleObjectProperty<>();
		ObsLongHolder ret = new ObsLongHolderImpl(underlying);
		follow((observable, oldValue, newValue) -> underlying.set(mapper.applyAsLong(newValue)));
		return ret;
	}

	@Override
	public ObsDoubleHolder mapDouble(ToDoubleFunction<C> mapper) {
		SimpleObjectProperty<Double> underlying = new SimpleObjectProperty<>();
		ObsDoubleHolder ret = new ObsDoubleHolderImpl(underlying);
		follow((observable, oldValue, newValue) -> underlying.set(mapper.applyAsDouble(newValue)));
		return ret;
	}

	@Override
	public <V> ObsListHolder<V> toList(Function<C, Iterable<V>> generator) {
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
