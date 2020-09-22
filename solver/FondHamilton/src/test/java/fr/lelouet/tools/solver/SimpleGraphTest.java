package fr.lelouet.tools.solver;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.IntPredicate;
import java.util.stream.Collectors;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.lelouet.tools.solver.SimpleGraph.AdjMatrix;
import fr.lelouet.tools.solver.SimpleGraph.Completion;

public class SimpleGraphTest {

	@Test
	public void testTriangle() {
		SimpleGraph<String> test = SimpleGraph.triangle();
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
		Assert.assertEquals(test.toMatrix().connected("a", null), new HashSet<>(Arrays.asList("a", "b", "c")));
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
		Assert.assertEquals(test.toMatrix().connected("a", null), new HashSet<>(Arrays.asList("a", "b", "c")));
	}

	/**
	 * a-b-c-d-e , test connected(a) avoiding c
	 */
	@Test
	public void testConnected3() {
		SimpleGraph<String> test = SimpleGraph.corridor(5);
		Assert.assertEquals(test.toMatrix().connected("a", v -> !v.equals("c")), new HashSet<>(Arrays.asList("a", "b")));

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
		Assert.assertEquals(test.distance("a", "e", null), 4);
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
		Assert.assertEquals(test.distance("a", "e", null), 1);
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
		Completion<String> complete = test.complete("a", null, null);
		Assert.assertEquals(complete.index.size(), 3);
	}

	@Test
	public void testCorridor() {
		SimpleGraph<String> test = SimpleGraph.corridor(27);
		Set<String> vertices = test.vertices().collect(Collectors.toSet());
		Assert.assertTrue(vertices.contains("aa"), "got " + vertices);
		Assert.assertTrue(vertices.contains("ba"), "got " + vertices);
		Assert.assertFalse(vertices.contains("bb"), "got " + vertices);
	}

	@Test(dependsOnMethods = { "testCorridor", "testDistance" })
	public void testDistanceCorridor() {
		SimpleGraph<String> test = SimpleGraph.corridor(27);
		Assert.assertEquals(test.distance("aa", "ba", null), 26);
	}

	@Test
	public void testCastle() {
		SimpleGraph<String> test = SimpleGraph.castle(3);
		Assert.assertEquals(test.adjacent("a").collect(Collectors.joining()), "bcdg");
		Assert.assertEquals(test.adjacent("d").collect(Collectors.joining()), "aefg");
		Assert.assertEquals(test.adjacent("g").collect(Collectors.joining()), "adhi");
		Assert.assertEquals(test.distance("b", "f", null), 3);
	}

	/**
	 * find the deadends on a castle of size 3. deadends should be b,c ; e,f ; h,i
	 */
	@Test
	public void testDeadEndsCastle3() {
		SimpleGraph<String> test = SimpleGraph.castle(3);
		Set<Set<String>> deadends = test.deadEnds();
		Assert.assertEquals(deadends.size(), 3, "deadends are " + deadends);
		Assert.assertTrue(deadends.contains(new HashSet<>(Arrays.asList("b", "c"))), "deadends are " + deadends);
		Assert.assertTrue(deadends.contains(new HashSet<>(Arrays.asList("e", "f"))), "deadends are " + deadends);
		Assert.assertTrue(deadends.contains(new HashSet<>(Arrays.asList("h", "i"))), "deadends are " + deadends);
	}

	/**
	 * test a graph in accordion : a - (bc) - d - (ef) - g -(hi) -j
	 */
	@Test
	public void testDeadEndsAccordion() {
		SimpleGraph<String> test = SimpleGraph.natural();
		test.addEdge("a", "b");
		test.addEdge("b", "d");
		test.addEdge("a", "c");
		test.addEdge("c", "d");
		test.addEdge("d", "e");
		test.addEdge("e", "g");
		test.addEdge("d", "f");
		test.addEdge("f", "g");
		test.addEdge("g", "h");
		test.addEdge("h", "j");
		test.addEdge("g", "i");
		test.addEdge("i", "j");
		Set<Set<String>> deadends = test.deadEnds();
		Assert.assertEquals(deadends.size(), 2, "deadends are " + deadends);
		Assert.assertTrue(deadends.contains(new HashSet<>(Arrays.asList("a", "b", "c"))), "deadends are " + deadends);
		Assert.assertTrue(deadends.contains(new HashSet<>(Arrays.asList("h", "i", "j"))), "deadends are " + deadends);
	}

	@Test
	public void testMatrix() {
		SimpleGraph<String> test = SimpleGraph.natural();
		test.addEdge("a", "b");
		test.addEdge("b", "c");
		test.addEdge("c", "d");
		AdjMatrix<String> matrix = test.toMatrix();
		int posA = matrix.index.position("a");
		Assert.assertEquals(posA, 0);
		int posB = matrix.index.position("b");
		Assert.assertEquals(posB, 1);
		int posC = matrix.index.position("c");
		Assert.assertEquals(posC, 2);
		int posD = matrix.index.position("d");
		Assert.assertEquals(posD, 3);
		IntPredicate notB = i -> i != posB;
		boolean[] connectedA = matrix.connected(matrix.index.position("a"), notB);
		Assert.assertTrue(connectedA[posA]);
		Assert.assertFalse(connectedA[posB]);
		Assert.assertFalse(connectedA[posC]);
		Assert.assertFalse(connectedA[posD]);
		boolean[] connectedC = matrix.connected(matrix.index.position("c"), notB);
		Assert.assertFalse(connectedC[posA]);
		Assert.assertFalse(connectedC[posB]);
		Assert.assertTrue(connectedC[posC]);
		Assert.assertTrue(connectedC[posD]);
	}

}
