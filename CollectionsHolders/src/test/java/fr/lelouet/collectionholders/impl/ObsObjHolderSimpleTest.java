package fr.lelouet.collectionholders.impl;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.lelouet.collectionholders.interfaces.collections.ObsListHolder;
import fr.lelouet.collectionholders.interfaces.collections.ObsMapHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsFloatHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsIntHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsLongHolder;
import fr.lelouet.tools.lambdaref.GCManage;

public class ObsObjHolderSimpleTest {

	// TODO fix
	// @Test(timeOut = 10000)
	public void testWeakRef() {
		ObsObjHolderSimple<String> test = new ObsObjHolderSimple<>("a");
		internalCall(test);
		GCManage.force();
		test.set("c");
		Assert.assertEquals(test.followers(), 0);
	}


	protected void internalCall(ObsObjHolderSimple<String> test) {
		for (int i = 1; i <= 1; i++) {
			ObsIntHolder length = test.mapInt(String::length);
			Assert.assertEquals(length.get(), (Integer) 1);
			Assert.assertEquals(test.followers(), i);
		}
	}

	@Test
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

}
