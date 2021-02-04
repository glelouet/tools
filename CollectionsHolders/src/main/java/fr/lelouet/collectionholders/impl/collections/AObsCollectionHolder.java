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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.lelouet.collectionholders.impl.ObsObjHolderSimple;
import fr.lelouet.collectionholders.interfaces.ObsObjHolder;
import fr.lelouet.collectionholders.interfaces.collections.ObsCollectionHolder;
import fr.lelouet.collectionholders.interfaces.collections.ObsListHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsBoolHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsIntHolder;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 *
 * @param <U>
 *          The type of object hold inside
 * @param <C>
 *          The type of collection, eg list
 */
public abstract class AObsCollectionHolder<U, C extends Collection<U>> extends ObsObjHolderSimple<C>
implements ObsCollectionHolder<U, C> {

	private static final Logger logger = LoggerFactory.getLogger(AObsCollectionHolder.class);

	public AObsCollectionHolder(C item) {
		super(item);
	}

	public AObsCollectionHolder() {
	}

	/** change the collection hold with an empty one. */
	public abstract void setEmpty();

	@Getter(lazy = true)
	@Accessors(fluent = true)
	private final ObsIntHolder size = mapInt(Collection::size);

	@Getter(lazy = true)
	@Accessors(fluent = true)
	private final ObsBoolHolder isEmpty = test(Collection::isEmpty);

	@Override
	public <K> ObsListHolderImpl<K> mapItems(Function<U, K> mapper) {
		ObsListHolderImpl<K> ret = new ObsListHolderImpl<>();
		follow((o) -> {
			List<K> mappedList = o.stream().map(mapper).collect(Collectors.toList());
			ret.set(mappedList);
		}, ret);
		return ret;
	}

	@Override
	public ObsListHolder<U> sorted(Comparator<U> comparator) {
		ObsListHolderImpl<U> ret = new ObsListHolderImpl<>();
		follow((o) -> {
			List<U> sortedList = new ArrayList<>(o);
			Collections.sort(sortedList, comparator);
			ret.set(sortedList);
		}, ret);
		return ret;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V, O> ObsListHolder<O> prodList(ObsCollectionHolder<V, ?> right, BiFunction<U, V, O> operand) {
		ObsListHolderImpl<O> ret = new ObsListHolderImpl<>();
		Collection<U>[] leftCollection = new Collection[1];
		Collection<V>[] rightCollection = new Collection[1];
		Runnable update = () -> {
			if (leftCollection[0] != null && rightCollection[0] != null) {
				List<O> newproduct = leftCollection[0].stream()
						.flatMap(leftElem -> rightCollection[0].stream().map(rightElem -> operand.apply(leftElem, rightElem)))
						.collect(Collectors.toList());
				ret.set(newproduct);
			}
		};
		follow((lo) -> {
			leftCollection[0] = lo;
			update.run();
		}, ret);
		right.follow((ro) -> {
			rightCollection[0] = ro;
			update.run();
		}, ret);
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

		private ObsCollectionHolder<V, C2> observed = null;

		private C2 lastReceived = null;

		private boolean received = false;

		private Consumer<C2> listener;

		private Runnable updater;

		private String debug = null;

		public ObsFlattenData(ObsCollectionHolder<V, C2> observed, Runnable updater, String debug) {
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
	public <V, C2 extends Collection<V>> ObsListHolder<V> flatten(Function<U, ObsCollectionHolder<V, C2>> mapper) {
		return flatten(mapper, null);
	}

	public <V, C2 extends Collection<V>> ObsListHolder<V> flatten(Function<U, ObsCollectionHolder<V, C2>> mapper,
			String debuger) {
		ObsListHolderImpl<V> ret = new ObsListHolderImpl<>();

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
					ret.set(newlist);
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
				ObsCollectionHolder<V, C2> converted = mapper.apply(u);
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
		}, ret);
		return ret;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <K> ObsCollectionHolder<K, ?> unpackItems(Function<U, ObsObjHolder<K>> mapper) {
		ObsListHolderImpl<K> ret = new ObsListHolderImpl<>();
		Object lock = new Object();
		List<ObsObjHolder<K>> holders = new ArrayList<>();
		List<Consumer<K>> followers = new ArrayList<>();
		List<Boolean> receipt = new ArrayList<>();
		List<K> received = new ArrayList<>();
		Runnable update = () -> {
			synchronized (lock) {
				for (boolean b : receipt) {
					if (!b) {
						return;
					}
				}
				ret.set(new ArrayList<>(received));
			}
		};
		follow(l -> {
			ObsObjHolder<K>[] mapped = l.stream().map(mapper).toArray(ObsObjHolder[]::new);
			synchronized (lock) {
				for (int i = 0; i < holders.size(); i++) {
					holders.get(i).unfollow(followers.get(i));
				}
				holders.clear();
				receipt.clear();
				followers.clear();
				received.clear();
				for (int i = 0; i < mapped.length; i++) {
					ObsObjHolder<K> h = mapped[i];
					receipt.add(false);
					holders.add(h);
					received.add(null);
					int fj = i;
					Consumer<K> c = k -> {
						synchronized (lock) {
							receipt.set(fj, true);
							received.set(fj, k);
						}
						update.run();
					};
					followers.add(c);
				}
			}
			for (int i = 0; i < mapped.length; i++) {
				holders.get(i).follow(followers.get(i), ret);
			}
		});
		return ret;
	}

	/**
	 * filter and applies the values
	 *
	 * @param onNewValue
	 *          handler of the values that are accepted
	 * @param filterer
	 *          the function to follow the elements
	 */
	protected void filterWhen(Consumer<Stream<U>> onNewValue, Function<? super U, ObsBoolHolder> filterer,
			Consumer<Object> holder) {
		Map<U, ObsBoolHolder> filters = new LinkedHashMap<>();
		Map<U, Boolean> elementsPredicate = new LinkedHashMap<>();
		Map<U, Consumer<Boolean>> listeners = new HashMap<>();
		Runnable update = () -> {
			synchronized (elementsPredicate) {
				if (elementsPredicate.size() == filters.size()) {
					Stream<U> filteredStream = elementsPredicate.entrySet().stream().filter(e -> e.getValue())
							.map(e -> e.getKey());
					onNewValue.accept(filteredStream);
				}
			}
		};
		follow(c -> {
			synchronized (filters) {
				// first add the elements that are to be added
				for (U u : c) {
					if (!filters.containsKey(u)) {
						ObsBoolHolder predicate = filterer.apply(u);
						filters.put(u, predicate);
						Consumer<Boolean> cons = b -> {
							elementsPredicate.put(u, b);
							update.run();
						};
						listeners.put(u, cons);
						predicate.follow(cons, holder);
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
		}, holder);
	}

	@Override
	public int hashCode() {
		return isDataAvailable() ? 0 : get().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj.getClass() == this.getClass()) {
			AObsCollectionHolder<?, ?> other = (AObsCollectionHolder<?, ?>) obj;
			// equals if same status of data received AND same data received, if
			// received.
			return !isDataAvailable() && !other.isDataAvailable()
					|| isDataAvailable() && other.isDataAvailable() && get().equals(other.get());
		}
		return false;
	}

}
