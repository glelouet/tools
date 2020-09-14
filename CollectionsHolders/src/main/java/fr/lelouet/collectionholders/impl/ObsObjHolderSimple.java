package fr.lelouet.collectionholders.impl;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import fr.lelouet.collectionholders.interfaces.RWObsObjHolder;
import fr.lelouet.tools.lambdaref.HoldingRef;

/**
 * an implementation that contains the item to return and the list of listeners;
 */
public class ObsObjHolderSimple<U> extends AObsObjHolder<U> implements RWObsObjHolder<U> {

	public ObsObjHolderSimple() {
	}

	public ObsObjHolderSimple(U item) {
		set(item);
	}

	protected CountDownLatch dataReceivedLatch = new CountDownLatch(1);

	public void waitData() {
		try {
			dataReceivedLatch.await();
		} catch (InterruptedException e) {
			throw new UnsupportedOperationException("catch this", e);
		}
	}

	public boolean isDataAvailable() {
		return dataReceivedLatch.getCount() == 0;
	}

	protected U item;

	@Override
	public U get() {
		waitData();
		return item;
	}

	private LinkedList<HoldingRef<Consumer<U>>> followers = new LinkedList<>();

	@Override
	public synchronized void follow(Consumer<U> cons, Object holder) {
		if (holder == null) {
			holder = this;
		}
		followers.add(new HoldingRef<>(cons, holder));
		if (dataReceivedLatch.getCount() == 0) {
			cons.accept(item);
		}
	}

	@Override
	public synchronized void unfollow(Consumer<U> cons) {
		followers.removeIf(ref -> {
			var h = ref.get();
			return h == null || h.equals(cons);
		});
	}

	@Override
	public synchronized void set(U newitem) {
		// if there was already a value set, and we set to this same value, don't
		// propagate.
		if (dataReceivedLatch.getCount() == 0 && (newitem == item || newitem != null && newitem.equals(item))) {
			return ;
		}
		item = newitem;
		transmitToListeners();
		dataReceivedLatch.countDown();
	}

	/**
	 * transmit the item to the listeners. Should be called inside a synchronized
	 * call.
	 */
	protected void transmitToListeners() {
		for (Iterator<HoldingRef<Consumer<U>>> it = followers.iterator(); it.hasNext();) {
			Consumer<U> cons = it.next().get();
			if (cons == null) {
				it.remove();
			} else {
				cons.accept(item);
			}
		}
	}

}
