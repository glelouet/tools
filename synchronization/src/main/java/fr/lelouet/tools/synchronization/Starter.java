package fr.lelouet.tools.synchronization;

import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allow several threads to be paused, and restarted at the same time.<br />
 * <p>
 * All the threads must call {@link #waitForStart()} to be set in pause.<br />
 * Then a call to {@link #start()} will remove them all from the pause.
 * </p>
 * <p>
 * a call to {@link #waitFor(int)} or {@link #wait(long, int)} makes active wait
 * until enough threads have been paused
 * </p>
 * 
 * @author guillaume Le Louet
 * 
 */
public class Starter {

	private static final Logger logger = LoggerFactory.getLogger(Starter.class);

	private Semaphore starter = new Semaphore(0);

	/**
	 * set to true when releasing the threads : a Threads should not be paused
	 * when the threads are being released.
	 */
	private boolean isReleasing = false;

	/** pause the invoking thread until {@link #start()} is called */
	public void waitForStart() {
		if (!isReleasing) {
			try {
				starter.acquire();
			} catch (InterruptedException e) {
				throw new UnsupportedOperationException(e);
			}
		}
	}

	/**
	 * start all paused threads
	 */
	public synchronized void start() {
		isReleasing = true;
		starter.release(starter.getQueueLength());
		isReleasing = false;
	}

	/**
	 * actively waits until given number of threads is waiting. Not
	 * synchronized, should only be used in a control Thread.
	 */
	public void waitFor(int nbThreads) {
		waitFor(nbThreads, Long.MAX_VALUE);
	}

	/**
	 * actively wait until a given number of threads are waiting, or the given
	 * time is elapsed. Uses {@link System.currentTimeMillis()}.
	 * 
	 * @param nbThreads
	 *            the number of threads we want to have before exiting
	 * @param maxTime
	 *            the time, as System.currentTimeMillis , to reach before
	 *            getting a timeout.
	 */
	public void waitFor(int nbThreads, long maxTime) {
		while (starter.getQueueLength() < nbThreads
				&& System.currentTimeMillis() < maxTime) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				logger.trace("", e);
			}
		}
	}

	/**
	 * the number of threads awaiting
	 * 
	 * @return the number of threads thar are waiting for a {@link #start()}
	 * 
	 * @return
	 */
	public long getThreadsNb() {
		return starter.getQueueLength();
	}

}
