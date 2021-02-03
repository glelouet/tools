package fr.lelouet.collectionholders.impl.collections;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.lelouet.collectionholders.impl.ObsObjHolderSimple;
import fr.lelouet.collectionholders.impl.numbers.ObsBoolHolderImpl;
import fr.lelouet.collectionholders.interfaces.ObsObjHolder;
import fr.lelouet.collectionholders.interfaces.collections.ObsMapHolder;

public class ObsSetHolderImplTest {

	@Test(timeOut = 500)
	public void testCreating() {
		ObsSetHolderImpl<String> test = new ObsSetHolderImpl<>();

		ObsObjHolder<Boolean> a1 = test.contains("a");
		ObsObjHolder<Boolean> c1 = test.contains("c");
		ObsObjHolder<Boolean> av1 = test.contains(new ObsObjHolderSimple<>("a"));
		ObsObjHolder<Boolean> cv1 = test.contains(new ObsObjHolderSimple<>("c"));

		Set<String> internal = new HashSet<>();
		internal.add("a");
		internal.add("b");
		test.set(internal);

		ObsObjHolder<Boolean> a2 = test.contains("a");
		ObsObjHolder<Boolean> c2 = test.contains("c");
		ObsObjHolder<Boolean> av2 = test.contains(new ObsObjHolderSimple<>("a"));
		ObsObjHolder<Boolean> cv2 = test.contains(new ObsObjHolderSimple<>("c"));

		for (ObsObjHolder<Boolean> a : Arrays.asList(a1, a2, av1, av2)) {
			Assert.assertTrue(a.get());
		}

		for (ObsObjHolder<Boolean> c : Arrays.asList(c1, c2, cv1, cv2)) {
			Assert.assertFalse(c.get());
		}
	}

	@Test(timeOut = 500)
	public void testFollow() {
		ObsSetHolderImpl<String> test = ObsSetHolderImpl.of("a", "bb", "ccc");

		ObsMapHolder<String, Integer> sizes = test.toMap(s -> s, s -> s.length());
		Assert.assertEquals((int) sizes.get().get("a"), 1);
	}

	@Test(timeOut = 500)
	public void testFilter() {
		ObsSetHolderImpl<String> test = ObsSetHolderImpl.of(null, null);
		ObsSetHolderImpl<String> nonNull = test.filter(s -> s != null);
		Assert.assertEquals(nonNull.get(), Collections.emptySet());
		test.setEmpty();
		Assert.assertEquals(nonNull.get(), Collections.emptySet());
		test.set("a", "b");
		Assert.assertEquals(nonNull.get(), new HashSet<>(Arrays.asList("a", "b")));
	}

	@Test(timeOut = 500)
	public void testFilterWhen() {
		ObsSetHolderImpl<String> test = ObsSetHolderImpl.of("a", "b");
		ObsBoolHolderImpl ret = new ObsBoolHolderImpl(false);
		ObsSetHolderImpl<String> accepted = test.filterWhen(s -> ret);
		Assert.assertEquals(accepted.get(), Collections.emptySet());
		ret.set(true);
		Assert.assertEquals(accepted.get(), new HashSet<>(Arrays.asList("a", "b")));
		test.setEmpty();
		Assert.assertEquals(accepted.get(), Collections.emptySet());
		ret.set(false);
		Assert.assertEquals(accepted.get(), Collections.emptySet());
	}

}
