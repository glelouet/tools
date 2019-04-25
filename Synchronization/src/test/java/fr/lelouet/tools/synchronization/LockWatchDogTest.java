package fr.lelouet.tools.synchronization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;

import org.testng.Assert;
import org.testng.annotations.Test;

public class LockWatchDogTest {

	@Test
	public void testDeadLocks() {
		Object lock = "a";

		try {
			LockWatchDog.BARKER.tak(lock);
			LockWatchDog.BARKER.hld(lock);
			LockWatchDog.BARKER.tak(lock);
			Assert.fail("expected a deadlock");
		} catch (NullPointerException npe) {
		} finally {
			LockWatchDog.BARKER.rel(lock);
		}

		try {
			LockWatchDog.BARKER.tak(lock);
			LockWatchDog.BARKER.hld(lock);
			LockWatchDog.BARKER.tak(lock);
			Assert.fail("expected a deadlock");
		} catch (NullPointerException npe) {
		} finally {
			LockWatchDog.BARKER.rel(lock);
		}

		Object lock2 = "b";

		try {
			LockWatchDog.BARKER.tak(lock2);
			LockWatchDog.BARKER.hld(lock2);
			LockWatchDog.BARKER.tak(lock);
			LockWatchDog.BARKER.hld(lock);
			LockWatchDog.BARKER.tak(lock);
			Assert.fail("expected a deadlock");
		} catch (NullPointerException npe) {
		} finally {
			LockWatchDog.BARKER.rel(lock);
			LockWatchDog.BARKER.rel(lock2);
		}
	}

	@Test
	public void testTripleThreadDeadlock() throws InterruptedException {
		Object a = "a";
		Object b = "b";
		Object c = "c";
		CountDownLatch cdl1 = new CountDownLatch(1);
		new Thread(() -> {
			LockWatchDog.BARKER.tak(a);
			LockWatchDog.BARKER.hld(a);
			LockWatchDog.BARKER.tak(b);
			cdl1.countDown();
		}).start();
		cdl1.await();

		CountDownLatch cdl2 = new CountDownLatch(1);
		new Thread(() -> {
			LockWatchDog.BARKER.tak(b);
			LockWatchDog.BARKER.hld(b);
			LockWatchDog.BARKER.tak(c);
			cdl2.countDown();
		}).start();
		cdl2.await();

		LockWatchDog.BARKER.tak(c);
		LockWatchDog.BARKER.hld(c);
		try {
			LockWatchDog.BARKER.tak(a);
			Assert.fail("expected a deadlock");
		} catch (NullPointerException npe) {

		}
		LockWatchDog.BARKER.rel(a);
		LockWatchDog.BARKER.rel(b);
		LockWatchDog.BARKER.rel(c);
	}

	@Test
	public void testNoLockEmptyLists() {
		Object a = new ArrayList<>();
		Object b = Collections.emptyList();
		Object c = new ArrayList<>();
		try {
			LockWatchDog.BARKER.tak(a);
			LockWatchDog.BARKER.hld(a);
			LockWatchDog.BARKER.tak(b);
			LockWatchDog.BARKER.hld(b);
			LockWatchDog.BARKER.tak(c);
			LockWatchDog.BARKER.hld(c);
			try {
				LockWatchDog.BARKER.tak(a);
				Assert.fail("expected a deadlock");
			} catch (NullPointerException e) {
			}
		} finally {
			LockWatchDog.BARKER.rel(a);
			LockWatchDog.BARKER.rel(b);
			LockWatchDog.BARKER.rel(c);
		}
	}

}
