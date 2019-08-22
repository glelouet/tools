package fr.lelouet.collectionholders.impl;

import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

/**
 * an implementation that contains the item to return and the list of listeners;
 */
public class ObsObjHolderSimple<U> extends AObsObjHolder<U> {

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
		item = newitem;
		for (Consumer<U> cons : followers) {
			cons.accept(newitem);
		}
		waitLatch.countDown();
	}

}
