package fr.lelouet.tools.synchronization;

import java.util.concurrent.Callable;

/**
 * monitors the locks to ensure no deadlock is present.
 */
public interface IWatchDog {

	/**
	 * called when a lock is getting acquired
	 * 
	 * @param lock the lock
	 */
	public default void tak(Object lock) {
	}

	/**
	 * called when a lock is acquired
	 * 
	 * @param lock the lock
	 */
	public default void hld(Object lock) {
	}

	/**
	 * called when a lock is released
	 * 
	 * @param lock the lock
	 */
	public default void rel(Object lock) {
	}

	/**
	 * execute a callable in a try block, which starts by syncing on a lock. the
	 * finally block releases the lock.
	 *
	 * @param <T>
	 *               the type returned by the callable
	 * @param lock
	 *               the lock to sync over
	 * @param run
	 *               the callable to execute
	 */
	public default <T> T syncExecute(Object lock, Callable<T> run) {
		try {
			tak(lock);
			synchronized (lock) {
				hld(lock);
				T ret = run.call();
				rel(lock);
				return ret;
			}
		} catch (Exception e) {
			throw new UnsupportedOperationException(e);
		} finally {
			rel(lock);
		}
	}

	/**
	 * execute a runnable in a try block, which starts by syncing on a lock. the
	 * finally block releases the lock.
	 *
	 * @param lock
	 *               the lock to sync over
	 * @param run
	 *               the runnable to execute
	 */
	public default void syncExecute(Object lock, Runnable run) {
		syncExecute(lock, () -> {
			run.run();
			return null;
		});
	}

}
