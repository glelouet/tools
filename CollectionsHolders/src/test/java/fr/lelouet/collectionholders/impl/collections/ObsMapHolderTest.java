package fr.lelouet.collectionholders.impl.collections;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.lelouet.collectionholders.impl.ObsObjHolderSimple;
import fr.lelouet.collectionholders.interfaces.ObsObjHolder;
import fr.lelouet.collectionholders.interfaces.collections.ObsCollectionHolder;
import fr.lelouet.collectionholders.interfaces.collections.ObsMapHolder;
import fr.lelouet.collectionholders.interfaces.collections.ObsSetHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsIntHolder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

@Test(timeOut = 1)
public class ObsMapHolderTest {

	@Test(timeOut = 500)
	public void testMap() {
		ObservableMap<String, String> source = FXCollections.observableHashMap();
		ObsMapHolderImpl<String, String> sourceimpl = new ObsMapHolderImpl<>(source, true);
		source.put("a", "aa");
		source.put("b", "bb");
		ObsMapHolderImpl<String, String> mapped = ObsMapHolderImpl.map(sourceimpl, s -> "+" + s);
		HashMap<String, String> expected = new HashMap<>();
		expected.put("a", "+aa");
		expected.put("b", "+bb");
		Assert.assertEquals(mapped.get(), expected);
		source.put("c", "cc");
		Assert.assertEquals(mapped.get().get("c"), "+cc");
	}

	@Test(timeOut = 500)
	public void testMapreceived() {
		int[] count = new int[] { 0 };
		ObservableMap<String, String> source = FXCollections.observableHashMap();
		ObsMapHolderImpl<String, String> sourceimpl = new ObsMapHolderImpl<>(source);
		Consumer<Map<String, String>> run = (m) -> count[0] += m.size();
		sourceimpl.follow(run);
		source.put("a", "b");
		sourceimpl.dataReceived();
		Assert.assertEquals(count[0], 1);
		sourceimpl.dataReceived();
		Assert.assertEquals(count[0], 2);
		source.put("a2", "c");
		sourceimpl.dataReceived();
		Assert.assertEquals(count[0], 4);
		Stream.of('a', 'b', 'c').parallel().mapToInt(c -> Character.digit(c, 10)).min().orElseGet(() -> Integer.MAX_VALUE);
	}

	@Test(timeOut = 500)
	public void testReceivedPreFollow() {
		ObservableMap<String, String> source = FXCollections.observableHashMap();
		ObsMapHolderImpl<String, String> sourceimpl = new ObsMapHolderImpl<>(source);
		source.put("a", "b");
		sourceimpl.dataReceived();
		ObsIntHolder size = sourceimpl.size();
		Assert.assertEquals(size.get(), (Integer) 1);
	}

	@Test(timeOut = 500)
	public void testReceivedPostFollow() {
		ObservableMap<String, String> source = FXCollections.observableHashMap();
		ObsMapHolderImpl<String, String> sourceimpl = new ObsMapHolderImpl<>(source);
		ObsIntHolder size = sourceimpl.size();
		source.put("a", "b");
		sourceimpl.dataReceived();
		Assert.assertEquals(size.get(), (Integer) 1);
	}

