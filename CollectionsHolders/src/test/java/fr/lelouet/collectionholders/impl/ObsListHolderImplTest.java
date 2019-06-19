package fr.lelouet.collectionholders.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.lelouet.collectionholders.interfaces.ObsMapHolder;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public class ObsListHolderImplTest {

	@Test
	public void testCreation() {
		ObservableList<String> internal = FXCollections.observableArrayList();
		@SuppressWarnings("unchecked")
		List<String>[] last = new List[1];
		ArrayList<ListChangeListener.Change<? extends String>> modifs = new ArrayList<>();
		ObsListHolderImpl<String> test = new ObsListHolderImpl<>(internal);
		test.addReceivedListener(o -> last[0] = o);
		test.follow(l -> modifs.add(l));

		Assert.assertNull(last[0]);
		Assert.assertEquals(modifs.size(), 0);

		internal.addAll("a", "b");
		Assert.assertNull(last[0]);
		Assert.assertEquals(modifs.size(), 1);
		test.dataReceived();
		Assert.assertNotNull(last[0]);
		Assert.assertEquals(modifs.size(), 1);
		Assert.assertEquals(test.copy(), Arrays.asList("a", "b"));

		internal.addAll("a", "b");
		test.dataReceived();
		Assert.assertNotNull(last[0]);
		Assert.assertEquals(modifs.size(), 2);
		Assert.assertEquals(test.copy(), Arrays.asList("a", "b", "a", "b"));

	}

	@Test(dependsOnMethods = "testCreation")
	public void testMap() {
		ObservableList<String> internal = FXCollections.observableArrayList();
		ObsListHolderImpl<String> test = new ObsListHolderImpl<>(internal);
		ObsMapHolder<Integer, String> map1 = test.map(s -> s.length());
		ObsMapHolder<String, Integer> map2 = test.map(s -> s, s -> s.length());

		internal.addAll("a", "bb");
		test.dataReceived();

		ObsMapHolder<Integer, String> map1l = test.map(s -> s.length());
		ObsMapHolder<String, Integer> map2l = test.map(s -> s, s -> s.length());

		Assert.assertEquals(map1.get(1), "a");
		Assert.assertEquals(map1.get(2), "bb");

		Assert.assertEquals(map1l.get(1), "a");
		Assert.assertEquals(map1l.get(2), "bb");

		Assert.assertEquals(map2.get("a"), (Integer) 1);
		Assert.assertEquals(map2.get("bb"), (Integer) 2);

		Assert.assertEquals(map2l.get("a"), (Integer) 1);
		Assert.assertEquals(map2l.get("bb"), (Integer) 2);

		internal.addAll("c", "ddd");

		Assert.assertEquals(map1.get(1), "c");
		Assert.assertEquals(map1.get(2), "bb");
		Assert.assertEquals(map1.get(3), "ddd");

		Assert.assertEquals(map1l.get(1), "c");
		Assert.assertEquals(map1l.get(2), "bb");
		Assert.assertEquals(map1l.get(3), "ddd");

		Assert.assertEquals(map2.get("a"), (Integer) 1);
		Assert.assertEquals(map2.get("bb"), (Integer) 2);
		Assert.assertEquals(map2.get("c"), (Integer) 1);
		Assert.assertEquals(map2.get("ddd"), (Integer) 3);

		Assert.assertEquals(map2l.get("a"), (Integer) 1);
		Assert.assertEquals(map2l.get("bb"), (Integer) 2);
		Assert.assertEquals(map2l.get("c"), (Integer) 1);
		Assert.assertEquals(map2l.get("ddd"), (Integer) 3);
	}

	@Test(dependsOnMethods = "testCreation")
	public void testFilter() {
		ObservableList<String> internal = FXCollections.observableArrayList();
		ObsListHolderImpl<String> test = new ObsListHolderImpl<>(internal);
		ObsListHolderImpl<String> filtered = test.filter(s -> s.length() > 1);

		internal.addAll("a", "bb", "ccc");
		test.dataReceived();

		Assert.assertEquals(filtered.copy(), Arrays.asList("bb", "ccc"));

		ObsListHolderImpl<String> filtered2 = test.filter(s -> s.length() > 1);
		Assert.assertEquals(filtered2.copy(), Arrays.asList("bb", "ccc"));

		internal.addAll("dddd");
		test.dataReceived();
		Assert.assertEquals(filtered.copy(), Arrays.asList("bb", "ccc", "dddd"));
		Assert.assertEquals(filtered2.copy(), Arrays.asList("bb", "ccc", "dddd"));

	}
}
