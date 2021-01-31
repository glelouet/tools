package fr.lelouet.collectionholders.impl.collections;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.lelouet.collectionholders.impl.ObsObjHolderSimple;
import fr.lelouet.collectionholders.interfaces.ObsObjHolder;
import fr.lelouet.collectionholders.interfaces.collections.ObsCollectionHolder;
import fr.lelouet.collectionholders.interfaces.collections.ObsListHolder;

public class AObsCollectionHolderTest {

	@Test(timeOut = 500)
	public void testFlatten() {
		ObsListHolderImpl<ObsListHolder<Character>> test = new ObsListHolderImpl<>();
		ObsListHolder<Character> flattened = test.flatten(l -> l);

		List<Character> charList = Arrays.asList('c', 'h', 'a', 'r');
		ObsListHolderImpl<Character> charObs = new ObsListHolderImpl<>(charList);


		test.set(Arrays.asList(charObs));

		Assert.assertEquals(flattened.get(), Arrays.asList('c', 'h', 'a', 'r'));
	}

	@Test(timeOut = 500)
	public void testFlattenConcat() {
		ObsListHolderImpl<ObsListHolder<Character>> test = new ObsListHolderImpl<>();

		ObsListHolderImpl<Character> list1 = new ObsListHolderImpl<>(Arrays.asList('c', 'h', 'a', 'r'));
		ObsListHolderImpl<Character> list2 = new ObsListHolderImpl<>(Arrays.asList('i', 's', 'm'));
		test.set(Arrays.asList(list1, list2));

		ObsListHolder<Character> flattened = test.flatten(l -> l);
		Assert.assertEquals(flattened.get(), Arrays.asList('c', 'h', 'a', 'r', 'i', 's', 'm'));

		list2.set(Arrays.asList('a', 'c', 't', 'e', 'r'));

		Assert.assertEquals(flattened.get(), Arrays.asList('c', 'h', 'a', 'r', 'a', 'c', 't', 'e', 'r'));
	}

	@Test(timeOut = 500)
	public void testMapUnpack() {
		// source is the list of ints.
		ObsListHolderImpl<Integer> source = ObsListHolderImpl.of(0, 1, 2, 3, 4, 5);
		// we back each int to a different string holder so we can get them.
		Map<Integer, ObsObjHolderSimple<String>> backMap = new HashMap<>();
		ObsCollectionHolder<String, ?> test = source
				.unpackItems(i -> backMap.computeIfAbsent(i, i2 -> new ObsObjHolderSimple<>("" + i2)));
		// concat the string holders.
		ObsObjHolder<String> concat = test.map(l -> l.stream().collect(Collectors.joining()));

		Assert.assertEquals(concat.get(), "012345");

		backMap.get(0).set("a");
		Assert.assertEquals(concat.get(), "a12345");
	}

}
