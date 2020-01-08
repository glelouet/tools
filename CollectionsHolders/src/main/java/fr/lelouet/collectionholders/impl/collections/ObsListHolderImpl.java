package fr.lelouet.collectionholders.impl.collections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fr.lelouet.collectionholders.interfaces.collections.ObsListHolder;
import fr.lelouet.collectionholders.interfaces.collections.ObsMapHolder;
import fr.lelouet.collectionholders.interfaces.collections.ObsSetHolder;
import fr.lelouet.tools.synchronization.LockWatchDog;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;

public class ObsListHolderImpl<U> extends
AObsCollectionHolder<U, List<U>, ObservableList<U>, ListChangeListener<? super U>> implements ObsListHolder<U> {

	/**
	 * create an unmodifiable list of items
	 *
	 * @param <U>
	 *          type of the items
	 * @param args
	 *          items to add
	 * @return a new list
	 */
	@SafeVarargs
	public static <U> ObsListHolderImpl<U> of(U... args) {
		ObsListHolderImpl<U> ret = new ObsListHolderImpl<>(FXCollections.observableArrayList(args));
		ret.dataReceived();
		return ret;
	}

	public ObsListHolderImpl(ObservableList<U> underlying) {
		super(underlying);
	}

	@Override
	public List<U> get() {
		waitData();
		return LockWatchDog.BARKER.syncExecute(underlying, () -> {
			return new ArrayList<>(underlying);
		});
	}

	@Override
	public void apply(BiConsumer<Integer, U> cons) {
		waitData();
		LockWatchDog.BARKER.syncExecute(underlying, () -> {
			for (int i = 0; i < underlying.size(); i++) {
				cons.accept(i, underlying.get(i));
			}
		});
	}

	@Override
	public void followItems(ListChangeListener<? super U> listener) {
		LockWatchDog.BARKER.syncExecute(underlying, () -> {
			ObservableList<U> otherlist = FXCollections.observableArrayList();
			otherlist.addListener(listener);
			otherlist.addAll(underlying);
			underlying.addListener(listener);
		});
	}

	@Override
	public void unfollowItems(ListChangeListener<? super U> change) {
		LockWatchDog.BARKER.syncExecute(underlying, () -> {
			underlying.removeListener(change);
		});
	}

	public static <T> ObsListHolderImpl<T> filter(ObsListHolder<T> source, Predicate<? super T> predicate) {
		ObservableList<T> internal = FXCollections.observableArrayList();
		ObsListHolderImpl<T> ret = new ObsListHolderImpl<>(internal);
		source.follow((t) -> {
			List<T> filteredList = t.stream().filter(predicate).collect(Collectors.toList());
			if (!internal.equals(filteredList) || internal.isEmpty()) {
				synchronized (internal) {
					internal.clear();
					internal.addAll(filteredList);
				}
				ret.dataReceived();
			}
		});
		return ret;
	}

	@Override
	public ObsListHolderImpl<U> filter(Predicate<? super U> predicate) {
		return filter(this, predicate);
	}

	private ObsSetHolder<U> distinct = null;

	@Override
	public ObsSetHolder<U> distinct() {
		if (distinct == null) {
			synchronized (this) {
				if (distinct == null) {
					ObservableSet<U> internal = FXCollections.observableSet(new HashSet<>());
					ObsSetHolderImpl<U> ret = new ObsSetHolderImpl<>(internal);
					follow((l) -> {
						boolean modified = false;
						synchronized (internal) {
							modified = internal.retainAll(l);
							modified = internal.addAll(l) ? true : modified;
						}
						if (modified) {
							ret.dataReceived();
						}
					});
					distinct = ret;
				}
			}
		}
		return distinct;
	}

	@Override
	public <K> ObsMapHolder<K, U> toMap(Function<U, K> keyExtractor) {
		return ObsMapHolderImpl.toMap(this, keyExtractor);
	}

	@Override
	public <K, V> ObsMapHolder<K, V> toMap(Function<U, K> keyExtractor, Function<U, V> valExtractor) {
		return ObsMapHolderImpl.toMap(this, keyExtractor, valExtractor);
	}

	private ObsListHolderImpl<U> reverse = null;

	@Override
	public ObsListHolderImpl<U> reverse() {
		if (reverse == null) {
			synchronized (this) {
				if (reverse == null) {
					ObservableList<U> internal = FXCollections.observableArrayList();
					ObsListHolderImpl<U> ret = new ObsListHolderImpl<>(internal);
					follow((o) -> {
						List<U> reverseList = new ArrayList<>(o);
						Collections.reverse(reverseList);
						if (!internal.equals(reverseList)) {
							synchronized (internal) {
								internal.clear();
								internal.addAll(reverseList);
							}
							ret.dataReceived();
						}
					});
					ret.reverse = this;
					reverse = ret;
				}
			}
		}
		return reverse;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ObsListHolder<U> concat(ObsListHolder<? extends U>... lists) {
		if (lists == null || lists.length == 0) {
			return this;
		}
		ObsListHolder<U>[] array = Stream.concat(Stream.of(this), lists == null ? Stream.empty() : Stream.of(lists))
				.filter(m -> m != null).toArray(ObsListHolder[]::new);
		ObservableList<U> internal = FXCollections.observableArrayList();
		ObsListHolderImpl<U> ret = new ObsListHolderImpl<>(internal);
		LinkedHashMap<ObsListHolder<U>, List<U>> alreadyreceived = new LinkedHashMap<>();
		for (ObsListHolder<U> m : array) {
			m.follow(list -> {
				synchronized (alreadyreceived) {
					alreadyreceived.remove(m);
					alreadyreceived.put(m, list);
					if (alreadyreceived.size() == array.length) {
						List<U> newList = alreadyreceived.values().stream().flatMap(m2 -> m2.stream()).collect(Collectors.toList());
						if (!internal.equals(newList) || internal.isEmpty()) {
							synchronized (internal) {
								internal.clear();
								internal.addAll(newList);
							}
							ret.dataReceived();
						}
					}
				}
			});
		}
		return ret;
	}

}