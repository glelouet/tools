package fr.lelouet.tools.solver;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * represents a Simple graph :
 * <ul>
 * <li>Vertices of a given (parameter) type</li>
 * <li>a comparator to be able to compare the vertices. If the type extends
 * {@link Comparable} then you can use natural comparator.</li>
 * <li>a series of accepted couple of vertices as edges.</li>
 * <li>no double edge on two vertices, nor loop on one vertex</li>
 * </ul>
 * <p>
 * The vertices are stored with the edges to the vertices that are greater than
 * them. Typically to store the edge A-B, A is remembered to be linked to B, and
 * B is not remembered to be linked to A. This allows to save memory while still
 * having deterministic structure.
 * </p>
 *
 * @author glelouet
 * @param T
 *          the type of the vertices
 *
 */
public class SimpleGraph<T> {

	@SuppressWarnings("unused")
	private static final long serialVersionUID = 1L;

	private final Comparator<T> comparator;

	private final HashMap<T, Set<T>> edges = new HashMap<>();

	public SimpleGraph(Comparator<T> comparator2) {
		this.comparator = comparator2;
	}

	public static <U extends Comparable<U>> SimpleGraph<U> natural() {
		return new SimpleGraph<U>(Comparator.naturalOrder());
	}

	public void addVertex(T vertex) {
		edges.computeIfAbsent(vertex, o -> new HashSet<>());
	}

	/**
	 * add an edge from one vertex to another. Both vertex are added.<br />
	 * If the edge is already present, nothing is added.<br />
	 * If v1=v2 comparator-wise, the vertex is added but not the edge.
	 *
	 * @param v1
	 * @param v2
	 */
	public void addEdge(T v1, T v2) {
		int cmp = comparator.compare(v1, v2);
		if (cmp == 0) {
			addVertex(v1);
			return;
		}
		// ensure v1 < v2
		if (cmp > 0) {
			T tmp = v1;
			v1 = v2;
			v2 = tmp;
		}
		addVertex(v1);
		addVertex(v2);
		edges.get(v1).add(v2);
	}

	public Stream<T> vertices() {
		return edges.keySet().stream().sorted(comparator);
	}

	/**
	 * stream the T adjacent to a source.
	 *
	 * @param source
	 *          a vertex of the graph.
	 * @return a new stream of the vertices that are adjacent to the graph.
	 */
	public Stream<T> adjacent(T source) {
		Stream<T> lower = edges.entrySet().stream().filter(e -> comparator.compare(e.getKey(), source) < 0)
				.filter(e -> e.getValue().contains(source)).map(e -> e.getKey());
		Stream<T> greater = edges.getOrDefault(source, Collections.emptySet()).stream();
		return Stream.concat(lower, greater).sorted(comparator);
	}

	/**
	 * make a breadth-first traversal from a source, collecting all the vertices
	 * met.
	 *
	 * @param source
	 *          a vertex of the graph
	 * @return a new set containing all the vertices reached from the source
	 */
	public Set<T> connected(T source) {
		Set<T> done = new HashSet<>();
		Set<T> frontier = new HashSet<>(Arrays.asList(source));
		while (!frontier.isEmpty()) {
			Set<T> nextFrontier = new HashSet<>();
			done.addAll(frontier);
			for (T t : frontier) {
				adjacent(t).forEach(v -> {
					if (!done.contains(v)) {
						nextFrontier.add(v);
					}
				});
			}
			frontier = nextFrontier;
		}
		return done;
	}

	/**
	 *
	 * @param origin
	 * @param destination
	 * @return - 1 if no route from one to another.
	 */
	public int distance(T origin, T destination) {
		if (origin == null || destination == null) {
			return -1;
		}
		if (origin.equals(destination)) {
			return 0;
		}
		int distance = 1;
		Set<T> done = new HashSet<>();
		Set<T> frontier = new HashSet<>(Arrays.asList(origin));
		boolean[] found = new boolean[] { false };
		while (!frontier.isEmpty()) {
			Set<T> nextFrontier = new HashSet<>();
			done.addAll(frontier);
			for (T t : frontier) {
				adjacent(t).forEach(v -> {
					if (v.equals(destination)) {
						found[0] = true;
					} else
						if (!done.contains(v)) {
							nextFrontier.add(v);
						}
				});
				if (found[0]) {
					return distance;
				}
			}
			frontier = nextFrontier;
			distance++;
		}
		return -1;
	}

	public static class Completion<T> {
		public int[][] distances;
		public Indexer<T> index;
	}

	/**
	 * generate the complete graph from this, starting from a source.
	 *
	 * @param source
	 *          a vertex in this
	 * @return a new Completion that holds the vertices that are reachable from
	 *         the source, and the distances between them.
	 */
	public Completion<T> complete(T source) {
		Completion<T> ret = new Completion<>();
		Indexer<T> index = ret.index = new Indexer<>(comparator, connected(source));
		int[][] distances = ret.distances = new int[index.size()][];
		for (int i = 0; i < index.size(); i++) {
			distances[i] = new int[index.size()];
			distances[i][i] = 0;
			T origin = index.item(i);
			for (int j = 0; j < i; j++) {
				distances[i][j] = distances[j][i] = distance(origin, index.item(j));
			}
		}
		return ret;
	}

	// useful graphs

	/**
	 *
	 * @return a new graph of 3 vertices a, b, c each linked to the other ones
	 */
	public static SimpleGraph<String> triangle() {
		SimpleGraph<String> ret = SimpleGraph.natural();
		ret.addEdge("a", "b");
		ret.addEdge("b", "c");
		ret.addEdge("a", "c");
		return ret;
	}

	/**
	 *
	 * @return a new graph of 4 vertices a, b, d c, each linked to the previous
	 *         one.
	 */
	public static SimpleGraph<String> square() {
		SimpleGraph<String> ret = SimpleGraph.natural();
		ret.addEdge("a", "b");
		ret.addEdge("b", "d");
		ret.addEdge("c", "d");
		ret.addEdge("a", "c");
		return ret;
	}

	/**
	 *
	 * @return a new graph shaped in a 8 : <pre>
	 * a - b
	 * |   |
	 * c - d
	 * |   |
	 * e - f
	 * </pre>
	 */
	public static SimpleGraph<String> eight() {
		SimpleGraph<String> ret = SimpleGraph.natural();
		ret.addEdge("a", "b");
		ret.addEdge("a", "c");
		ret.addEdge("b", "d");
		ret.addEdge("c", "d");
		ret.addEdge("c", "e");
		ret.addEdge("d", "f");
		ret.addEdge("e", "f");
		return ret;
	}

	public static SimpleGraph<String> corridor(int vertices) {
		int wordL = (int) Math.ceil(Math.log(vertices) / Math.log(26));
		SimpleGraph<String> ret = SimpleGraph.natural();
		String last = null;
		for (int i = 0; i < vertices; i++) {
			int cp = i;
			String vertex = "";
			for(int idx=0;idx<wordL;idx++) {
				vertex = (char) ('a' + cp % 26) + vertex;
				cp = cp / 26;
			}
			if (last == null) {
				ret.addVertex(vertex);
			} else {
				ret.addEdge(last, vertex);
			}
			last = vertex;
		}
		return ret;
	}

}
