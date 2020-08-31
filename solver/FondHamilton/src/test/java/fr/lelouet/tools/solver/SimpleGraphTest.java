package fr.lelouet.tools.solver;

import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.lelouet.tools.solver.SimpleGraph.Completion;

public class SimpleGraphTest {

	@Test
	public void testTriangle() {
		SimpleGraph<String> test = SimpleGraph.natural();
		test.addEdge("b", "c");
		test.addEdge("a", "b");
		test.addEdge("a", "c");
		Assert.assertEquals(test.adjacent("a").collect(Collectors.joining()), "bc");
		Assert.assertEquals(test.adjacent("b").collect(Collectors.joining()), "ac");
		Assert.assertEquals(test.adjacent("c").collect(Collectors.joining()), "ab");
	}

	/**
	 * Test with 6 nodes that are linked to make a 3
	 * <pre>
	 * A - B
	 *     |
	 * C - D
	 *     |
	 * E - F
	 * </pre>
	 */
	@Test
	public void test3() {
		SimpleGraph<String> test = SimpleGraph.natural();
		test.addEdge("a", "b");
		test.addEdge("b", "d");
		test.addEdge("c", "d");
		test.addEdge("f", "d");
		test.addEdge("e", "f");
		Assert.assertEquals(test.adjacent("a").collect(Collectors.joining()), "b");
		Assert.assertEquals(test.adjacent("b").collect(Collectors.joining()), "ad");
		Assert.assertEquals(test.adjacent("c").collect(Collectors.joining()), "d");
		Assert.assertEquals(test.adjacent("d").collect(Collectors.joining()), "bcf");
		Assert.assertEquals(test.adjacent("e").collect(Collectors.joining()), "f");
		Assert.assertEquals(test.adjacent("f").collect(Collectors.joining()), "de");
	}

	/***
	 * a,b,c are connected ; d is alone
	 */
	@Test
	public void testConnected() {
		SimpleGraph<String> test = SimpleGraph.natural();
		test.addEdge("a", "b");
		test.addEdge("b", "c");
		test.addVertex("d");
		Assert.assertEquals(test.connected("a"), new HashSet<>(Arrays.asList("a", "b", "c")));
	}

	/**
	 * a, b, c are connected ; d,e are connected.
	 */
	@Test
	public void testConnected2() {
		SimpleGraph<String> test = SimpleGraph.natural();
		test.addEdge("a", "b");
		test.addEdge("b", "c");
		test.addEdge("d", "e");
		Assert.assertEquals(test.connected("a"), new HashSet<>(Arrays.asList("a", "b", "c")));
	}

	/**
	 * distance from a to e in a-b-c-d-e is 4
	 */
	@Test
	public void testDistance() {
		SimpleGraph<String> test = SimpleGraph.natural();
		test.addEdge("a", "b");
		test.addEdge("b", "c");
		test.addEdge("c", "d");
		test.addEdge("d", "e");
		Assert.assertEquals(test.distance("a", "e"), 4);
	}

	/**
	 * same as above but with a-e so distance is 1
	 */
	@Test
	public void testDistance2() {
		SimpleGraph<String> test = SimpleGraph.natural();
		test.addEdge("a", "b");
		test.addEdge("b", "c");
		test.addEdge("c", "d");
		test.addEdge("d", "e");
		test.addEdge("e", "a");
		Assert.assertEquals(test.distance("a", "e"), 1);
	}

	/**
	 * a, b, c are connected ; d,e are connected.
	 */
	@Test
	public void testComplete() {
		SimpleGraph<String> test = SimpleGraph.natural();
		test.addEdge("a", "b");
		test.addEdge("b", "c");
		test.addEdge("d", "e");
		Completion<String> complete = test.complete("a");
		Assert.assertEquals(complete.index.size(), 3);
	}

}
