package fr.lelouet.tools.synchronization;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map.Entry;
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
	 * execute a runnable in a try block, which starts by syncing on a lock. the
	 * finally block releases the lock.
	 *
	 * @param lock
	 * @param run
	 */
	public void syncExecute(Object lock, Runnable run) {
		try {
			tak(lock);
			synchronized (lock) {
				hld(lock);
				run.run();
			}
		} finally {
			rel(lock);
		}
	}

	private final IdentityHashMap<Object, AquireData> aquisitions = new IdentityHashMap<>();

	/** for each thread, the set of locks it holds */
	private final HashMap<Thread, IdentityHashMap<Object, Object>> threadsLocksHolding = new HashMap<>();

	public static boolean skip = System.getProperties().contains("nowatchdog");

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
			}
			data.dates.add(new Date());
			List<StackTraceElement> l = Arrays.asList(Thread.currentThread().getStackTrace());
			data.takerTraces.put(th, l.subList(2, l.size()));
			// search for deadlocks
			// starting by this thread, get all the locks it owns
			// for each locked owned, add the thread that are taking them
			// deadlock if a lock owned is the lock we are acquiring.
			List<Thread> nextThreads = new ArrayList<>();
			nextThreads.add(th);
			do {
				Thread loopThread = nextThreads.remove(0);
				IdentityHashMap<Object, Object> locksHoldByLoopThread = threadsLocksHolding.get(loopThread);
				if (locksHoldByLoopThread != null) {
					for (Object otherLockHold : locksHoldByLoopThread.keySet()) {
						if (otherLockHold == lock) {
							logLocks();
							data.takerTraces.remove(th);
							throw new NullPointerException("deadlock");
						}
						AquireData ad = aquisitions.get(otherLockHold);
						for (Thread toAdd : ad.takerTraces.keySet()) {
							if(toAdd!=loopThread) {
								nextThreads.add(toAdd);
							}
						}
					}
				}
			} while (!nextThreads.isEmpty());
		}
	}

	public void rel(Object lock) {
		if (skip) {
			return;
		}
		Thread th = Thread.currentThread();
		synchronized (aquisitions) {
			AquireData data = aquisitions.get(lock);
			if (data == null || data.takerTraces.size() == 0) {
				throw new NullPointerException("releasing a lock not acquired");
			}
			data.takerTraces.remove(th);
			data.holder = null;
			if (data.takerTraces.size() == 0) {
				data.dates.clear();
			}
			IdentityHashMap<Object, Object> threadSets = threadsLocksHolding.get(th);
			threadSets.remove(lock);
			if (threadSets.isEmpty()) {
				threadsLocksHolding.remove(th);
			}
		}
	}

	public void hld(Object lock) {
		if (skip) {
			return;
		}
		Thread th = Thread.currentThread();
		synchronized (aquisitions) {
			AquireData data = aquisitions.get(lock);
			if (data == null || data.takerTraces.size() == 0) {
				logger.warn("holding a lock not acquired", new NullPointerException());
				return;
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
				if (val.takerTraces.size() == 0) {
					continue;
				}
				nolock = false;
				logger.debug("" + val.takerTraces.size());
				Date firstdate = val.dates.stream().sorted((d1, d2) -> (int) Math.signum(d2.getTime() - d1.getTime()))
						.findFirst().orElse(null);
				logger.debug("  acquired " + (now.getTime() - firstdate.getTime()) / 1000 + " s ago, hold by " + val.holder);
				for (Entry<Thread, List<StackTraceElement>> l : val.takerTraces.entrySet()) {
					logger.debug("    " + l.getKey());
					for (StackTraceElement v : l.getValue()) {
						logger.debug("      " + v.toString().replace('[', ' '));
					}
				}
			}
			if (nolock) {
				logger.trace("watchdog no lock");
			}
		}
	}

	public static final long periodLogSeconds = 30;

	private LockWatchDog() {
		if (!skip) {
			Executors.newScheduledThreadPool(1).scheduleAtFixedRate(this::log, periodLogSeconds, periodLogSeconds,
					TimeUnit.SECONDS);
		}
	}

	private HashMap<Thread, Date> monitorTime = new HashMap<>();

	public void monitor(Runnable run) {
		Thread thread = Thread.currentThread();
		Date date = new Date();
		boolean added = false;
		try {
			synchronized (monitorTime) {
				added = monitorTime.putIfAbsent(thread, date) == null;
			}
			run.run();
		} finally {
			if (added) {
				synchronized (monitorTime) {
					monitorTime.remove(thread);
				}
			}
		}
	}

	public void logMonitors() {
		synchronized (monitorTime) {
			logger.debug("monitored threads : ");
			Date now = new Date();
			for (Entry<Thread, Date> e : monitorTime.entrySet()) {
				Exception ex = new Exception("thread at position");
				ex.setStackTrace(e.getKey().getStackTrace());
				long secondsAcquired = (now.getTime() - e.getValue().getTime()) / 1000;
				if (secondsAcquired > periodLogSeconds) {
					logger.debug(" " + (now.getTime() - e.getValue().getTime()) / 1000 +"s ago", ex);
				}
			}
		}
	}

	public void log() {
		logLocks();
		logMonitors();
	}

	public static final LockWatchDog BARKER = new LockWatchDog();

}
