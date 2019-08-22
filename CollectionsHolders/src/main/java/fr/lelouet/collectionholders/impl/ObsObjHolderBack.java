package fr.lelouet.collectionholders.impl;

import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * an implementation that relies on an observable value
 *
 * @param <U>
 */
public class ObsObjHolderBack<U> extends AObsObjHolder<U> {

	protected ObservableValue<U> underlying;

	public ObsObjHolderBack(ObservableValue<U> underlying) {
		this.underlying = underlying;
		underlying.addListener(this::objchangelisten);
		if (underlying.getValue() != null) {
			objchangelisten(underlying, null, underlying.getValue());
		}
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
	public U get() {
		waitData();
		return underlying.getValue();
	}

	protected void objchangelisten(Object o, U old, U now) {
		waitLatch.countDown();
	}


	public void follow(ChangeListener<U> change) {
		synchronized (underlying) {
			if (waitLatch.getCount() <= 0) {
				change.changed(underlying, null, underlying.getValue());
			}
			underlying.addListener(change);
		}
	}


	public void unfollow(ChangeListener<U> change) {
		synchronized (underlying) {
			underlying.removeListener(change);
		}
	}

	// TODO remove this class
	@Override
	public void set(U item) {
		// do nothing. will remove.
	}

	@Override
	public void follow(Consumer<U> cons) {
		// TODO Auto-generated method stub

	}

	@Override
	public void unfollow(Consumer<U> cons) {
		// TODO Auto-generated method stub

	}

}
