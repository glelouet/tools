package fr.lelouet.tools.holders.cache;

import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.slf4j.Logger;

import fr.lelouet.tools.holders.interfaces.numbers.IntHolder;

public class ExamplePeriodic {

	private static final Logger log = org.slf4j.LoggerFactory.getLogger(ExamplePeriodic.class);

	public static void main(String[] args) throws InterruptedException {
		ScheduledThreadPoolExecutor stpe = new ScheduledThreadPoolExecutor(10);
		int[] add = new int[1];
		URIBasedCache<Integer, IntHolder> cache = PeriodicFetch
				.cacheToInt(
						stpe,
						uri -> uri.length() + add[0],
						1000);
		cache.get("my uri").follow(i -> log.info("received from 'my uri' " + i));
		cache.get("my 2nd uri").follow(i -> log.info("received from 'my 2nd uri' " + i));
		while (true) {
			Thread.sleep(3000);
			add[0]++;
		}
	}

}
