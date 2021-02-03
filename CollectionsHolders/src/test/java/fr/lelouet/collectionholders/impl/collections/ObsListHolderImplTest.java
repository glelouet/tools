package fr.lelouet.collectionholders.impl.collections;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.lelouet.collectionholders.interfaces.ObsObjHolder;
import fr.lelouet.collectionholders.interfaces.collections.ObsListHolder;
import fr.lelouet.collectionholders.interfaces.collections.ObsMapHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsDoubleHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsIntHolder;

public class ObsListHolderImplTest {

	@Test(timeOut = 500)
	public void testCreation() {
		@SuppressWarnings("unchecked")
		List<String>[] last = new List[1];
		ObsListHolderImpl<String> test = new ObsListHolderImpl<>();
		test.follow(o -> last[0] = o);

		Assert.assertNull(last[0]);

		test.setEmpty();
		Assert.assertEquals(test.get(), Collections.emptyList());
		Assert.assertEquals(test.size().get(), (Integer) 0);
		Assert.assertTrue(test.isEmpty().get());

		test.set(Arrays.asList("a", "b"));
		Assert.assertNotNull(last[0]);
		Assert.assertEquals(test.get(), Arrays.asList("a", "b"));
		Assert.assertEquals(test.size().get(), (Integer) 2);
		Assert.assertFalse(test.isEmpty().get());

		test.set(null);
		Assert.assertEquals(test.get(), Collections.emptyList());
		Assert.assertEquals(test.size().get(), (Integer) 0);
		Assert.assertTrue(test.isEmpty().get());

		test.set(Arrays.asList("a", "b", "a", "b"));
		Assert.assertNotNull(last[0]);
		Assert.assertEquals(test.get(), Arrays.asList("a", "b", "a", "b"));

	}

	@Test(dependsOnMethods = "testCreation", timeOut = 500)
	public void testMap() {
		ObsListHolderImpl<String> source = ObsListHolderImpl.of("a", "b", "c", "d");
		ObsObjHolder<String> test = source.map(l -> l.stream().collect(Collectors.joining()));
		Assert.assertEquals(test.get(), "abcd");
		source.set("a", "b", "b");
		Assert.assertEquals(test.get(), "abb");
	}

	@Test(dependsOnMethods = "testCreation", timeOut = 500)
	public void testMapItems() {
		ObsListHolderImpl<String> source = ObsListHolderImpl.of("a", "bb", "c", "ddd");
		ObsListHolderImpl<Integer> test = source.mapItems(String::length);
		Assert.assertEquals(test.get(), Arrays.asList(1, 2, 1, 3));
	}

	@Test(dependsOnMethods = "testCreation", timeOut = 500)
	public void testToMap() {
		ObsListHolderImpl<String> source = ObsListHolderImpl.of("a", "bb", "c", "ddd");
		ObsMapHolder<Integer, String> test = source.toMap(String::length);
		Assert.assertEquals(test.get().get(1), "c");
		Assert.assertEquals(test.get().get(2), "bb");
		Assert.assertEquals(test.get().get(3), "ddd");
		Assert.assertEquals(test.get().get(4), null);

	}

	@Test(dependsOnMethods = "testCreation", timeOut = 500)
	public void testFilter() {
		ObsListHolderImpl<String> test = new ObsListHolderImpl<>();
		ObsListHolderImpl<String> filtered = test.filter(s -> s.length() > 1);

		test.set(Arrays.asList("a", "bb", "ccc"));
		Assert.assertEquals(filtered.get(), Arrays.asList("bb", "ccc"));

		ObsListHolderImpl<String> filtered2 = test.filter(s -> s.length() > 1);
		Assert.assertEquals(filtered2.get(), Arrays.asList("bb", "ccc"));

		test.set(Arrays.asList("a", "bb", "ccc", "dddd"));

		Assert.assertEquals(filtered.get(), Arrays.asList("bb", "ccc", "dddd"));
		Assert.assertEquals(filtered2.get(), Arrays.asList("bb", "ccc", "dddd"));
	}

	@Test(dependsOnMethods = "testCreation", timeOut = 500)
	public void testProdList() {
		ObsListHolderImpl<String> test1 = ObsListHolderImpl.of("a", "b");
		ObsListHolderImpl<String> test2 = ObsListHolderImpl.of("1", "2");

		ObsListHolder<Object> prod = test1.prodList(test2, (s1, s2) -> s1 + s2);
		Assert.assertEquals(prod.get(), Arrays.asList("a1", "a2", "b1", "b2"));
	}

