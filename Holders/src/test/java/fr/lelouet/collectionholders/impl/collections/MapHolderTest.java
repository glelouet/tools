package fr.lelouet.collectionholders.impl.collections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.lelouet.collectionholders.impl.ObjHolderSimple;
import fr.lelouet.collectionholders.interfaces.ObjHolder;
import fr.lelouet.collectionholders.interfaces.collections.CollectionHolder;
import fr.lelouet.collectionholders.interfaces.collections.MapHolder;
import fr.lelouet.collectionholders.interfaces.collections.SetHolder;

@Test(timeOut = 500)
public class MapHolderTest {

	@Test(timeOut = 500)
	public void testMap() {
		Map<String, String> data = new HashMap<>();
		data.put("a", "aa");
		data.put("b", "bb");
		MapHolderImpl<String, String> sourceimpl = new MapHolderImpl<>(data);

		MapHolder<String, Object> mapped = sourceimpl.mapValues(s -> "+" + s);
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
		MapHolderImpl<String, String> m1 = new MapHolderImpl<>(im1);

		Map<String, String> im2 = new HashMap<>();
		im2.put("b", "b");
		im2.put("c", "b");
		MapHolderImpl<String, String> m2 = new MapHolderImpl<>(im2);

		MapHolder<String, String> merged = m1.merge((a, b) -> a + b, m2);

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
		MapHolderImpl<String, String> map = new MapHolderImpl<>(im);

		ObjHolderSimple<String> k1 = new ObjHolderSimple<>();
		String k2 = "k2";
		ObjHolderSimple<String> k3 = new ObjHolderSimple<>("k3");

		ObjHolder<String> at1 = map.at(k1, "");
		ObjHolder<String> at2 = map.at(k2, "");
		ObjHolder<String> at3 = map.at(k3, "");

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
		MapHolderImpl<String, String> map = new MapHolderImpl<>(imap);
		SetHolder<String> keys = map.keys();
		CollectionHolder<String, ?> values = map.values();

		Assert.assertTrue(keys.contains("a").get());
		Assert.assertTrue(keys.contains(new ObjHolderSimple<>("b")).get());
		Assert.assertEquals((int) keys.size().get(), 2);

		Assert.assertEquals((int) values.size().get(), 2);

	}

	@Test(timeOut = 500)
	public void testOf() {
		String[][] map = { { "a", "aa" }, { "b", "bb" } };
		MapHolderImpl<String, String> test = MapHolderImpl.of(map);
		Assert.assertEquals(test.get().get("a"), "aa");
		Assert.assertEquals(test.get().get("b"), "bb");
		Assert.assertEquals(test.at("a", "n").get(), "aa");
		Assert.assertEquals(test.at("b", "n").get(), "bb");
	}

	@Test(timeOut = 500)
	public void testFilterKeys() {
		Map<String, String> imap = new HashMap<>();
		MapHolderImpl<String, String> map = new MapHolderImpl<>(imap);

		List<String> ilist = new ArrayList<>();
		ListHolderImpl<String> list = new ListHolderImpl<>(ilist);

		MapHolder<String, String> test = map.filterKeys(list);

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
		MapHolderImpl<String, String> map = new MapHolderImpl<>(imap);
		Assert.assertTrue(map.containsKey("a").get());
		Assert.assertFalse(map.containsKey("c").get());

	}

}
