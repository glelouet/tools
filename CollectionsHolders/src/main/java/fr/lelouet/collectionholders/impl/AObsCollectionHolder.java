package fr.lelouet.collectionholders.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import fr.lelouet.collectionholders.interfaces.ObsCollectionHolder;
import fr.lelouet.collectionholders.interfaces.ObsObjHolder;
import fr.lelouet.tools.synchronization.LockWatchDog;
import javafx.beans.property.SimpleObjectProperty;

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

	private ObsObjHolderImpl<Integer> size = null;

	@Override
	public ObsObjHolder<Integer> size() {
		if (size == null) {
			synchronized (this) {
				if (size == null) {
					SimpleObjectProperty<Integer> internal = new SimpleObjectProperty<>();
					ObsObjHolderImpl<Integer> ret = new ObsObjHolderImpl<>(internal);
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

}
