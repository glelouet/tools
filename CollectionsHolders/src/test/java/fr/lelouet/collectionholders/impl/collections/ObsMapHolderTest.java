package fr.lelouet.collectionholders.impl.collections;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.lelouet.collectionholders.impl.ObsObjHolderImpl;
import fr.lelouet.collectionholders.impl.collections.ObsMapHolderImpl;
import fr.lelouet.collectionholders.interfaces.ObsObjHolder;
import fr.lelouet.collectionholders.interfaces.collections.ObsCollectionHolder;
import fr.lelouet.collectionholders.interfaces.collections.ObsMapHolder;
import fr.lelouet.collectionholders.interfaces.collections.ObsSetHolder;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

public class ObsMapHolderTest {

	@Test
	public void testMap() {
		ObservableMap<String, String> source = FXCollections.observableHashMap();
		ObsMapHolderImpl<String, String> sourceimpl = new ObsMapHolderImpl<>(source, true);
		source.put("a", "aa");
		source.put("b", "bb");
		ObsMapHolderImpl<String, String> mapped = ObsMapHolderImpl.map(sourceimpl, s -> "+" + s);
		HashMap<String, String> expected = new HashMap<>();
		expected.put("a", "+aa");
		expected.put("b", "+bb");
		Assert.assertEquals(mapped.copy(), expected);
		source.put("c", "cc");
		Assert.assertEquals(mapped.copy().get("c"), "+cc");
	}

	@Test
	public void testMapreceived() {
		int[] count = new int[] { 0 };
		ObservableMap<String, String> source = FXCollections.observableHashMap();
		ObsMapHolderImpl<String, String> sourceimpl = new ObsMapHolderImpl<>(source);
		Consumer<Map<String, String>> run = (m) -> count[0] += m.size();
		sourceimpl.addReceivedListener(run);
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

	@Test
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
		Assert.assertEquals(merged1.copy().get("key1"), "m1k1");
		Assert.assertEquals(merged1.copy().get("key2"), "m1k2");

		im2.put("key2", "m2k2");
		m2.dataReceived();

		Assert.assertTrue(merged1.isDataReceived());
		Assert.assertEquals(merged1.copy().get("key1"), "m1k1");
		Assert.assertEquals(merged1.copy().get("key2"), "m2k2");

		ObsMapHolderImpl<String, String> merged2 = (ObsMapHolderImpl<String, String>) ObsMapHolderImpl.merge((v1, v2) -> v2,
				m1, m2, m3);
		Assert.assertTrue(merged2.isDataReceived());
		Assert.assertEquals(merged2.copy().get("key1"), "m1k1");
		Assert.assertEquals(merged2.copy().get("key2"), "m2k2");

		ObsMapHolderImpl<String, String> merged3 = (ObsMapHolderImpl<String, String>) ObsMapHolderImpl.merge((v1, v2) -> v2,
				m2, m1, m3);
		Assert.assertTrue(merged3.isDataReceived());
		Assert.assertEquals(merged3.copy().get("key1"), "m1k1");
		Assert.assertEquals(merged3.copy().get("key2"), "m1k2");

		im3.put("key3", "m3k3");
		im3.put("key1", "m3k1");
		m3.dataReceived();
		im2.put("key2", "m2k2");
		m2.dataReceived();
		for (ObsMapHolderImpl<String, String> m : Arrays.asList(merged1, merged2, merged3)) {
			Assert.assertTrue(m.isDataReceived());
			Assert.assertEquals(m.copy().get("key1"), "m3k1");
			Assert.assertEquals(m.copy().get("key2"), "m2k2");
			Assert.assertEquals(m.copy().get("key3"), "m3k3");
		}

	}

	@SuppressWarnings("unchecked")
	@Test
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

		Assert.assertEquals(merged.get("a"), "a", "map is" + merged.copy());
		Assert.assertEquals(merged.get("b"), "ab", "map is" + merged.copy());
		Assert.assertEquals(merged.get("c"), "b", "map is" + merged.copy());

		m1.dataReceived();

		Assert.assertEquals(merged.get("a"), "a");
		Assert.assertEquals(merged.get("b"), "ba");
		Assert.assertEquals(merged.get("c"), "b");

	}

	@Test
	public void testAt() {

		ObservableMap<String, String> imap = FXCollections.observableHashMap();
		ObsMapHolderImpl<String, String> map = new ObsMapHolderImpl<>(imap);

		SimpleObjectProperty<String> ik1 = new SimpleObjectProperty<>();
		ObsObjHolderImpl<String> k1 = new ObsObjHolderImpl<>(ik1);

		String k2 = "k2";

		SimpleObjectProperty<String> ik3 = new SimpleObjectProperty<>("k3");
		ObsObjHolderImpl<String> k3 = new ObsObjHolderImpl<>(ik3);

		ObsObjHolder<String> at1 = map.at(k1);
		ObsObjHolder<String> at2 = map.at(k2);
		ObsObjHolder<String> at3 = map.at(k3);

		ik1.set("k1");

		imap.put("k1", "v1");
		imap.put("k2", "v2");
		imap.put("k3", "v3");
		map.dataReceived();

		Assert.assertEquals(at1.get(), "v1");
		Assert.assertEquals(at2.get(), "v2");
		Assert.assertEquals(at3.get(), "v3");

		ik3.set("k1");
		imap.put("k1", "v1b");
		map.dataReceived();

		Assert.assertEquals(at1.get(), "v1b");
		Assert.assertEquals(at2.get(), "v2");
		Assert.assertEquals(at3.get(), "v1b");

	}

	@Test
	public void testKeyValue() {
		ObservableMap<String, String> imap = FXCollections.observableHashMap();
		ObsMapHolderImpl<String, String> map = new ObsMapHolderImpl<>(imap);

		imap.put("a", "va");

		ObsSetHolder<String> keys = map.keys();
		ObsCollectionHolder<String, ?, ?> values = map.values();

		imap.put("b", "vb");

		map.dataReceived();

		Assert.assertTrue(keys.contains("a").get());
		Assert.assertTrue(keys.contains(new ObsObjHolderImpl<>(new SimpleObjectProperty<>("b"))).get());
		Assert.assertEquals((int) keys.size().get(), 2);

		Assert.assertEquals((int) values.size().get(), 2);

	}

}