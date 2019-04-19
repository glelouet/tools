package fr.lelouet.tools.synchronization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

public class StarterTest extends Starter {

	private static final Logger logger = LoggerFactory
			.getLogger(StarterTest.class);

	private int count = 0;

	public synchronized void inc() {
		count++;
	}

	protected class incrementer implements Runnable {

		@Override
		public void run() {
			logger.trace("running");
			waitForStart();
			logger.trace("incrementing");
			inc();
		}

	}

	@Test
	public void simpleUseCase() {
		// we want to use this number of threads
		int threadsnum = 5;
		// we wait 1000 ms before the threads are dead.
		long timeout = 1000;
		// first we start threads that will be waiting for the start()
		for (int i = 0; i < threadsnum; i++) {
			new Thread(new incrementer()).start();
		}
		// we need to be sure every thread is started.
		waitFor(threadsnum, System.currentTimeMillis() + timeout);
		Assert.assertEquals(getThreadsNb(), threadsnum);
		// we start all the threads at once.
		start();
		long releaseTime = System.currentTimeMillis();
		while (count < 5) {
			long now = System.currentTimeMillis();
			if (now - releaseTime > 1000) {
				logger.debug(
						"released at {}, and at {} got only {} counts : error",
						new Object[] { releaseTime, now, count });
				Assert.fail("took more than one second to update the value");
			}
			Thread.yield();
		}
		Assert.assertEquals(getThreadsNb(), 0);
		Assert.assertEquals(count, threadsnum);
	}

}
