package fr.lelouet.tools.solver;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
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
					} else if (!done.contains(v)) {
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
	 * @param predicate
	 *          predicate to accept vertices besides the source. if null, all
	 *          vertices are accepted.
	 * @return a new Completion that holds the vertices that are reachable from
	 *         the source, and the distances between them.
	 */
	public Completion<T> complete(T source, Predicate<T> predicate) {
		Predicate<T> withSource = predicate == null ? v -> true : predicate.or(v -> source.equals(v));
		Completion<T> ret = new Completion<>();
		Set<T> allowed = connected(source).stream().filter(withSource)
				.collect(Collectors.toSet());
		Indexer<T> index = ret.index = new Indexer<>(comparator, allowed);
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
	 * @return a new graph of 3 Ts, the foot of each being the a. <pre>
	 *       c - b - d
	 *           |
	 *    f- e - a - h - i
	 *       |       |
	 *       g       j
	 *   </pre>
	 */
	public static SimpleGraph<String> triT() {
		SimpleGraph<String> ret = SimpleGraph.natural();
		ret.addEdge("a", "b");
		ret.addEdge("b", "c");
		ret.addEdge("b", "d");
		ret.addEdge("a", "e");
		ret.addEdge("e", "f");
		ret.addEdge("e", "g");
		ret.addEdge("a", "h");
		ret.addEdge("h", "i");
		ret.addEdge("h", "j");
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

	/**
	 * create a cycle of vertice, each point of the cycle is the start of another
	 * cycle of 3 vertices.<br />
	 * <p>
	 * example for 2 1 : a-b-c-a (one cycle from a)
	 * </p>
	 * <p>
	 * example for 2 : a-b-c-a-d-e-f-d (one cycle from a, one from d)
	 * </p>
	 * <p>
	 * example for 3 : main cycle is a-d-g-a, and thre cycles centered on those
	 * three vertices
	 * </p>
	 *
	 * @param towers
	 *          the number of vertices in the main cycle, must be >1
	 *
	 * @return a new graph
	 */
	public static SimpleGraph<String> castle(int towers) {
		SimpleGraph<String> ret = SimpleGraph.natural();
		int length=(int) Math.ceil(Math.log(towers*				3) / Math.log(26));
		String first=null;
		String last=null;
		for(int i=0;i<towers;i++) {
			String base=name(i*3, length);
			if(last!=null) {
				ret.addEdge(last, base);
			} else {
				first=base;
			}
			String angle1 = name(i * 3 + 1, length);
			String angle2 = name(i * 3 + 2, length);
			ret.addEdge(base, angle1);
			ret.addEdge(base, angle2);
			ret.addEdge(angle1, angle2);
			last = base;
		}
		ret.addEdge(first, last);
		return ret;
	}

	/**
	 * create the name of the nth vertices with a word length.
	 *
	 * @param vertex
	 *          indice of the vertex in the graph.
	 * @param wordLength
	 *          number of chars. misisng ones are 'a' instead of 0
	 * @return the corresponding name of the vertex in base 26(characters a-z),
	 *         with leading a to ensure the length. eg name(25, 3)=aaz
	 */
	protected static String name(int vertex, int wordLength) {
		String name = "";
		for (int idx = 0; idx < wordLength; idx++) {
			name = (char) ('a' + vertex % 26) + name;
			vertex = vertex / 26;
		}
		return name;
	}

	/**
	 * create a corridor, that is a series of vertex linked each to the
	 * next.<br />
	 * eg corridor(3) should return a - b - c ; while corridor(27) should return
	 * aa - ab - ... - az - ba
	 *
	 * @param vertices
	 *          number of vertex to have in the graph
	 * @return a new graph .
	 */
	public static SimpleGraph<String> corridor(int vertices) {
		int wordL = (int) Math.ceil(Math.log(vertices) / Math.log(26));
		SimpleGraph<String> ret = SimpleGraph.natural();
		String last = null;
		for (int i = 0; i < vertices; i++) {
			String vertex = name(i, wordL);
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
