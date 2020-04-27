package fr.lelouet.tools.synchronization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** lock watchdog to watch what acquires and releases synchronization items */
public class LockWatchDog {

	private static final Logger logger = LoggerFactory.getLogger(LockWatchDog.class);

	public static class AquireData {

		public HashMap<Thread, List<StackTraceElement>> takerTraces = new HashMap<>();
		public ArrayList<Date> dates = new ArrayList<>();

		public Thread holder;

	}

	/**
	 * execute a callable in a try block, which starts by syncing on a lock. the
	 * finally block releases the lock.
	 *
	 * @param <T>
	 *          the type returned by the callable
	 *
	 * @param lock
	 *          the lock to sync over
	 * @param run
	 *          the callable to execute
	 */
	public <T> T syncExecute(Object lock, Callable<T> run) {
		try {
			tak(lock);
			synchronized (lock) {
				hld(lock);
				T ret = run.call();
				rel(lock);
				return ret;
			}
		} catch (Exception e) {
			throw new UnsupportedOperationException("catch this", e);
		} finally {
			rel(lock);
		}
	}

	/**
	 * execute a runnable in a try block, which starts by syncing on a lock. the
	 * finally block releases the lock.
	 *
	 *
	 * @param lock
	 *          the lock to sync over
	 * @param run
	 *          the runnable to execute
	 */
	public void syncExecute(Object lock, Runnable run) {
		syncExecute(lock, () -> {
			run.run();
			return null;
		});
	}

	private final IdentityHashMap<Object, AquireData> aquisitions = new IdentityHashMap<>();

	/** for each thread, the set of locks it holds */
	private final HashMap<Thread, IdentityHashMap<Object, Object>> threadsLocksHolding = new HashMap<>();

	public static boolean skip = System.getProperties().contains("nowatchdog");
	// true;

	public void tak(Object lock) {
		if (skip) {
			return;
		}
		Thread th = Thread.currentThread();
		synchronized (aquisitions) {
			AquireData data = aquisitions.get(lock);
			if (data == null) {
				data = new AquireData();
				aquisitions.put(lock, data);
			} else {
				Set<Object> idset = Collections.newSetFromMap(new IdentityHashMap<>());
				idset.add(lock);
				try {
					searchdeadlocks(idset, th);
				} catch (NullPointerException npe) {
					debug(" while thread " + th + " is taking lock " + identityPrint(lock) + " on "
							+ Thread.currentThread().getStackTrace()[2]);
					throw npe;
				}
			}
			data.dates.add(new Date());
			List<StackTraceElement> l = Arrays.asList(Thread.currentThread().getStackTrace());
			data.takerTraces.put(th, l.subList(2, l.size()));
		}
	}

	/**
	 * search for a deadlock
	 * <p>
	 * The pattern of search is following : <br />
	 * thread t_a wants lock l_a but the lock l_a is actually hold by a thread and
	 * there is a list of locks l_a l_2 l_3 . . . l_n, that for each i, l_i is
	 * hold by a thread t_i that is trying to take the lock l_i+1 and l_n is hold
	 * by t_a
	 * </p>
	 * <p>
	 * recursively checks that a forbidden object is not hold by a current thread
	 * or a thread waiting after an object this current thread holds
	 * </p>
	 * <p>
	 * recursion allows for easy stack trace explanation.
	 * </p>
	 *
	 * @param forbidden
	 * @param currentthread
	 */
	private void searchdeadlocks(Set<Object> forbidden, Thread currentthread) {
		IdentityHashMap<Object, Object> locksHoldByLoopThread = threadsLocksHolding.get(currentthread);
		if (locksHoldByLoopThread != null) {
			for (Object nextLock : locksHoldByLoopThread.keySet()) {
				if (forbidden.contains(nextLock)) {
					AquireData acq = aquisitions.get(nextLock);
					debug("deadlock : " + currentthread + " holds " + identityPrint(nextLock));
					for (Entry<Thread, List<StackTraceElement>> e : acq.takerTraces.entrySet()) {
						debug("    " + e.getKey());
						for (int i = 0; i < e.getValue().size() && i < 5; i++) {
							debug("     " + e.getValue().get(i));
						}
					}
					debugtrace(acq.takerTraces.get(acq.holder));
					throw new NullPointerException("deadlock");
				} else {
					forbidden.add(nextLock);
					try {
						AquireData ad = aquisitions.get(nextLock);
						for (Thread nextthread : ad.takerTraces.keySet()) {
							// we need to avoid checking which locks
							if (nextthread != currentthread) {
								searchdeadlocks(forbidden, nextthread);
							}
						}
					} catch (Throwable e) {
						AquireData acq = aquisitions.get(nextLock);
						debug(" " + currentthread + " holds " + identityPrint(nextLock) + " on " + acq.takerTraces.get(acq.holder));
						for (Entry<Thread, List<StackTraceElement>> entry : acq.takerTraces.entrySet()) {
							debug("    " + entry.getKey());
							for (int i = 0; i < entry.getValue().size() && i < 5; i++) {
								debug("     " + entry.getValue().get(i));
							}
						}
						debugtrace(acq.takerTraces.get(acq.holder));
						throw e;
					}
					forbidden.remove(nextLock);
				}
			}
		}
	}

