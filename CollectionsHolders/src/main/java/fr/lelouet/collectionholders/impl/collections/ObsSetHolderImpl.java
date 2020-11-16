package fr.lelouet.collectionholders.impl.collections;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import fr.lelouet.collectionholders.impl.numbers.ObsBoolHolderImpl;
import fr.lelouet.collectionholders.interfaces.ObsObjHolder;
import fr.lelouet.collectionholders.interfaces.collections.ObsMapHolder;
import fr.lelouet.collectionholders.interfaces.collections.ObsSetHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsBoolHolder;
import fr.lelouet.tools.synchronization.LockWatchDog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;

public class ObsSetHolderImpl<U> extends AObsCollectionHolder<U, Set<U>, ObservableSet<U>, SetChangeListener<? super U>>
implements ObsSetHolder<U> {

	@Override
	public Set<U> copy() {
		ObservableSet<U> underlying = underlying();
		synchronized (underlying) {
			return new LinkedHashSet<>(underlying);
		}
	}

	/**
	 * create a unmodifiable observable set of items
	 *
	 * @param <U>
	 *          type of the items
	 * @param args
	 *          items to add.
	 * @return a new observable set
	 */
	@SafeVarargs
	public static <U> ObsSetHolderImpl<U> of(U... args) {
		ObsSetHolderImpl<U> ret = new ObsSetHolderImpl<>(FXCollections.observableSet(args));
		ret.dataReceived();
		return ret;
	}

	public ObsSetHolderImpl(ObservableSet<U> underlying) {
		super(underlying);
	}

	public ObsSetHolderImpl() {
		this(FXCollections.observableSet(new LinkedHashSet<>()));
	}

	@Override
	public Set<U> get() {
		waitData();
		return LockWatchDog.BARKER.syncExecute(underlying(), () -> {
			return new HashSet<>(underlying());
		});
	}

	@Override
	public void followItems(SetChangeListener<? super U> listener) {
		LockWatchDog.BARKER.syncExecute(underlying(), () -> {
			ObservableSet<U> otherset = FXCollections.observableSet(new HashSet<>());
			otherset.addListener(listener);
			otherset.addAll(underlying());
			underlying().addListener(listener);
		});
	}

	@Override
	public void unfollowItems(SetChangeListener<? super U> change) {
		LockWatchDog.BARKER.syncExecute(underlying(), () -> {
			underlying().removeListener(change);
		});
	}

	@Override
	public ObsSetHolderImpl<U> filter(Predicate<? super U> predicate) {
		ObservableSet<U> internal = FXCollections.observableSet(new HashSet<>());
		ObsSetHolderImpl<U> ret = new ObsSetHolderImpl<>(internal);
		follow((t) -> {
			Set<U> filteredSet = t.stream().filter(predicate).collect(Collectors.toSet());
			synchronized (internal) {
				internal.retainAll(filteredSet);
				internal.addAll(filteredSet);
			}
			ret.dataReceived();
		});
		return ret;
	}

	@Override
	public ObsSetHolderImpl<U> filterWhen(Function<? super U, ObsBoolHolder> filterer) {
		ObservableSet<U> internal = FXCollections.observableSet(new HashSet<>());
		ObsSetHolderImpl<U> ret = new ObsSetHolderImpl<>(internal);
		filterWhen(filteredStream -> {
			Set<U> filteredSet = filteredStream.collect(Collectors.toSet());
			synchronized (internal) {
				internal.retainAll(filteredSet);
				internal.addAll(filteredSet);
			}
			ret.dataReceived();
		}, filterer);
		return ret;
	}

	@Override
	public <K, V> ObsMapHolder<K, V> toMap(Function<U, K> keyExtractor, Function<U, V> valExtractor,
			BinaryOperator<V> collisionHandler) {
		return ObsMapHolderImpl.toMap(this, keyExtractor, valExtractor, collisionHandler);
	}

	@Override
	public ObsBoolHolderImpl contains(U value) {
		ObsBoolHolderImpl ret = new ObsBoolHolderImpl();
		follow((t) -> {
			ret.set(t.contains(value));
		});
		return ret;
	}

	@Override
	public ObsBoolHolderImpl contains(ObsObjHolder<U> value) {
		ObsBoolHolderImpl ret = new ObsBoolHolderImpl();
		Set<Object> received = new HashSet<>();
		Runnable update = () -> {
			if (received.size() >= 2) {
				ret.set(underlying().contains(value.get()));
			}
		};
		follow((t) -> {
			received.add(this);
			update.run();
		});
		value.follow((newValue) -> {
			received.add(value);
			update.run();
		});
		return ret;
	}

	@Override
	public ObsSetHolder<U> distinct() {
		return this;
	}

}
