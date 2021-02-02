package fr.lelouet.collectionholders.impl.collections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.lelouet.collectionholders.impl.ObsObjHolderSimple;
import fr.lelouet.collectionholders.interfaces.ObsObjHolder;
import fr.lelouet.collectionholders.interfaces.collections.ObsCollectionHolder;
import fr.lelouet.collectionholders.interfaces.collections.ObsMapHolder;
import fr.lelouet.collectionholders.interfaces.collections.ObsSetHolder;

@Test(timeOut = 500)
public class ObsMapHolderTest {

	@Test(timeOut = 500)
	public void testMap() {
		Map<String, String> data = new HashMap<>();
		data.put("a", "aa");
		data.put("b", "bb");
		ObsMapHolderImpl<String, String> sourceimpl = new ObsMapHolderImpl<>(data);

		ObsMapHolder<String, Object> mapped = sourceimpl.mapValues(s -> "+" + s);
		HashMap<String, String> expected = new HashMap<>();
		expected.put("a", "+aa");
		expected.put("b", "+bb");
		Assert.assertEquals(mapped.get(), expected);
		data = new HashMap<>();
		data.put("c", "cc");
		sourceimpl.set(data);
		Assert.assertEquals(mapped.get().get("c"), "+cc");
	}

	@SuppressWarnings("unchecked")
	@Test(timeOut = 500)
	public void testMerge2() {
		Map<String, String> im1 = new HashMap<>();
		im1.put("a", "a");
		im1.put("b", "a");
		ObsMapHolderImpl<String, String> m1 = new ObsMapHolderImpl<>(im1);

		Map<String, String> im2 = new HashMap<>();
		im2.put("b", "b");
		im2.put("c", "b");
		ObsMapHolderImpl<String, String> m2 = new ObsMapHolderImpl<>(im2);

		ObsMapHolder<String, String> merged = m1.merge((a, b) -> a + b, m2);

		Assert.assertEquals(merged.get().get("a"), "a");
		Assert.assertEquals(merged.get().get("b"), "ab");
		Assert.assertEquals(merged.get().get("c"), "b");

	}

	@Test(timeOut = 500)
	public void testAt() {
		Map<String, String> im = new HashMap<>();
		im.put("k1", "v1");
		im.put("k2", "v2");
		im.put("k3", "v3");
		ObsMapHolderImpl<String, String> map = new ObsMapHolderImpl<>(im);

		ObsObjHolderSimple<String> k1 = new ObsObjHolderSimple<>();
		String k2 = "k2";
		ObsObjHolderSimple<String> k3 = new ObsObjHolderSimple<>("k3");

		ObsObjHolder<String> at1 = map.at(k1, "");
		ObsObjHolder<String> at2 = map.at(k2, "");
		ObsObjHolder<String> at3 = map.at(k3, "");

		k1.set("k1");

		Assert.assertEquals(at1.get(), "v1");
		Assert.assertEquals(at2.get(), "v2");
		Assert.assertEquals(at3.get(), "v3");
	}

	@Test(timeOut = 500)
	public void testKeyValue() {
		Map<String, String> imap = new HashMap<>();
		imap.put("a", "va");
		imap.put("b", "vb");
		ObsMapHolderImpl<String, String> map = new ObsMapHolderImpl<>(imap);
		ObsSetHolder<String> keys = map.keys();
		ObsCollectionHolder<String, ?> values = map.values();

		Assert.assertTrue(keys.contains("a").get());
		Assert.assertTrue(keys.contains(new ObsObjHolderSimple<>("b")).get());
		Assert.assertEquals((int) keys.size().get(), 2);

		Assert.assertEquals((int) values.size().get(), 2);

	}

	@Test(timeOut = 500)
	public void testOf() {
		String[][] map = { { "a", "aa" }, { "b", "bb" } };
		ObsMapHolderImpl<String, String> test = ObsMapHolderImpl.of(map);
		Assert.assertEquals(test.get().get("a"), "aa");
		Assert.assertEquals(test.get().get("b"), "bb");
		Assert.assertEquals(test.at("a", "n").get(), "aa");
		Assert.assertEquals(test.at("b", "n").get(), "bb");
	}

	@Test(timeOut = 500)
	public void testFilterKeys() {
		Map<String, String> imap = new HashMap<>();
		ObsMapHolderImpl<String, String> map = new ObsMapHolderImpl<>(imap);

		List<String> ilist = new ArrayList<>();
		ObsListHolderImpl<String> list = new ObsListHolderImpl<>(ilist);

		ObsMapHolder<String, String> test = map.filterKeys(list);

		Assert.assertEquals(test.size().get(), (Integer)0);

		ilist = new ArrayList<>();
		ilist.add("a1");
		ilist.add("a2");
		list.set(ilist);
		Assert.assertEquals(test.size().get(), (Integer) 0);

		imap = new HashMap<>();
		imap.put("a1", "aa");
		imap.put("b1", "bb");
		map.set(imap);
		Assert.assertEquals(test.size().get(), (Integer) 1);
		Assert.assertEquals(test.get().get("a1"), "aa");
		Assert.assertEquals(test.get().get("a2"), null);
		Assert.assertEquals(test.get().get("b1"), null);
	}

	@Test(timeOut = 500)
	public void testContainsKey() {
		Map<String, String> imap = new HashMap<>();
		imap.put("a", "aaa");
		imap.put("b", "bbb");
		ObsMapHolderImpl<String, String> map = new ObsMapHolderImpl<>(imap);
		Assert.assertTrue(map.containsKey("a").get());
		Assert.assertFalse(map.containsKey("c").get());

	}

}