	@Test(timeOut = 500)
	public void testMerge() {
		ObservableMap<String, String> im1 = FXCollections.observableHashMap();
		ObsMapHolderImpl<String, String> m1 = new ObsMapHolderImpl<>(im1);
		ObservableMap<String, String> im2 = FXCollections.observableHashMap();
		ObsMapHolderImpl<String, String> m2 = new ObsMapHolderImpl<>(im2);
		ObservableMap<String, String> im3 = FXCollections.observableHashMap();
		ObsMapHolderImpl<String, String> m3 = new ObsMapHolderImpl<>(im3);

		im1.put("key1", "m1k1");
		im1.put("key2", "m1k2");

		ObsMapHolderImpl<String, String> merged1 = (ObsMapHolderImpl<String, String>) ObsMapHolderImpl.merge((v1, v2) -> v2,
				m1, m2, m3);
		Assert.assertFalse(merged1.isDataReceived());

		m1.dataReceived();
		m2.dataReceived();
		m3.dataReceived();

		Assert.assertTrue(merged1.isDataReceived());
		Assert.assertEquals(merged1.get().get("key1"), "m1k1");
		Assert.assertEquals(merged1.get().get("key2"), "m1k2");

		im2.put("key2", "m2k2");
		m2.dataReceived();

		Assert.assertTrue(merged1.isDataReceived());
		Assert.assertEquals(merged1.get().get("key1"), "m1k1");
		Assert.assertEquals(merged1.get().get("key2"), "m2k2");

		ObsMapHolderImpl<String, String> merged2 = (ObsMapHolderImpl<String, String>) ObsMapHolderImpl.merge((v1, v2) -> v2,
				m1, m2, m3);
		Assert.assertTrue(merged2.isDataReceived());
		Assert.assertEquals(merged2.get().get("key1"), "m1k1");
		Assert.assertEquals(merged2.get().get("key2"), "m2k2");

		ObsMapHolderImpl<String, String> merged3 = (ObsMapHolderImpl<String, String>) ObsMapHolderImpl.merge((v1, v2) -> v2,
				m2, m1, m3);
		Assert.assertTrue(merged3.isDataReceived());
		Assert.assertEquals(merged3.get().get("key1"), "m1k1");
		Assert.assertEquals(merged3.get().get("key2"), "m1k2");

		im3.put("key3", "m3k3");
		im3.put("key1", "m3k1");
		m3.dataReceived();
		im2.put("key2", "m2k2");
		m2.dataReceived();
		for (ObsMapHolderImpl<String, String> m : Arrays.asList(merged1, merged2, merged3)) {
			Assert.assertTrue(m.isDataReceived());
			Assert.assertEquals(m.get().get("key1"), "m3k1");
			Assert.assertEquals(m.get().get("key2"), "m2k2");
			Assert.assertEquals(m.get().get("key3"), "m3k3");
		}

	}

	@SuppressWarnings("unchecked")
	@Test(timeOut = 500)
	public void testMerge2() {

		ObservableMap<String, String> im1 = FXCollections.observableHashMap();
		ObsMapHolderImpl<String, String> m1 = new ObsMapHolderImpl<>(im1);
		ObservableMap<String, String> im2 = FXCollections.observableHashMap();
		ObsMapHolderImpl<String, String> m2 = new ObsMapHolderImpl<>(im2);

		im1.put("a", "a");
		im1.put("b", "a");
		im2.put("b", "b");
		im2.put("c", "b");

		m2.dataReceived();
		m1.dataReceived();

		ObsMapHolder<String, String> merged = m1.merge((a, b) -> a + b, m2);

		Assert.assertEquals(merged.get("a"), "a", "map is" + merged.get());
		Assert.assertEquals(merged.get("b"), "ab", "map is" + merged.get());
		Assert.assertEquals(merged.get("c"), "b", "map is" + merged.get());

		m1.dataReceived();

		Assert.assertEquals(merged.get("a"), "a");
		Assert.assertEquals(merged.get("b"), "ba");
		Assert.assertEquals(merged.get("c"), "b");

	}

	@Test(timeOut = 500)
	public void testAt() {

		ObservableMap<String, String> imap = FXCollections.observableHashMap();
		ObsMapHolderImpl<String, String> map = new ObsMapHolderImpl<>(imap);

		ObsObjHolderSimple<String> k1 = new ObsObjHolderSimple<>();
		String k2 = "k2";
		ObsObjHolderSimple<String> k3 = new ObsObjHolderSimple<>("k3");

		ObsObjHolder<String> at1 = map.at(k1, "");
		ObsObjHolder<String> at2 = map.at(k2, "");
		ObsObjHolder<String> at3 = map.at(k3, "");

		k1.set("k1");

		imap.put("k1", "v1");
		imap.put("k2", "v2");
		imap.put("k3", "v3");

		map.dataReceived();

		Assert.assertEquals(at1.get(), "v1");
		Assert.assertEquals(at2.get(), "v2");
		Assert.assertEquals(at3.get(), "v3");

		k3.set("k1");
		imap.put("k1", "v1b");
		map.dataReceived();

		Assert.assertEquals(at1.get(), "v1b");
		Assert.assertEquals(at2.get(), "v2");
		Assert.assertEquals(at3.get(), "v1b");

	}

