package fr.lelouet.collectionholders.impl.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.CountDownLatch;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import fr.lelouet.collectionholders.impl.ObsObjHolderImpl;
import fr.lelouet.collectionholders.impl.numbers.ObsIntHolderImpl;
import fr.lelouet.collectionholders.interfaces.ObsObjHolder;
import fr.lelouet.collectionholders.interfaces.collections.ObsCollectionHolder;
import fr.lelouet.collectionholders.interfaces.collections.ObsListHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsIntHolder;
import fr.lelouet.tools.synchronization.LockWatchDog;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public abstract class AObsCollectionHolder<U, C extends Collection<U>, OC extends C, L>
implements ObsCollectionHolder<U, C, L> {

	protected OC underlying;

	public AObsCollectionHolder(OC underlying) {
		this.underlying = underlying;
	}

	CountDownLatch waitLatch = new CountDownLatch(1);

	private ArrayList<Consumer<C>> receiveListeners;

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

	private ObsIntHolderImpl size = null;

	@Override
	public ObsIntHolder size() {
		if (size == null) {
			synchronized (this) {
				if (size == null) {
					SimpleObjectProperty<Integer> internal = new SimpleObjectProperty<>();
					ObsIntHolderImpl ret = new ObsIntHolderImpl(internal);
					addReceivedListener(c -> internal.set(c.size()));
					size = ret;
				}
			}
		}
		return size;
	}

	@Override
	public void addReceivedListener(Consumer<C> callback) {
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
	public boolean remReceivedListener(Consumer<C> callback) {
		synchronized (underlying) {
			return receiveListeners.remove(callback);
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
				for (Consumer<C> r : receiveListeners) {
					r.accept(consumed);
				}
			}
		});
	}

	@Override
	public ObsListHolder<U> sorted(Comparator<U> comparator) {
		ObservableList<U> internal = FXCollections.observableArrayList();
		ObsListHolderImpl<U> ret = new ObsListHolderImpl<>(internal);
		addReceivedListener(o -> {
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
		addReceivedListener(o -> {
			leftCol[0] = o;
			update.run();
		});
		right.addReceivedListener(o -> {
			rightCol[0] = o;
			update.run();
		});
		return ret;
	}

	@Override
	public <V> ObsObjHolder<V> reduce(Function<C, V> collectionReducer) {
		SimpleObjectProperty<V> internal = new SimpleObjectProperty<>();
		ObsObjHolder<V> ret = new ObsObjHolderImpl<>(internal);
		addReceivedListener(l -> internal.set(collectionReducer.apply(l)));
		return ret;
	}

}
