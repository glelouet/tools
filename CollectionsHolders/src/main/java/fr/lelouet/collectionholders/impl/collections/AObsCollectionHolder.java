package fr.lelouet.collectionholders.impl.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.lelouet.collectionholders.impl.AObsObjHolder;
import fr.lelouet.collectionholders.impl.ObsObjHolderSimple;
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
public abstract class AObsCollectionHolder<U, C extends Collection<U>, OC extends C, L> extends ObsObjHolderSimple<C>
implements ObsCollectionHolder<U, C, L> {

	private static final Logger logger = LoggerFactory.getLogger(AObsCollectionHolder.class);

	private final OC underlying;

	public OC underlying() {
		return underlying;
	}

	public AObsCollectionHolder(OC underlying) {
		item = this.underlying = underlying;
	}

	@Override
	public synchronized void set(C newlist) {
		// if there was already a value set, and we set to this same value, don't
		// propagate.
		underlying().clear();
		underlying().addAll(newlist);
		synchronized (underlying) {
			transmitToListeners();
		}
		dataReceivedLatch.countDown();

	}

	@Override
	public void apply(Consumer<U> cons) {
		waitData();
		LockWatchDog.BARKER.syncExecute(underlying(), () -> {
			for (U u : underlying()) {
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

	private ObsBoolHolder isEmpty = null;

	@Override
	public ObsBoolHolder isEmpty() {
		if (isEmpty == null) {
			ObsIntHolder msize = size();
			synchronized (this) {
				if (isEmpty == null) {
					isEmpty = msize.eq(0);
				}
			}
		}
		return isEmpty;
	}

	/**
	 * called by the data fetcher when data has been received. This specifies that
	 * the items stored are consistent and can be used as a bulk.
	 */
	public void dataReceived() {
		LockWatchDog.BARKER.syncExecute(this, () -> {
			dataReceivedLatch.countDown();
			transmitToListeners();
		});
	}

	@Override
	public <K> ObsListHolderImpl<K> mapItems(Function<U, K> mapper) {
		ObservableList<K> internal = FXCollections.observableArrayList();
		ObsListHolderImpl<K> ret = new ObsListHolderImpl<>(internal);
		follow((o) -> {
			List<K> mappedList = o.stream().map(mapper).collect(Collectors.toList());
			synchronized (internal) {
				internal.clear();
				internal.addAll(mappedList);
			}
			ret.dataReceived();
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
			synchronized (internal) {
				internal.clear();
				internal.addAll(sortedList);
			}
			ret.dataReceived();
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
				synchronized (internal) {
					internal.clear();
					internal.addAll(newproduct);
				}
				ret.dataReceived();
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

	/**
	 * Keep data about a converted element of a obsevableCollection that is
	 * flattened.<br />
	 * More precisely, keeps the converted collection, the last received data, the
	 * internal listeners added.<br />
	 * Provides {@link #received() a method} to test if the data has been received
	 * yet, and {@link #last() another one} to get that data. There are two
	 * methods because "null" may be a possible data. <br />
	 * Also provides {@link #addListener() a method} to add a runnable as a
	 * listener whenever data is received. This method should be called out of
	 * sync block if the runnable uses a sync block (the listener can be called
	 * inside the add). {@link #removeListener() Another method} removes that
	 * listeners, for when the converted element is removed from the flattened
	 * collection.
	 *
	 * @param <V>
	 *          converted type
	 * @param <C2>
	 *          converted collection type.
	 */
	private class ObsFlattenData<V, C2 extends Collection<V>> {

		private ObsCollectionHolder<V, C2, ?> observed = null;

		private C2 lastReceived = null;

		private boolean received = false;

		private Consumer<C2> listener;

		private Runnable updater;

		private String debug = null;

		public ObsFlattenData(ObsCollectionHolder<V, C2, ?> observed, Runnable updater, String debug) {
			this.observed = observed;
			this.updater = updater;
			this.debug = debug;
			listener = this::onDataReceived;

		}

		protected void onDataReceived(C2 newCol) {
			received = true;
			if (debug != null) {
				logger.debug(debug + " flatten partial received new collection " + newCol + " had "
						+ (lastReceived == null ? "null" : lastReceived));
			}
			lastReceived = newCol;
			updater.run();
		}

		/**
		 * must be called AFTER the storing, so out of constructor otherwise may be
		 * called within a sync and thus create deadlock
		 */
		public void addListener() {
			observed.follow(listener);
		}

		public void removeListener() {
			observed.unfollow(listener);
		}

		public boolean received() {
			return received;
		}

		public C2 last() {
			return lastReceived;
		}

	}

	@Override
	public <V, C2 extends Collection<V>> ObsListHolder<V> flatten(Function<U, ObsCollectionHolder<V, C2, ?>> mapper) {
		return flatten(mapper, null);
	}

	public <V, C2 extends Collection<V>> ObsListHolder<V> flatten(Function<U, ObsCollectionHolder<V, C2, ?>> mapper,
			String debuger) {
		ObservableList<V> underlying = FXCollections.observableArrayList();
		ObsListHolderImpl<V> ret = new ObsListHolderImpl<>(underlying);

		/**
		 * for each item of the collection, the known corresponding obsmapholder we
		 * are listening <br />
		 * We use identity hashmap because if the keys are collections, that can
		 * lead to issues.
		 */
		IdentityHashMap<U, ObsFlattenData<V, C2>> mappedvalues = new IdentityHashMap<>();
		ArrayList<U> order = new ArrayList<>();

		/**
		 * try to merge the known collections, if they are all we need .Synced over
		 * mappedValues, so not to be called within another sync
		 */
		Runnable tryUpdate = () -> {
			synchronized (mappedvalues) {
				if (mappedvalues.values().stream().filter(fl -> !fl.received()).findAny().isEmpty()) {
					// all the mapped values hold data
					List<V> newlist = order.stream().flatMap(key -> mappedvalues.get(key).last().stream())
							.collect(Collectors.toList());
					if (debuger != null) {
						logger.debug(debuger + " flatten got all collections, propagating data " + newlist);
					}
					synchronized (underlying) {
						underlying.setAll(newlist);
					}
					ret.dataReceived();
				} else {
					// some data did not produce a collection yet.
					if (debuger != null) {
						logger.debug(debuger + " flatten missing collections reception");
					}
				}
			}
		};

		follow(c -> {
			Collection<U> toRemove, toAdd;
			synchronized (mappedvalues) {
				if (mappedvalues.keySet().equals(c)) {
					return;
				}
				toRemove = new ArrayList<>(mappedvalues.keySet());
				toRemove.removeAll(c);
				toAdd = new ArrayList<>(c);
				toAdd.removeAll(mappedvalues.keySet());
			}
			for (U u : toRemove) {
				synchronized (mappedvalues) {
					ObsFlattenData<V, C2> holder = mappedvalues.remove(u);
					holder.removeListener();
					for (Iterator<U> it = order.iterator(); it.hasNext();) {
						if (it.next() == u) {
							it.remove();
						}
					}
				}
			}
			for (U u : toAdd) {
				ObsCollectionHolder<V, C2, ?> converted = mapper.apply(u);
				ObsFlattenData<V, C2> holder = new ObsFlattenData<>(converted, tryUpdate, debuger);
				synchronized (mappedvalues) {
					mappedvalues.put(u, holder);
					for (Iterator<U> it = order.iterator(); it.hasNext();) {
						if (it.next() == u) {
							it.remove();
						}
					}
					order.add(u);
				}
				holder.addListener();
			}
			tryUpdate.run();
		});
		return ret;
	}

	protected void filterWhen(Consumer<Stream<U>> onNewValue, Function<? super U, ObsBoolHolder> filterer) {
		Map<U, ObsBoolHolder> filters = new LinkedHashMap<>();
		Map<U, Boolean> elementsPredicate = new LinkedHashMap<>();
		Map<U, Consumer<Boolean>> listeners = new HashMap<>();
		Runnable update = () -> {
			synchronized (elementsPredicate) {
				// System.err.println("run update predicates=" + elementsPredicate);
				if (elementsPredicate.size() == filters.size()) {
					Stream<U> filteredStream = elementsPredicate.entrySet().stream().filter(e -> e.getValue())
							.map(e -> e.getKey());
					onNewValue.accept(filteredStream);
				}
			}
		};
		follow(c -> {
			// System.err.println("filter got new collection");
			synchronized (filters) {
				// first add the elements that are to be added
				for (U u : c) {
					if (!filters.containsKey(u)) {
						ObsBoolHolder predicate = filterer.apply(u);
						filters.put(u, predicate);
						Consumer<Boolean> cons = b -> {
							// System.err.println("got new predicate value " + b + " for " +
							// u);
							elementsPredicate.put(u, b);
							update.run();
						};
						listeners.put(u, cons);
						predicate.follow(cons);
					}
				}
				// then remove the elements that need to be removed.
				List<U> removed = filters.keySet().stream().filter(u -> !c.contains(u)).collect(Collectors.toList());
				if (!removed.isEmpty()) {
					synchronized (elementsPredicate) {
						for (U u : removed) {
							ObsBoolHolder filter = filters.remove(u);
							filter.unfollow(listeners.get(u));
							filters.remove(u);
							elementsPredicate.remove(u);
						}
					}
					update.run();
				}
				if (c.isEmpty()) {
					update.run();
				}
			}
		});
	}

	@Override
	public int hashCode() {
		return dataReceivedLatch.getCount() == 0 ? 0 : underlying().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj.getClass() == this.getClass()) {
			AObsCollectionHolder<?, ?, ?, ?> other = (AObsCollectionHolder<?, ?, ?, ?>) obj;
			// equals if same status of data received AND same data received, if
			// received.
			return dataReceivedLatch.getCount() != 0 && other.dataReceivedLatch.getCount() != 0
					|| dataReceivedLatch.getCount() == 0 && other.dataReceivedLatch.getCount() == 0
					&& underlying().equals(other.underlying());
		}
		return false;
	}

	@Override
	public <T> ObsObjHolder<T> unPack(Function<C, ObsObjHolder<T>> unpacker) {
		return AObsObjHolder.unPack(this, unpacker);
	}

}
