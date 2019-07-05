package fr.lelouet.collectionholders.impl.collections;

import java.util.Arrays;
import java.util.HashSet;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.lelouet.collectionholders.impl.ObsObjHolderImpl;
import fr.lelouet.collectionholders.interfaces.ObsObjHolder;
import fr.lelouet.collectionholders.interfaces.collections.ObsMapHolder;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

public class ObsSetHolderImplTest {

	@Test
	public void testCreating() {
		ObservableSet<String> internal = FXCollections.observableSet(new HashSet<>());
		ObsSetHolderImpl<String> test = new ObsSetHolderImpl<>(internal);

		ObsObjHolder<Boolean> a1 = test.contains("a");
		ObsObjHolder<Boolean> c1 = test.contains("c");
		ObsObjHolder<Boolean> av1 = test.contains(new ObsObjHolderImpl<>(new SimpleObjectProperty<>("a")));
		ObsObjHolder<Boolean> cv1 = test.contains(new ObsObjHolderImpl<>(new SimpleObjectProperty<>("c")));

		internal.add("a");
		internal.add("b");
		test.dataReceived();

		ObsObjHolder<Boolean> a2 = test.contains("a");
		ObsObjHolder<Boolean> c2 = test.contains("c");
		ObsObjHolder<Boolean> av2 = test.contains(new ObsObjHolderImpl<>(new SimpleObjectProperty<>("a")));
		ObsObjHolder<Boolean> cv2 = test.contains(new ObsObjHolderImpl<>(new SimpleObjectProperty<>("c")));

		for (ObsObjHolder<Boolean> a : Arrays.asList(a1, a2, av1, av2)) {
			Assert.assertTrue(a.get());
		}

		for (ObsObjHolder<Boolean> c : Arrays.asList(c1, c2, cv1, cv2)) {
			Assert.assertFalse(c.get());
		}

		internal.remove("a");
		internal.add("c");
		test.dataReceived();

		for (ObsObjHolder<Boolean> a : Arrays.asList(a1, a2, av1, av2)) {
			Assert.assertFalse(a.get());
		}

		for (ObsObjHolder<Boolean> c : Arrays.asList(c1, c2, cv1, cv2)) {
			Assert.assertTrue(c.get());
		}

		internal.add("aa");
		internal.add("ab");
		test.dataReceived();

		ObsSetHolderImpl<String> filtered = test.filter(s -> s.startsWith("a"));
		Assert.assertEquals((int) filtered.size().get(), 2);
		Assert.assertTrue(filtered.contains("aa").get());
		Assert.assertTrue(filtered.contains("ab").get());
	}

	@Test
	public void testFollow() {

		ObservableSet<String> internal = FXCollections.observableSet(new HashSet<>());
		ObsSetHolderImpl<String> test = new ObsSetHolderImpl<>(internal);

		internal.addAll(Arrays.asList("a", "bb", "ccc"));
		test.dataReceived();

		ObsMapHolder<String, Integer> sizes = test.mapItems(s -> s, s -> s.length());
		Assert.assertEquals((int) sizes.get("a"), 1);
	}

}
