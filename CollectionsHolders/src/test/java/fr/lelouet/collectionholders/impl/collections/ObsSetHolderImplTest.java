package fr.lelouet.collectionholders.impl.collections;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.lelouet.collectionholders.impl.ObsObjHolderSimple;
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

		System.err.println("made tests");

		a1.follow(b -> System.err.println("received a1 " + b));
		c1.follow(b -> System.err.println("received c1 " + b));
		av1.follow(b -> System.err.println("received av1 " + b));
		cv1.follow(b -> System.err.println("received cv1 " + b));

		Set<String> internal = new HashSet<>();
		internal.add("a");
		internal.add("b");
		test.set(internal);

		System.err.println("set internal");

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

		Set<String> internal = new HashSet<>();
		internal.addAll(Arrays.asList("a", "bb", "ccc"));
		ObsSetHolderImpl<String> test = new ObsSetHolderImpl<>(internal);

		ObsMapHolder<String, Integer> sizes = test.toMap(s -> s, s -> s.length());
		Assert.assertEquals((int) sizes.get().get("a"), 1);
	}

}
