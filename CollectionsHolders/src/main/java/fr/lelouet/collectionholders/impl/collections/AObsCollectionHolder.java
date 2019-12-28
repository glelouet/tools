package fr.lelouet.collectionholders.impl.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import fr.lelouet.collectionholders.impl.ObsObjHolderSimple;
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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public abstract class AObsCollectionHolder<U, C extends Collection<U>, OC extends C, L>
implements ObsCollectionHolder<U, C, L> {

	protected OC underlying;

	public AObsCollectionHolder(OC underlying) {
		this.underlying = underlying;
	}

	CountDownLatch waitLatch = new CountDownLatch(1);
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

	private ArrayList<Consumer<C>> receiveListeners;

	@Override
	public void follow(Consumer<C> callback) {
		LockWatchDog.BARKER.syncExecute(underlying, () -> {
			if (receiveListeners == null) {
				receiveListeners = new ArrayList<>();
			}
			receiveListeners.add(callback);
			if (waitLatch.getCount() == 0) {
				callback.accept(underlying);
			}
		});
	}

	@Override
	public void unfollow(Consumer<C> callback) {
		synchronized (underlying) {
			receiveListeners.remove(callback);
		}
	}


	/**
	 * called by the data fetcher when data has been received. This specifies that
	 * the items stored are consistent and can be used as a bulk.
	 */
	public void dataReceived() {
		LockWatchDog.BARKER.syncExecute(underlying, () -> {
			waitLatch.countDown();
			if (receiveListeners != null) {
				C consumed = underlying;
				for (Consumer<C> r : receiveListeners) {
					r.accept(consumed);
				}
			}
		});
	}

	@Override
	public <K> ObsListHolderImpl<K> mapItems(Function<U, K> mapper) {
		ObservableList<K> internal = FXCollections.observableArrayList();
		ObsListHolderImpl<K> ret = new ObsListHolderImpl<>(internal);
		follow((o) -> {
			List<K> mappedList = o.stream().map(mapper).collect(Collectors.toList());
			if (!internal.equals(mappedList) || internal.isEmpty()) {
				synchronized (internal) {
					internal.clear();
					internal.addAll(mappedList);
				}
				ret.dataReceived();
			}
		});
		return ret;
	}

	@Override
	public ObsListHolder<U> sorted(Comparator<U> comparator) {
		ObservableList<U> internal = FXCollections.observableArrayList();
		ObsListHolderImpl<U> ret = new ObsListHolderImpl<>(internal);
		follow((o) -> {
			List<U> sortedList = new ArrayList<>(o);
			Collections.sort(sortedList, comparator);
			if (!internal.equals(sortedList) || internal.isEmpty()) {
				synchronized (internal) {
					internal.clear();
					internal.addAll(sortedList);
				}
				ret.dataReceived();
			}
		});
		return ret;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V, O> ObsListHolder<O> prodList(ObsCollectionHolder<V, ?, ?> right, BiFunction<U, V, O> operand) {
		ObservableList<O> internal = FXCollections.observableArrayList();
		ObsListHolderImpl<O> ret = new ObsListHolderImpl<>(internal);
		Collection<U>[] leftCollection = new Collection[1];
		Collection<V>[] rightCollection = new Collection[1];
		Runnable update = () -> {
			if (leftCollection[0] != null && rightCollection[0] != null) {
				List<O> newproduct = leftCollection[0].stream()
						.flatMap(
								leftElem -> rightCollection[0].stream().map(rightElem -> operand.apply(leftElem, rightElem)))
						.collect(Collectors.toList());
				if (!internal.equals(newproduct) || internal.isEmpty()) {
					synchronized (internal) {
						internal.clear();
						internal.addAll(newproduct);
					}
					ret.dataReceived();
				}
			}
		};
		follow((o) -> {
			leftCollection[0] = o;
			update.run();
		});
		right.follow((o) -> {
			rightCollection[0] = o;
			update.run();
		});
		return ret;
	}

	@Override
	public <V> ObsObjHolder<V> reduce(Function<C, V> collectionReducer) {
		ObsObjHolderSimple<V> ret = new ObsObjHolderSimple<>();
		follow((l) -> ret.set(collectionReducer.apply(l)));
		return ret;
	}

	@Override
	public ObsIntHolderImpl reduceInt(ToIntFunction<C> collectionReducer) {
		ObsIntHolderImpl ret = new ObsIntHolderImpl();
		follow((l) -> ret.set(collectionReducer.applyAsInt(l)));
		return ret;
	}

	@Override
	public ObsDoubleHolder reduceDouble(ToDoubleFunction<C> collectionReducer) {
		ObsDoubleHolderImpl ret = new ObsDoubleHolderImpl();
		follow((l) -> ret.set(collectionReducer.applyAsDouble(l)));
		return ret;
	}

	@Override
	public ObsLongHolder reduceLong(ToLongFunction<C> collectionReducer) {
		ObsLongHolderImpl ret = new ObsLongHolderImpl();
		follow((l) -> ret.set(collectionReducer.applyAsLong(l)));
		return ret;
	}

	@Override
	public <V> ObsObjHolder<V> map(Function<C, V> mapper) {
		ObsObjHolderSimple<V> ret = new ObsObjHolderSimple<>();
		follow((l) -> ret.set(mapper.apply(l)));
		return ret;
	}

	@Override
	public ObsBoolHolder test(Predicate<C> test) {
		ObsBoolHolderImpl ret = new ObsBoolHolderImpl();
		follow((newValue) -> ret.set(test.test(newValue)));
		return ret;
	}

	@Override
	public ObsIntHolder mapInt(ToIntFunction<C> mapper) {
		ObsIntHolderImpl ret = new ObsIntHolderImpl();
		follow((newValue) -> ret.set(mapper.applyAsInt(newValue)));
		return ret;
	}

	@Override
	public ObsLongHolder mapLong(ToLongFunction<C> mapper) {
		ObsLongHolderImpl ret = new ObsLongHolderImpl();
		follow((newValue) -> ret.set(mapper.applyAsLong(newValue)));
		return ret;
	}

	@Override
	public ObsDoubleHolder mapDouble(ToDoubleFunction<C> mapper) {
		ObsDoubleHolderImpl ret = new ObsDoubleHolderImpl();
		follow((newValue) -> ret.set(mapper.applyAsDouble(newValue)));
		return ret;
	}

	@Override
	public <V> ObsListHolder<V> toList(Function<C, Iterable<V>> generator) {
		ObservableList<V> internal = FXCollections.observableArrayList();
		ObsListHolderImpl<V> ret = new ObsListHolderImpl<>(internal);
		follow((newValue) -> {
			List<V> newlist = StreamSupport.stream(generator.apply(newValue).spliterator(), false)
					.collect(Collectors.toList());
			if (!internal.equals(newlist) || internal.isEmpty()) {
				synchronized (internal) {
					internal.clear();
					internal.addAll(newlist);
				}
				ret.dataReceived();
			}
		});
		return ret;
	}

	/**
	 * NOT DONE YET<br />
	 * flatten a collection of collections of V in a collection of V.
	 *
	 * @param <V>
	 *          type of the items hold in the sub collections
	 * @param holders
	 *          the collections holder
	 * @return a new collectionholder that contains the items holds in the sub
	 *         collections.
	 */
	public static <V> ObsCollectionHolder<V, ?, ?> flatten(
			ObsCollectionHolder<ObsCollectionHolder<V, ?, ?>, ?, ?> holders) {
		// TODO
		return null;

	}

}
