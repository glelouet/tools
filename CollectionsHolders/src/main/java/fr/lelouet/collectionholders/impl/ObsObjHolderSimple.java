package fr.lelouet.collectionholders.impl;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import fr.lelouet.collectionholders.interfaces.ObsObjHolder;
import fr.lelouet.collectionholders.interfaces.RWObsObjHolder;
import fr.lelouet.tools.lambdaref.withstore.references.HardRef;
import fr.lelouet.tools.lambdaref.withstore.references.IRef;
import fr.lelouet.tools.lambdaref.withstore.references.UsualRef;
import fr.lelouet.tools.lambdaref.withstore.references.WeakRef;
import lombok.Getter;
import lombok.Setter;

/**
 * an implementation that contains the item to return and the list of listeners;
 */
public class ObsObjHolderSimple<U> extends AObsObjHolder<U> implements RWObsObjHolder<U>, Consumer<Object> {

	public ObsObjHolderSimple() {
	}

	public ObsObjHolderSimple(U item) {
		set(item);
	}

	private CountDownLatch dataReceivedLatch = new CountDownLatch(1);

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

	private final LinkedList<IRef<Consumer<U>>> followers = new LinkedList<>();

	private static final boolean USEWEAKREF = true;

	@Override
	public synchronized ObsObjHolder<U> follow(Consumer<U> cons, Consumer<Object> holder) {
		if (holder == null) {
			followers.add(new HardRef<>(cons));
			IRef.hard(this);
		} else {
			if (USEWEAKREF) {
				holder.accept(new Object[] { cons, this });
				followers.add(new WeakRef<>(cons));
			} else {
				followers.add(new UsualRef<>(cons));
			}
		}
		if (dataReceivedLatch.getCount() == 0) {
			cons.accept(item);
		}
		return this;
	}

	@Override
	public synchronized void unfollow(Consumer<U> cons) {
		followers.removeIf(ref -> {
			Consumer<U> h = ref.get();
			return h == null || h.equals(cons);
		});
	}

	@Override
	public synchronized void set(U newitem) {
		// if there was already a value set, and we set to this same value, don't
		// propagate.
		if (isDataAvailable() && (newitem == item || newitem != null && newitem.equals(item))) {
			return ;
		}
		item = newitem;
		transmitToListeners();
		dataReceivedLatch.countDown();
	}

	@Override
	public ObsObjHolder<U> or(U defaultValue) {
		ObsObjHolderSimple<U> ret = new ObsObjHolderSimple<>(defaultValue);
		follow(ret::set);
		return ret;
	}

	@Getter
	@Setter
	private String name = null;

	/**
	 * transmit the item to the listeners. Should be called inside a synchronized
	 * call.
	 */
	protected void transmitToListeners() {
		for (Iterator<IRef<Consumer<U>>> it = followers.iterator(); it.hasNext();) {
			Consumer<U> cons = it.next().get();
			if (cons == null) {
				it.remove();
			} else {
				cons.accept(item);
			}
		}
	}

	public int followers() {
		return followers.size();
	}

	//
	// consumer<Object>
	//

	private final transient LinkedList<Object> stored = new LinkedList<>();

	@Override
	public void accept(Object t) {
		synchronized (stored) {
			stored.add(t);
		}
	}

}