	@Test(timeOut = 500)
	public void testKeyValue() {
		ObservableMap<String, String> imap = FXCollections.observableHashMap();
		ObsMapHolderImpl<String, String> map = new ObsMapHolderImpl<>(imap);

		imap.put("a", "va");

		ObsSetHolder<String> keys = map.keys();
		ObsCollectionHolder<String, ?, ?> values = map.values();

		imap.put("b", "vb");

		map.dataReceived();

		Assert.assertTrue(keys.contains("a").get());
		Assert.assertTrue(keys.contains(new ObsObjHolderSimple<>("b")).get());
		Assert.assertEquals((int) keys.size().get(), 2);

		Assert.assertEquals((int) values.size().get(), 2);

	}

	@Test(timeOut = 500)
	public void testOf() {
		String[][] map = { { "a", "aa" }, { "b", "bb" } };
		ObsMapHolderImpl<String, String> test = ObsMapHolderImpl.of(map);
		Assert.assertEquals(test.get("a"), "aa");
		Assert.assertEquals(test.get("b"), "bb");
		Assert.assertEquals(test.at("a", "n").get(), "aa");
		Assert.assertEquals(test.at("b", "n").get(), "bb");
	}

	@Test(timeOut = 500)
	public void testFilterKeys() {
		ObservableMap<String, String> imap = FXCollections.observableHashMap();
		ObsMapHolderImpl<String, String> map = new ObsMapHolderImpl<>(imap);

		ObservableList<String> ilist = FXCollections.observableArrayList();
		ObsListHolderImpl<String> list = new ObsListHolderImpl<>(ilist);

		ObsMapHolder<String, String> test = map.filterKeys(list);

		map.dataReceived();list.dataReceived();
		Assert.assertEquals(test.size().get(), (Integer)0);

		ilist.add("a1");
		ilist.add("a2");
		list.dataReceived();
		Assert.assertEquals(test.size().get(), (Integer) 0);

		imap.put("a1", "aa");
		imap.put("b1", "bb");
		map.dataReceived();
		Assert.assertEquals(test.size().get(), (Integer) 1);
		Assert.assertEquals(test.get("a1"), "aa");
		Assert.assertEquals(test.get("a2"), null);
		Assert.assertEquals(test.get("b1"), null);

		imap.put("a2", "ab");
		map.dataReceived();
		ilist.remove("a1");
		list.dataReceived();
		Assert.assertEquals(test.size().get(), (Integer) 1);
		Assert.assertEquals(test.get("a1"), null);
		Assert.assertEquals(test.get("a2"), "ab");
		Assert.assertEquals(test.get("b1"), null);
	}

	@Test(timeOut = 500)
	public void testFlatten() {
		ObsMapHolderImpl<Integer, ObsMapHolderImpl<String, String>> access = new ObsMapHolderImpl<>();
		ObsSetHolder<String> allVals = access.values().flatten(m->m.values()).distinct();
		boolean[] received= {false};
		Assert.assertFalse(received[0]);
		allVals.peek(o -> received[0] = true);
		access.underlying().put(0, new ObsMapHolderImpl<>());
		access.underlying().put(1, new ObsMapHolderImpl<>());
		access.dataReceived();
		Assert.assertFalse(received[0]);
		access.underlying().get(0).underlying().put("a", "a0");
		access.underlying().get(0).dataReceived();
		access.underlying().get(1).dataReceived();
		Assert.assertTrue(received[0]);
		Assert.assertTrue(allVals.contains("a0").get());
		access.underlying().get(0).underlying().put("b", "b0");
		access.underlying().get(0).dataReceived();
		Assert.assertTrue(allVals.contains("b0").get());
		access.underlying().get(1).underlying().put("a", "a1");
		access.underlying().get(1).dataReceived();
		Assert.assertTrue(allVals.contains("a1").get());
	}

