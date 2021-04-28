package fr.lelouet.tools.holders.cache;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.lelouet.tools.lambdaref.GCManage;

public class WeakCacheTest {

	@Test
	public void testArrayStore() {
		WeakCache<Integer, Integer[]> squarray = new WeakCache<>(i -> new Integer[] { i });
		Integer[] i2Arr = squarray.get(2);
		Assert.assertEquals(i2Arr[0], (Integer) 2);
		Assert.assertEquals(squarray.size(), 1);
		i2Arr = null;
		GCManage.force();
		Assert.assertEquals(squarray.size(),0);
	}

	@Test
	public void testMultiCreation() {
		int[] count = new int[] { 0 };
		WeakCache<Integer, Object> cache = new WeakCache<>(i -> {
			count[0]++;
			return new Object();
		});
		Object o2 = cache.get(2);
		Assert.assertEquals(cache.size(), 1);
		Assert.assertEquals(count[0], 1);
		Object o2b = cache.get(2);
		Assert.assertEquals(cache.size(), 1);
		Assert.assertEquals(count[0], 1);
		Assert.assertEquals(o2b, o2);

		@SuppressWarnings("unused")
		Object o3 = cache.get(3);
		Assert.assertEquals(cache.size(), 2);
		Assert.assertEquals(count[0], 2);

		o2 = o2b = null;
		GCManage.force();

		Assert.assertEquals(cache.size(), 1);
		Assert.assertEquals(count[0], 2);
	}

}
