package fr.lelouet.collectionholders.impl;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.lelouet.collectionholders.interfaces.ObsObjHolder;
import fr.lelouet.collectionholders.interfaces.RWObsObjHolder;
import fr.lelouet.tools.lambdaref.withstore.references.IRef;
import fr.lelouet.tools.lambdaref.withstore.references.UsualRef;
import fr.lelouet.tools.lambdaref.withstore.references.WeakRef;
import lombok.Getter;
import lombok.Setter;

/**
 * an implementation that contains the item to return and the list of listeners;
 */
public class ObsObjHolderSimple<U> extends AObsObjHolder<U> implements RWObsObjHolder<U>, Consumer<Object> {

	private static final Logger logger = LoggerFactory.getLogger(ObsObjHolderSimple.class);

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

	@SuppressWarnings("unchecked")
	@Override
	public synchronized void follow(Consumer<U> cons, Consumer<Object>... holders) {
		if (holders == null || holders.length == 0) {
			followers.add(new UsualRef<>(cons));
		} else {
			for (Consumer<Object> holder : holders) {
				holder.accept(cons);
			}
			followers.add(new WeakRef<>(cons));
			// followers.add(new UsualRef<>(cons));
		}
		if (dataReceivedLatch.getCount() == 0) {
			cons.accept(item);
		}
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
		if (dataReceivedLatch.getCount() == 0 && (newitem == item || newitem != null && newitem.equals(item))) {
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
				logger.debug("remove listener from " + (name == null ? this : name));
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
