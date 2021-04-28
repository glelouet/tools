package fr.lelouet.tools.holders.impl.collections;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.lelouet.tools.holders.impl.ObjHolderSimple;
import fr.lelouet.tools.holders.interfaces.ObjHolder;
import fr.lelouet.tools.holders.interfaces.collections.CollectionHolder;
import fr.lelouet.tools.holders.interfaces.collections.ListHolder;

public class ACollectionHolderTest {

	@Test(timeOut = 500)
	public void testFlatten() {
		ListHolderImpl<ListHolder<Character>> test = new ListHolderImpl<>();
		ListHolder<Character> flattened = test.flatten(l -> l);

		List<Character> charList = Arrays.asList('c', 'h', 'a', 'r');
		ListHolderImpl<Character> charObs = new ListHolderImpl<>(charList);


		test.set(Arrays.asList(charObs));

		Assert.assertEquals(flattened.get(), Arrays.asList('c', 'h', 'a', 'r'));
	}

	@Test(timeOut = 500)
	public void testFlattenConcat() {
		ListHolderImpl<ListHolder<Character>> test = new ListHolderImpl<>();

		ListHolderImpl<Character> list1 = new ListHolderImpl<>(Arrays.asList('c', 'h', 'a', 'r'));
		ListHolderImpl<Character> list2 = new ListHolderImpl<>(Arrays.asList('i', 's', 'm'));
		test.set(Arrays.asList(list1, list2));

		ListHolder<Character> flattened = test.flatten(l -> l);
		Assert.assertEquals(flattened.get(), Arrays.asList('c', 'h', 'a', 'r', 'i', 's', 'm'));

		list2.set(Arrays.asList('a', 'c', 't', 'e', 'r'));

		Assert.assertEquals(flattened.get(), Arrays.asList('c', 'h', 'a', 'r', 'a', 'c', 't', 'e', 'r'));
	}

	@Test(timeOut = 500)
	public void testMapUnpack() {
		// source is the list of ints.
		ListHolderImpl<Integer> source = ListHolderImpl.of(0, 1, 2, 3, 4, 5);
		// we back each int to a different string holder so we can get them.
		Map<Integer, ObjHolderSimple<String>> backMap = new HashMap<>();
		CollectionHolder<String, ?> test = source
				.unpackItems(i -> backMap.computeIfAbsent(i, i2 -> new ObjHolderSimple<>("" + i2)));
		// concat the string holders.
		ObjHolder<String> concat = test.map(l -> l.stream().collect(Collectors.joining()));

		Assert.assertEquals(concat.get(), "012345");

		backMap.get(0).set("a");
		Assert.assertEquals(concat.get(), "a12345");
	}

}
