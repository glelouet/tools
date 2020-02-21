package fr.lelouet.collectionholders.impl;

import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import fr.lelouet.collectionholders.interfaces.RWObsObjHolder;

/**
 * an implementation that contains the item to return and the list of listeners;
 */
public class ObsObjHolderSimple<U> extends AObsObjHolder<U> implements RWObsObjHolder<U> {

	public ObsObjHolderSimple() {
	}

	public ObsObjHolderSimple(U item) {
		set(item);
	}

	CountDownLatch waitLatch = new CountDownLatch(1);

	public void waitData() {
		try {
			waitLatch.await();
		} catch (InterruptedException e) {
			throw new UnsupportedOperationException("catch this", e);
		}
	}

	public boolean isDataAvailable() {
		return waitLatch.getCount() == 0;
	}

	private U item;

	@Override
	public U get() {
		waitData();
		return item;
	}

	private LinkedList<Consumer<U>> followers = new LinkedList<>();

	@Override
	public synchronized void follow(Consumer<U> cons) {
		followers.add(cons);
		if (waitLatch.getCount() == 0) {
			cons.accept(item);
		}
	}

	@Override
	public synchronized void unfollow(Consumer<U> cons) {
		followers.remove(cons);
	}

	@Override
	public synchronized void set(U newitem) {
		// if there was already a value set, and we set to this same value, don't
		// propagate.
		if (waitLatch.getCount() == 0 && (newitem == item || newitem != null && newitem.equals(item))) {
			return ;
		}
		item = newitem;
		for (Consumer<U> cons : followers) {
			cons.accept(newitem);
		}
		waitLatch.countDown();
	}

}