	private static String identityPrint(Object ob) {
		if (ob == null) {
			return "null";
		}
		return ob.getClass().getSimpleName() + "@" + Integer.toHexString(System.identityHashCode(ob));
	}

	private static void debugtrace(List<StackTraceElement> list) {
		for (StackTraceElement e : list) {
			debug("  at " + e);
		}
	}

	public void rel(Object lock) {
		if (skip) {
			return;
		}
		Thread th = Thread.currentThread();
		synchronized (aquisitions) {
			AquireData data = aquisitions.get(lock);
			if (data == null) {
				return;
			}
			data.takerTraces.remove(th);
			data.holder = null;
			if (data.takerTraces.size() == 0) {
				data.dates.clear();
			}
			IdentityHashMap<Object, Object> threadSets = threadsLocksHolding.get(th);
			if (threadSets != null) {
				threadSets.remove(lock);
				if (threadSets.isEmpty()) {
					threadsLocksHolding.remove(th);
				}
			}
		}
	}

	public void hld(Object lock) {
		if (skip) {
			return;
		}
		Thread th = Thread.currentThread();
		synchronized (aquisitions) {
			for (Entry<Thread, IdentityHashMap<Object, Object>> e : threadsLocksHolding.entrySet()) {
				if (e.getValue().containsKey(lock)) {
					debug("lock " + identityPrint(lock) + " requested by " + th + " already hold by " + e.getKey());
					AquireData acqa = aquisitions.get(lock);
					if (acqa != null) {
						for (Entry<Thread, List<StackTraceElement>> e2 : acqa.takerTraces.entrySet()) {
							debug(" " + e2.getKey());
							debugtrace(e2.getValue());
							debug("  currently");
							debugtrace(Arrays.asList(e2.getKey().getStackTrace()));
						}
					}
					throw new NullPointerException("double hold");
				}
			}
			AquireData data = aquisitions.get(lock);
			if (data == null || data.takerTraces.size() == 0) {
				throw new NullPointerException("holding lock not acquired " + identityPrint(lock));
			}
			data.holder = th;
			IdentityHashMap<Object, Object> locks = threadsLocksHolding.get(th);
			if (locks == null) {
				locks = new IdentityHashMap<>();
				threadsLocksHolding.put(th, locks);
			}
			locks.put(lock, null);
		}

	}

	public void logLocks() {
		Date now = new Date();
		synchronized (aquisitions) {
			boolean nolock = true;
			for (Entry<Object, AquireData> e : aquisitions.entrySet()) {
				AquireData val = e.getValue();
				Date firstdate = val.dates.stream().sorted((d1, d2) -> (int) Math.signum(d2.getTime() - d1.getTime()))
						.findFirst().orElse(null);
				long acquired = firstdate == null ? 0 : (now.getTime() - firstdate.getTime()) / 1000;
				if (val.takerTraces.size() == 0 || acquired < periodLogSeconds) {
					continue;
				}
				nolock = false;
				debug("" + val.takerTraces.size());
				debug("  acquired " + (now.getTime() - firstdate.getTime()) / 1000 + " s ago, hold by " + val.holder);
				for (Entry<Thread, List<StackTraceElement>> l : val.takerTraces.entrySet()) {
					debug("    " + l.getKey());
					for (StackTraceElement v : l.getValue()) {
						debug("      " + v.toString().replace('[', ' '));
					}
				}
			}
			if (nolock) {
				logger.trace("watchdog no lock");
			}
		}
	}

	private static void debug(String s) {
		// System.err.println(s);
		logger.debug(s);
	}

	public static final long periodLogSeconds = 30;

	private LockWatchDog() {
		if (!skip) {
			Executors.newScheduledThreadPool(1, r -> {
				Thread t = Executors.defaultThreadFactory().newThread(r);
				t.setDaemon(true);
				return t;
			}).scheduleAtFixedRate(this::log, periodLogSeconds, periodLogSeconds, TimeUnit.SECONDS);
		}
	}

	private HashMap<Thread, Date> monitorTime = new HashMap<>();

	public void monitor(Runnable run) throws Exception {
		Thread thread = Thread.currentThread();
		Date date = new Date();
		boolean added = false;
		Exception ex = null;
		try {
			synchronized (monitorTime) {
				added = monitorTime.putIfAbsent(thread, date) == null;
			}
			run.run();
		} catch (Exception e) {
			ex = e;
		} finally {
			if (added) {
				synchronized (monitorTime) {
					monitorTime.remove(thread);
				}
			}
			if (ex != null) {
				throw ex;
			}
		}
	}

	public void logMonitors() {
		if (monitorTime.size() > 0) {
			synchronized (monitorTime) {
				logger.debug("monitored threads : ");
				Date now = new Date();
				for (Entry<Thread, Date> e : monitorTime.entrySet()) {
					Exception ex = new Exception("thread at position");
					ex.setStackTrace(e.getKey().getStackTrace());
					long secondsAcquired = (now.getTime() - e.getValue().getTime()) / 1000;
					if (secondsAcquired > periodLogSeconds) {
						logger.debug(" " + (now.getTime() - e.getValue().getTime()) / 1000 + "s ago", ex);
					}
				}
			}
		}
	}

	public void log() {
		if (!skip) {
			logLocks();
		}
		logMonitors();
	}

	public static final LockWatchDog BARKER = new LockWatchDog();

}
