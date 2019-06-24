package fr.lelouet.collectionholders.impl.collections;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import fr.lelouet.collectionholders.impl.ObsObjHolderImpl;
import fr.lelouet.collectionholders.interfaces.ObsObjHolder;
import fr.lelouet.collectionholders.interfaces.collections.ObsMapHolder;
import fr.lelouet.collectionholders.interfaces.collections.ObsSetHolder;
import fr.lelouet.tools.synchronization.LockWatchDog;
import javafx.beans.Observable;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;

public class ObsSetHolderImpl<U> extends AObsCollectionHolder<U, Set<U>, ObservableSet<U>, SetChangeListener<? super U>>
implements ObsSetHolder<U> {

	public ObsSetHolderImpl(ObservableSet<U> underlying) {
		super(underlying);
	}

	@Override
	public Set<U> copy() {
		waitData();
		return LockWatchDog.BARKER.syncExecute(underlying, () -> {
			return new HashSet<>(underlying);
		});
	}

	@Override
	public void follow(SetChangeListener<? super U> listener) {
		LockWatchDog.BARKER.syncExecute(underlying, () -> {
			ObservableSet<U> otherset = FXCollections.observableSet(new HashSet<>());
			otherset.addListener(listener);
			otherset.addAll(underlying);
			underlying.addListener(listener);
		});
	}

	@Override
	public void unfollow(SetChangeListener<? super U> change) {
		LockWatchDog.BARKER.syncExecute(underlying, () -> {
			underlying.removeListener(change);
		});
	}

	@Override
	public Observable asObservable() {
		return underlying;
	}

	public static <T> ObsSetHolderImpl<T> filter(ObsSetHolder<T> source, Predicate<? super T> predicate) {
		ObservableSet<T> internal = FXCollections.observableSet(new HashSet<>());
		ObsSetHolderImpl<T> ret = new ObsSetHolderImpl<>(internal);
		source.addReceivedListener(t -> {
			internal.clear();
			t.stream().filter(predicate).forEach(internal::add);
			ret.dataReceived();
		});
		return ret;
	}

	@Override
	public ObsSetHolderImpl<U> filter(Predicate<? super U> predicate) {
		return filter(this, predicate);
	}

	@Override
	public <K> ObsMapHolder<K, U> map(Function<U, K> keyExtractor) {
		return ObsMapHolderImpl.toMap(this, keyExtractor);
	}

	@Override
	public <K, V> ObsMapHolder<K, V> map(Function<U, K> keyExtractor, Function<U, V> valExtractor) {
		return ObsMapHolderImpl.toMap(this, keyExtractor, valExtractor);
	}

	@Override
	public ObsObjHolder<Boolean> contains(U value) {
		SimpleObjectProperty<Boolean> internal = new SimpleObjectProperty<>();
		ObsObjHolderImpl<Boolean> ret = new ObsObjHolderImpl<>(internal);
		addReceivedListener(t -> {
			internal.set(t.contains(value));
		});
		return ret;
	}

	@Override
	public ObsObjHolder<Boolean> contains(ObsObjHolder<U> value) {
		SimpleObjectProperty<Boolean> internal = new SimpleObjectProperty<>();
		ObsObjHolderImpl<Boolean> ret = new ObsObjHolderImpl<>(internal);
		Set<Object> received = new HashSet<>();
		Runnable update = () -> {
			if (received.size() >= 2) {
				internal.set(underlying.contains(value.get()));
			}
		};
		addReceivedListener(t -> {
			received.add(this);
			update.run();
		});
		value.follow((observable, oldValue, newValue) -> {
			received.add(value);
			update.run();
		});
		return ret;
	}

}