	private static class Flatten2Asset {
		public final long location;
		public final int typeid;
		public final long qtty;

		public Flatten2Asset(long location, int typeid, long qtty) {
			this.location = location;
			this.typeid = typeid;
			this.qtty = qtty;
		}

	}

	private static class Flatten2Account {
		public final ObsListHolderImpl<Flatten2Asset> assetsHolder = new ObsListHolderImpl<>();
		public final ObsMapHolder<Long, Map<Integer, Long>> assetsByLoc = assetsHolder.toMap(a -> a.location, a -> {
			HashMap<Integer, Long> ret = new HashMap<>();
			ret.put(a.typeid, ret.getOrDefault(a.typeid, 0l) + a.qtty);
			return ret;
		}, (m1, m2) -> {
			for (Entry<Integer, Long> e : m2.entrySet()) {
				m1.put(e.getKey(), m1.getOrDefault(e.getKey(), 0l) + e.getValue());
			}
			return m1;
		});

		public void addType(long location, int type_id, long qtty) {
			assetsHolder.underlying.add(new Flatten2Asset(location, type_id, qtty));
			assetsHolder.dataReceived();
		}
	}

	/**
	 * complex test. Several accounts have each their cached list of assets. Each
	 * assets is a location id a type id, a quantity.<br />
	 * the mapping get all the assets of the accounts, regroup them by location,
	 * then filter the locations from a set of allowed locations,
	 */
	@SuppressWarnings("unchecked")
	@Test(timeOut = 500)
	public void testFlatten2() {
		// account ids are 1-9 ; location ids are 11-19 ; type ids are 101-109
		int ACC1 = 1;
		long LOC1 = 11;
		long LOC2 = 12;
		int TYPE1 = 101;
		int TYPE2 = 102;

		ObsMapHolderImpl<Integer, Flatten2Account> accounts = new ObsMapHolderImpl<>();
		accounts.underlying().put(ACC1, new Flatten2Account());
		/** all the assets of all the accounts */
		ObsMapHolder<Long, Map<Integer, Long>> fullAssets = ((ObsListHolderImpl<Flatten2Account>) accounts.values())
				.flatten(acc -> acc.assetsByLoc.entries())
				.toMap(
						e -> e.getKey(), e -> e.getValue(),
						(m1, m2) -> {Map<Integer, Long> ret = new HashMap<>(m1);
						for (Entry<Integer, Long> e : m2.entrySet()) {
							ret.put(e.getKey(), e.getValue() + ret.getOrDefault(e.getKey(), 0l));
						}
						return ret;
						});
		ObsSetHolderImpl<Long> allowedLocation = new ObsSetHolderImpl<>();
		ObsMapHolder<Integer, Long> allowedAssets = fullAssets.filterKeys(allowedLocation).values()
				.toList(maps -> maps.stream().flatMap(map -> map.entrySet().stream()).collect(Collectors.toList()))
				.toMap(Entry::getKey, Entry::getValue, Long::sum);

		accounts.dataReceived();
		accounts.get(ACC1).addType(LOC1, TYPE1, 10l);

		allowedLocation.underlying().add(LOC1);
		allowedLocation.dataReceived();

		Assert.assertEquals(allowedAssets.get(TYPE1), (Long) 10l);

		accounts.get(ACC1).addType(LOC1, TYPE2, 1l);
		Assert.assertEquals(allowedAssets.get(TYPE2), (Long) 1l);

		accounts.get(ACC1).addType(LOC2, TYPE2, 1);
		Assert.assertEquals(allowedAssets.get(TYPE2), (Long) 1l);

		allowedLocation.underlying().add(LOC2);
		allowedLocation.dataReceived();
		Assert.assertEquals(allowedAssets.get(TYPE2), (Long) 2l);

		accounts.get(ACC1).addType(LOC1, TYPE2, 2l);
		Assert.assertEquals(allowedAssets.get(TYPE2), (Long) 4l);

	}

}
