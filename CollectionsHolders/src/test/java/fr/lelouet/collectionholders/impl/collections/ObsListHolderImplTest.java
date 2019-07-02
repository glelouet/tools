package fr.lelouet.collectionholders.impl.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.lelouet.collectionholders.interfaces.collections.ObsListHolder;
import fr.lelouet.collectionholders.interfaces.collections.ObsMapHolder;
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
		ObsMapHolder<Integer, String> mapSize2String = test.map(s -> s.length());
		ObsMapHolder<String, Integer> mapString2Size = test.map(s -> s, s -> s.length());

		internal.addAll("a", "bb");
		test.dataReceived();

		ObsMapHolder<Integer, String> mapSize2StringLate = test.map(s -> s.length());
		ObsMapHolder<String, Integer> mapString2SizeLate = test.map(s -> s, s -> s.length());

		Assert.assertEquals(mapSize2String.get(1), "a");
		Assert.assertEquals(mapSize2String.get(2), "bb");

		Assert.assertEquals(mapSize2StringLate.get(1), "a");
		Assert.assertEquals(mapSize2StringLate.get(2), "bb");

		Assert.assertEquals(mapString2Size.get("a"), (Integer) 1);
		Assert.assertEquals(mapString2Size.get("bb"), (Integer) 2);

		Assert.assertEquals(mapString2SizeLate.get("a"), (Integer) 1);
		Assert.assertEquals(mapString2SizeLate.get("bb"), (Integer) 2);

		internal.addAll("c", "ddd");
		test.dataReceived();

		Assert.assertEquals(mapSize2String.get(1), "c");
		Assert.assertEquals(mapSize2String.get(2), "bb");
		Assert.assertEquals(mapSize2String.get(3), "ddd");

		Assert.assertEquals(mapSize2StringLate.get(1), "c");
		Assert.assertEquals(mapSize2StringLate.get(2), "bb");
		Assert.assertEquals(mapSize2StringLate.get(3), "ddd");

		Assert.assertEquals(mapString2Size.get("a"), (Integer) 1);
		Assert.assertEquals(mapString2Size.get("bb"), (Integer) 2);
		Assert.assertEquals(mapString2Size.get("c"), (Integer) 1);
		Assert.assertEquals(mapString2Size.get("ddd"), (Integer) 3);

		Assert.assertEquals(mapString2SizeLate.get("a"), (Integer) 1);
		Assert.assertEquals(mapString2SizeLate.get("bb"), (Integer) 2);
		Assert.assertEquals(mapString2SizeLate.get("c"), (Integer) 1);
		Assert.assertEquals(mapString2SizeLate.get("ddd"), (Integer) 3);
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

	@Test
	public void testProdList() {
		ObservableList<String> internal1 = FXCollections.observableArrayList();
		ObsListHolderImpl<String> test1 = new ObsListHolderImpl<>(internal1);
		internal1.addAll("a", "b");
		test1.dataReceived();

		ObservableList<String> internal2 = FXCollections.observableArrayList();
		ObsListHolderImpl<String> test2 = new ObsListHolderImpl<>(internal2);
		internal2.addAll("1", "2");
		test2.dataReceived();

		ObsListHolder<Object> prod = test1.prodList(test2, (s1, s2) -> s1 + s2);
		Assert.assertEquals(prod.copy(), Arrays.asList("a1", "a2", "b1", "b2"));
	}

	@Test
	public void testSort() {
		ObservableList<String> internal1 = FXCollections.observableArrayList();
		ObsListHolderImpl<String> test1 = new ObsListHolderImpl<>(internal1);
		internal1.addAll("d", "a", "c", "b");
		test1.dataReceived();

		ObsListHolder<String> sorted = test1.sorted(String::compareTo);
		Assert.assertEquals(sorted.copy(), Arrays.asList("a", "b", "c", "d"));
	}

	@Test
	public void testReverse() {
		ObservableList<String> internal1 = FXCollections.observableArrayList();
		ObsListHolderImpl<String> test1 = new ObsListHolderImpl<>(internal1);
		internal1.addAll("a", "b", "c", "d");
		test1.dataReceived();

		ObsListHolder<String> reversed = test1.reverse();
		Assert.assertEquals(reversed.copy(), Arrays.asList("d", "c", "b", "a"));

	}
}
