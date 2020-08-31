package fr.lelouet.tools.solver;

import java.util.Comparator;
import java.util.stream.Collectors;

import org.testng.Assert;
import org.testng.annotations.Test;

public class IndexerTest {

	@Test
	public void testCreation() {
		Indexer<String> test = Indexer.of("a", "b");
		Assert.assertEquals(test.size(), 2);
		Assert.assertEquals(test.item(0), "a");
		Assert.assertEquals(test.item(1), "b");
		Assert.assertEquals(test.iterator().next(), "a");
	}

	@Test
	public void testUnicity() {
		Indexer<String> test = Indexer.of("a", "c", "b", "c", "b", "a");
		Assert.assertEquals(test.stream().collect(Collectors.joining()), "abc");
	}

	@Test
	public void testComparatorSuccess() {
		Comparator<String[]> c = (s1, s2) -> s1[0].compareTo(s2[0]);
		new Indexer<>(c, new String[] { "a" }, new String[] { "b" });
	}

	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void testComparatorFail() {
		Comparator<String[]> c = (s1, s2)->s1[0].compareTo(s2[0]);
		new Indexer<>(c, new String[] { "a" }, new String[] { "a" });
	}

}
