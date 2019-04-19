package fr.lelouet.tools.synchronization;

import java.util.concurrent.Semaphore;

/**
 * allow several threads to wait at the same time for one event to happen. The
 * threads are sleeping until that event really happens, at which time they are
 * awoken and returned the event.
 * 
 * @author Guillaume Le Louët
 * 
 * @param <E>
 *            the event type
 */
public class MultiSemaphore<E> {

	/** the semaphore that makes the threads wait */
	private Semaphore waiter = new Semaphore(0);

	/**
	 * the semaphore that wakes up when all the threads registered to
	 * {@link #waiter} have successfully taken the {@link #lastReceived}
	 */
	private Semaphore ThreadsGone = null;

	/** to prevent two {@link #putNext(Object)} at the same time. */
	private Object ReceivedElementsLock = new Object();

	private E lastReceived = null;

	public E getNext() {
		try {
			waiter.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			throw new UnsupportedOperationException("TODO : handle this.", e);
		}
		E ret = lastReceived;
		Semaphore toUnlock = null;
		while (toUnlock == null) {
			toUnlock = ThreadsGone;
			Thread.yield();
		}
		toUnlock.release();
		return ret;
	}

	public void putNext(E elem) {
		Semaphore msem = null;
		synchronized (ReceivedElementsLock) {
			msem = waiter;
			waiter = new Semaphore(0);
			int nbThreads = msem.getQueueLength();
			msem.release(nbThreads);
			Semaphore toWait = new Semaphore(1 - nbThreads);
			ThreadsGone = toWait;
			try {
				toWait.acquire();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				throw new UnsupportedOperationException("TODO : handle this.",
						e);
			}
			ThreadsGone = null;
		}
	}

}
