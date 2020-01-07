package fr.lelouet.collectionholders.impl.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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

/**
 *
 *
 * @param <U>
 *          The type of object hold inside
 * @param <C>
 *          The type of collection, eg list
 * @param <OC>
 *          the type of observable collection
 * @param <L>
 *          internal observer
 */
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
						.flatMap(leftElem -> rightCollection[0].stream().map(rightElem -> operand.apply(leftElem, rightElem)))
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

	@Override
	public <V, C2 extends Collection<V>> ObsListHolder<V> flatten(Function<U, ObsCollectionHolder<V, C2, ?>> mapper) {
		ObservableList<V> underlying = FXCollections.observableArrayList();
		ObsListHolderImpl<V> ret = new ObsListHolderImpl<>(underlying);

		HashMap<U, ObsCollectionHolder<V, C2, ?>> mappedvalues = new LinkedHashMap<>();
		HashMap<ObsCollectionHolder<V, C2, ?>, Consumer<C2>> listeners = new LinkedHashMap<>();
		HashMap<ObsCollectionHolder<V, C2, ?>, Collection<V>> knownCollections = new LinkedHashMap<>();

		/**
		 * try to merge the known collections, if they are all we need .Synced over
		 * mappedValues, so not to be called within another sync
		 */
		Runnable tryUpdate = () -> {
			synchronized (mappedvalues) {
				if (new HashSet<>(mappedvalues.values()).equals(knownCollections.keySet())) {
					List<V> newlist = mappedvalues.values().stream().flatMap(coll -> knownCollections.get(coll).stream())
							.collect(Collectors.toList());
					if (!newlist.equals(underlying) || underlying.isEmpty()) {
						synchronized (underlying) {
							underlying.setAll(newlist);
						}
						ret.dataReceived();
					}
				} else {
				}
			}
		};

		follow(c -> {
			HashSet<U> toRemove, toAdd;
			synchronized (mappedvalues) {
				if (mappedvalues.keySet().equals(c)) {
					return;
				}
				toRemove = new LinkedHashSet<>(mappedvalues.keySet());
				toRemove.removeAll(c);
				toAdd = new LinkedHashSet<>(c);
				toAdd.removeAll(mappedvalues.keySet());
			}
			for (U u : toRemove) {
				synchronized (mappedvalues) {
					ObsCollectionHolder<V, C2, ?> converted = mappedvalues.remove(u);
					Consumer<C2> listener = listeners.remove(converted);
					converted.unfollow(listener);
					knownCollections.remove(converted);
				}
			}
			for (U u : toAdd) {
				ObsCollectionHolder<V, C2, ?> converted = mapper.apply(u);
				Consumer<C2> listener = c2 -> {
					synchronized (mappedvalues) {
						if (listeners.containsKey(converted)) {
							knownCollections.put(converted, c2);
						}
						else {
						}
					}
					tryUpdate.run();
				};
				synchronized (mappedvalues) {
					mappedvalues.put(u, converted);
					listeners.put(converted, listener);
				}
				// must be called outside of the
				converted.follow(listener);
			}
			tryUpdate.run();
		});

		return ret;

	}

}
