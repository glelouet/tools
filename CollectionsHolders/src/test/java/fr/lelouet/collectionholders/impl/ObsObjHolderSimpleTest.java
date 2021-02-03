package fr.lelouet.collectionholders.impl;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.lelouet.collectionholders.impl.numbers.ObsIntHolderImpl;
import fr.lelouet.collectionholders.interfaces.ObsObjHolder;
import fr.lelouet.collectionholders.interfaces.collections.ObsListHolder;
import fr.lelouet.collectionholders.interfaces.collections.ObsMapHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsFloatHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsLongHolder;

public class ObsObjHolderSimpleTest {

	// TODO fix
	// @Test(timeOut = 10000)
	// public void testWeakRef() {
	// ObsObjHolderSimple<String> test = new ObsObjHolderSimple<>("a");
	// internalCall(test);
	// GCManage.force();
	// test.set("c");
	// Assert.assertEquals(test.followers(), 0);
	// }


	// protected void internalCall(ObsObjHolderSimple<String> test) {
	// for (int i = 1; i <= 1; i++) {
	// ObsIntHolder length = test.mapInt(String::length);
	// Assert.assertEquals(length.get(), (Integer) 1);
	// Assert.assertEquals(test.followers(), i);
	// }
	// }

	@Test(timeOut = 500)
	public void testMaps() {
		ObsObjHolderSimple<String> test = new ObsObjHolderSimple<>("hello");

		ObsFloatHolder averageFloat = test.mapFloat(s -> s.chars().mapToDouble(i -> (double) i).average().getAsDouble());
		ObsLongHolder totalLong = test.mapLong(s -> s.chars().mapToLong(i -> (long) i).sum());
		ObsListHolder<Character> listChars = test
				.mapList(s -> s.chars().mapToObj(i -> (char) i).collect(Collectors.toList()));
		ObsMapHolder<Character, Integer> map = test
				.mapMap(
						s -> s.chars().boxed().collect(Collectors.toMap(i -> (Character) (char) (int) i, i -> 1, Integer::sum)));
		Assert.assertEquals(averageFloat.get(), (Float) 106.4f);
		Assert.assertEquals(totalLong.get(), (Long) 532l);
		Assert.assertEquals(listChars.get(), Arrays.asList('h', 'e', 'l', 'l', 'o'));
		Map<Character, Integer> mapped = map.get();
		Assert.assertEquals(mapped.get('h'), (Integer) 1);
		Assert.assertEquals(mapped.get('l'), (Integer) 2);
		Assert.assertEquals(mapped.get('z'), null);
	}

	@Test(timeOut = 500)
	public void testWhenNull() {
		ObsObjHolderSimple<String> test = new ObsObjHolderSimple<>("hello");
		ObsObjHolder<String> notNull = test.when(s -> s == null, s -> "", s -> s);
		Assert.assertEquals(notNull.get(), "hello");
		test.set(null);
		Assert.assertEquals(notNull.get(), "");
	}

	@Test(timeOut = 500)
	public void testUnpack() {
		ObsIntHolderImpl h1 = new ObsIntHolderImpl(1);
		ObsIntHolderImpl h2 = new ObsIntHolderImpl(20);
		ObsObjHolderSimple<ObsIntHolderImpl> test = new ObsObjHolderSimple<>(h1);
		ObsObjHolder<Integer> res = test.unPack(h -> h);
		Assert.assertEquals(res.get(), (Integer) 1);
		test.set(h2);
		Assert.assertEquals(res.get(), (Integer) 20);
		h1.set(5);
		Assert.assertEquals(res.get(), (Integer) 20);
		h2.set(40);
		Assert.assertEquals(res.get(), (Integer) 40);
	}

}
