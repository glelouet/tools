package fr.lelouet.holders.impl.collections;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.lelouet.holders.impl.ObjHolderSimple;
import fr.lelouet.holders.impl.numbers.BoolHolderImpl;
import fr.lelouet.holders.interfaces.ObjHolder;
import fr.lelouet.holders.interfaces.collections.MapHolder;

public class SetHolderImplTest {

	@Test(timeOut = 500)
	public void testCreating() {
		SetHolderImpl<String> test = new SetHolderImpl<>();

		ObjHolder<Boolean> a1 = test.contains("a");
		ObjHolder<Boolean> c1 = test.contains("c");
		ObjHolder<Boolean> av1 = test.contains(new ObjHolderSimple<>("a"));
		ObjHolder<Boolean> cv1 = test.contains(new ObjHolderSimple<>("c"));

		Set<String> internal = new HashSet<>();
		internal.add("a");
		internal.add("b");
		test.set(internal);

		ObjHolder<Boolean> a2 = test.contains("a");
		ObjHolder<Boolean> c2 = test.contains("c");
		ObjHolder<Boolean> av2 = test.contains(new ObjHolderSimple<>("a"));
		ObjHolder<Boolean> cv2 = test.contains(new ObjHolderSimple<>("c"));

		for (ObjHolder<Boolean> a : Arrays.asList(a1, a2, av1, av2)) {
			Assert.assertTrue(a.get());
		}

		for (ObjHolder<Boolean> c : Arrays.asList(c1, c2, cv1, cv2)) {
			Assert.assertFalse(c.get());
		}
	}

	@Test(timeOut = 500)
	public void testFollow() {
		SetHolderImpl<String> test = SetHolderImpl.of("a", "bb", "ccc");

		MapHolder<String, Integer> sizes = test.toMap(s -> s, s -> s.length());
		Assert.assertEquals((int) sizes.get().get("a"), 1);
	}

	@Test(timeOut = 500)
	public void testFilter() {
		SetHolderImpl<String> test = SetHolderImpl.of(null, null);
		SetHolderImpl<String> nonNull = test.filter(s -> s != null);
		Assert.assertEquals(nonNull.get(), Collections.emptySet());
		test.setEmpty();
		Assert.assertEquals(nonNull.get(), Collections.emptySet());
		test.set("a", "b");
		Assert.assertEquals(nonNull.get(), new HashSet<>(Arrays.asList("a", "b")));
	}

	@Test(timeOut = 500)
	public void testFilterWhen() {
		SetHolderImpl<String> test = SetHolderImpl.of("a", "b");
		BoolHolderImpl ret = new BoolHolderImpl(false);
		SetHolderImpl<String> accepted = test.filterWhen(s -> ret);
		Assert.assertEquals(accepted.get(), Collections.emptySet());
		ret.set(true);
		Assert.assertEquals(accepted.get(), new HashSet<>(Arrays.asList("a", "b")));
		test.setEmpty();
		Assert.assertEquals(accepted.get(), Collections.emptySet());
		ret.set(false);
		Assert.assertEquals(accepted.get(), Collections.emptySet());
	}

}
