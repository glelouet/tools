package fr.lelouet.collectionholders.impl.collections;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import fr.lelouet.collectionholders.interfaces.collections.ObsListHolder;
import fr.lelouet.collectionholders.interfaces.collections.ObsMapHolder;
import fr.lelouet.collectionholders.interfaces.collections.ObsSetHolder;
import fr.lelouet.tools.synchronization.LockWatchDog;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;

public class ObsListHolderImpl<U> extends AObsCollectionHolder<U, List<U>, ObservableList<U>, ListChangeListener<? super U>>
implements ObsListHolder<U> {

	public ObsListHolderImpl(ObservableList<U> underlying) {
		super(underlying);
	}

	@Override
	public List<U> copy() {
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
	public void follow(ListChangeListener<? super U> listener) {
		LockWatchDog.BARKER.syncExecute(underlying, () -> {
			ObservableList<U> otherlist = FXCollections.observableArrayList();
			otherlist.addListener(listener);
			otherlist.addAll(underlying);
			underlying.addListener(listener);
		});
	}

	@Override
	public Observable asObservable() {
		return underlying;
	}

	@Override
	public void unfollow(ListChangeListener<? super U> change) {
		LockWatchDog.BARKER.syncExecute(underlying, () -> {
			underlying.removeListener(change);
		});
	}

	public static <T> ObsListHolderImpl<T> filter(ObsListHolder<T> source, Predicate<? super T> predicate) {
		ObservableList<T> internal = FXCollections.observableArrayList();
		ObsListHolderImpl<T> ret = new ObsListHolderImpl<>(internal);
		source.addReceivedListener(t -> {
			internal.clear();
			t.stream().filter(predicate).forEach(internal::add);
			ret.dataReceived();
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
					addReceivedListener(l -> {
						internal.retainAll(l);
						internal.addAll(l);
						ret.dataReceived();
					});
					distinct = ret;
				}
			}
		}
		return distinct;
	}

	@Override
	public <K> ObsMapHolder<K, U> map(Function<U, K> keyExtractor) {
		return ObsMapHolderImpl.toMap(this, keyExtractor);
	}

	@Override
	public <K, V> ObsMapHolder<K, V> map(Function<U, K> keyExtractor, Function<U, V> valExtractor) {
		return ObsMapHolderImpl.toMap(this, keyExtractor, valExtractor);
	}

}