	@Test(dependsOnMethods = "testCreation", timeOut = 500)
	public void testSort() {
		ObsListHolderImpl<String> test1 = ObsListHolderImpl.of("d", "a", "c", "b");

		ObsListHolder<String> sorted = test1.sorted(String::compareTo);
		Assert.assertEquals(sorted.get(), Arrays.asList("a", "b", "c", "d"));
	}

	@Test(dependsOnMethods = "testCreation", timeOut = 500)
	public void testReverse() {
		ObsListHolderImpl<String> test1 = ObsListHolderImpl.of("a", "b", "c", "d");

		ObsListHolder<String> reversed = test1.reverse();
		Assert.assertEquals(reversed.get(), Arrays.asList("d", "c", "b", "a"));
	}

	@Test(dependsOnMethods = "testCreation", timeOut = 500)
	public void testReduce() {
		ObsListHolderImpl<String> test1 = ObsListHolderImpl.of("d", "a", "c", "b");

		ObsObjHolder<String> concat = test1.reduce(String::concat, "");
		Assert.assertEquals(concat.get(), "dacb");

		ObsObjHolder<Integer> size = test1.reduce(s -> s.length(), Integer::sum, 0);
		Assert.assertEquals((int) size.get(), 4);

		ObsIntHolder size2 = test1.mapInt(l -> l.stream().mapToInt(String::length).sum());
		Assert.assertEquals((int) size2.get(), 4);

		/**
		 * each string in the list is a vector in a new dimension. compute distance
		 * 2 on the list of vectors
		 */
		ObsDoubleHolder dist = test1
				.mapDouble(l -> Math.sqrt(l.stream().mapToDouble(s -> s.length() * s.length()).sum()));
		Assert.assertEquals((double) dist.get(), 2.0);

		ObsListHolderImpl<String> test2 = new ObsListHolderImpl<>();

		// same but with 4 strings of size 2 (so distance is sqrt(4*2²)=2*2 = 4
		ObsDoubleHolder dist2 = test2
				.mapDouble(l -> Math.sqrt(l.stream().mapToDouble(s -> s.length() * s.length()).sum()));
		test2.set(Arrays.asList("aa", "bb", "cc", "dd"));
		Assert.assertEquals((double) dist2.get(), 4.0);

		ObsIntHolder sumSize = test1.reduceInt(String::length, Integer::sum, 0);
		Assert.assertEquals(sumSize.get(), (Integer) 4);
	}

	@Test(dependsOnMethods = "testCreation", timeOut = 500)
	public void testFilterWhen() {
		ObsListHolderImpl<ObsMapHolder<String, Integer>> source = ObsListHolderImpl.of();

		ObsListHolder<ObsMapHolder<String, Integer>> test = source.filterWhen(m -> m.isEmpty().not());
		Assert.assertTrue(test.isEmpty().get());

		ObsMapHolderImpl<String, Integer> map1 = new ObsMapHolderImpl<>();
		source.set(Arrays.asList(map1));
		Assert.assertTrue(test.isEmpty().get());

		HashMap<String, Integer> mm1 = new HashMap<>();
		mm1.put("a", 1);
		map1.set(mm1);
		Assert.assertFalse(test.isEmpty().get());

		ObsMapHolderImpl<String, Integer> map2 = new ObsMapHolderImpl<>();
		source.set(Arrays.asList(map1, map2));
		Assert.assertEquals(test.get().size(), 1, "got : " + test.get());

		HashMap<String, Integer> mm2 = new HashMap<>();
		mm2.put("bb", 2);
		map2.set(mm2);
		Assert.assertEquals(test.get().size(), 2, "got : " + test.get());

		source.set(Arrays.asList(map2));
		Assert.assertEquals(test.get().size(), 1, "got : " + test.get());

	}

	@SuppressWarnings("unchecked")
	@Test(dependsOnMethods = "testCreation", timeOut = 500)
	public void testConcat() {
		ObsListHolderImpl<String> source1 = ObsListHolderImpl.of("a", "b");
		ObsListHolderImpl<String> source2 = new ObsListHolderImpl<>();
		ObsListHolderImpl<String> test = source1.concat(source2);
		Assert.assertFalse(test.isDataAvailable());
		source2.set("c", "d");
		Assert.assertEquals(test.get(), Arrays.asList("a", "b", "c", "d"), "got" + test.get());
	}

	@Test
	public void testPos() {
		ObsListHolderImpl<String> source = ObsListHolderImpl.of("a", "b");
		Assert.assertEquals(source.pos(0, "").get(), "a");
		Assert.assertEquals(source.pos(1, "").get(), "b");
		Assert.assertEquals(source.pos(2, "").get(), "");
	}

}
