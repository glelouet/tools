package fr.lelouet.tools.solver;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(SimpleGraph.class);

	private final Comparator<T> comparator;

	private final HashMap<T, Set<T>> edges = new HashMap<>();

	public SimpleGraph(Comparator<T> comparator2) {
		comparator = comparator2;
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

	/**
	 *
	 * @param v1
	 * @param v2
	 * @return true if v1!=v2 and there is an edge from v1 to v2.
	 */
	public boolean isAdjacent(T v1, T v2) {
		int cmp = comparator.compare(v1, v2);
		if (cmp == 0) {
			return false;
		}
		// ensure v1 < v2
		if (cmp > 0) {
			T tmp = v1;
			v1 = v2;
			v2 = tmp;
		}
		return edges.getOrDefault(v1, Collections.emptySet()).contains(v2);
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
	 * Completion of a graph. For each vertex couple (u,v) there is a path from u
	 * to v. Holds the distances of each couple in the {@link #distances} matrix.
	 *
	 * @author
	 *
	 * @param <T>
	 */
	public static class Completion<T> {
		public int[][] distances;
		public Indexer<T> index;
	}

	/**
	 * generate the complete graph from this, starting from a source.
	 *
	 * @param source
	 *          a vertex in this
	 * @param retained
	 *          predicate to accept vertices besides the source. if null, all
	 *          vertices are accepted.
	 * @return a new Completion that holds the vertices that are reachable from
	 *         the source, and the distances between them.
	 */
	public Completion<T> complete(T source, Predicate<T> retained) {
		return toMatrix().complete(source, retained);
	}

	/**
	 * find the groups of important vertices that have only one way out, so in an
	 * optimal graph they follow each other. use the adjacent matrix for fast BFS.
	 *
	 * @param <T>
	 * @param graph
	 * @param important
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Set<Set<T>> deadEnds() {
		Set<Set<T>> ret = new HashSet<>();
		List<T> allVertices = vertices().collect(Collectors.toList());
		AdjMatrix<T> matrix = toMatrix();
		// for each vertex v, we create the connex subsets of the adjacent vertices,
		// without this vertex.
		vertices().forEach(v -> {
			int vi = matrix.index.position(v);
			IntPredicate notv = i -> i != vi;
			Object[] adj = adjacent(v).sorted(Comparator.comparing(elem -> adjacent(elem).count())).toArray();
			if (adj.length < 2) {
				return;
			}
			// set of connected subGraph when removing v
			Set<Set<T>> connectedSets = new HashSet<>();
			Set<Set<T>> toAdd = new HashSet<>();
			for (Object element : adj) {
				T v1 = (T) element;
				if (connectedSets.stream().filter(set -> set.contains(v1)).findAny().isEmpty()) {
					boolean[] connected = matrix.connected(matrix.index.position(v1), notv);
					Set<T> connectedV1 = new HashSet<>();
					for (int i = 0; i < matrix.index.size(); i++) {
						if (connected[i]) {
							connectedV1.add(matrix.index.item(i));
						}
					}
					connectedSets.add(connectedV1);
					// if the connected subgraph is too big, don't add it in the deadends.
					if (connectedV1.size() < allVertices.size() / 2) {
						toAdd.add(connectedV1);
					}
				}
			}
			if (!toAdd.isEmpty()) {
				// System.err.println(toAdd.size() + " deadends when removing " + v + "
				// : " + toAdd);
				ret.addAll(toAdd);
			}
		});
		return ret;
	}

	public static class AdjMatrix<T> {
		public final boolean[][] matrix;
		public final Indexer<T> index;
		public final Comparator<T> comparator;

		protected AdjMatrix(Comparator<T> comparator, Stream<T> vertices, BiPredicate<T, T> isAdjacent) {
			index = new Indexer<>(comparator, vertices.collect(Collectors.toList()));
			matrix = new boolean[index.size()][index.size()];
			this.comparator = comparator;
			for (int ti = 0; ti < index.size(); ti++) {
				T t = index.item(ti);
				for (int ui = 0; ui < ti; ui++) {
					T u = index.item(ui);
					if (isAdjacent.test(u, t)) {
						matrix[ti][ui] = matrix[ui][ti] = true;
					}
				}
			}
		}

		/**
		 * BFS with the indices.
		 *
		 * @param source
		 *          index of the source.
		 * @param accepted
		 *          predicate if a vertex indice is accepted.
		 * @return a new boolean[i], with value true if the vertex of index i is
		 *         reachable from source.
		 */
		public boolean[] connected(int source, IntPredicate accepted) {
			boolean[] done = new boolean[index.size()];
			int[] frontier = new int[index.size()];
			int frontierSize = 1;
			frontier[0] = source;
			int[] nextFrontier = new int[index.size()];
			while (frontierSize > 0) {
				int nextFrontierSize = 0;
				for (int i = 0; i < frontierSize; i++) {
					done[frontier[i]] = true;
				}
				for (int fi = 0; fi < frontierSize; fi++) {
					int i = frontier[fi];
					for (int j = 0; j < index.size(); j++) {
						if (i != j && matrix[i][j]) {
							if (!done[j]) {
								if (accepted == null || accepted.test(j)) {
									nextFrontier[nextFrontierSize] = j;
									nextFrontierSize++;
								}
							}
						}
					}
				}
				int[] tmp = frontier;
				frontier = nextFrontier;
				nextFrontier = tmp;
				frontierSize = nextFrontierSize;
			}
			return done;
		}

		public Set<T> connected(T source, Predicate<T> accepted) {
			boolean[] arr = connected(index.position(source), it -> accepted == null || accepted.test(index.item(it)));
			Set<T> ret = new HashSet<>();
			for (int i = 0; i < index.size(); i++) {
				if (arr[i]) {
					ret.add(index.item(i));
				}
			}
			return ret;
		}

		/**
		 *
		 * @param origin
		 * @param destination
		 * @return - 1 if no route from one to another.
		 */
		public int distance(int origin, int destination) {
			if (origin == destination) {
				return 0;
			}
			boolean[] done = new boolean[index.size()];
			int[] frontier = new int[index.size()];
			int frontierSize = 1;
			frontier[0] = origin;
			int[] nextFrontier = new int[index.size()];
			for (int distance = 1; frontierSize > 0; distance++) {
				for (int fi = 0; fi < frontierSize; fi++) {
					done[frontier[fi]] = true;
				}
				int nextFrontierSize = 0;
				for (int fi = 0; fi < frontierSize; fi++) {
					int visited = frontier[fi];
					for (int possiblei = 0; possiblei < matrix.length; possiblei++) {
						if (possiblei != visited && !done[possiblei] && matrix[visited][possiblei]) {
							if (possiblei == destination) {
								return distance;
							}
							nextFrontier[nextFrontierSize] = possiblei;
							nextFrontierSize++;
						}
					}
				}
				int[] tmp = frontier;
				frontier = nextFrontier;
				nextFrontier = tmp;
				frontierSize = nextFrontierSize;
			}
			return -1;
		}

		public int distance(T origin, T destination) {
			return distance(index.position(origin), index.position(destination));
		}

		public int[][] distances() {
			int[][] ret = new int[matrix.length][];
			for (int i = 0; i < ret.length; i++) {
				ret[i] = new int[matrix.length];
				for (int j = 0; j < matrix.length; j++) {
					ret[i][j] = matrix[i][j] ? 1 : -1;
				}
			}
			boolean change = true;
			while (change) {
				change = false;
				for (int i = 0; i < ret.length; i++) {
					for (int j = 0; j < i; j++) {
						int dist = ret[i][j];
						for (int intermediate = 0; intermediate < ret.length; intermediate++) {
							if (intermediate != i && intermediate != j && ret[i][intermediate] != -1 && ret[j][intermediate] != -1) {
								int interDist = ret[i][intermediate] + ret[j][intermediate];
								if (dist == -1 || dist > interDist) {
									dist=interDist;
								}
							}
						}
						if (ret[i][j] != dist) {
							ret[i][j] = ret[j][i] = dist;
							change = true;
						}
					}
				}
			}
			return ret;
		}

		public Completion<T> complete(T source, Predicate<T> retained) {
			Completion<T> ret = new Completion<>();
			Stream<T> stream = index.stream();
			if (retained != null) {
				stream = stream.filter(retained);
			}
			stream = Stream.concat(stream, Stream.of(source));
			ret.index = new Indexer<>(comparator, stream.collect(Collectors.toSet()));
			int[][] distances = distances();
			if (ret.index.size() == index.size()) {
				ret.distances = distances;
			} else {
				ret.distances = new int[ret.index.size()][ret.index.size()];
				for (int i = 1; i < ret.index.size(); i++) {
					int oldi = index.position(ret.index.item(i));
					for (int j = 0; j < i; j++) {
						int oldj = index.position(ret.index.item(j));
						ret.distances[i][j] = ret.distances[j][i] = distances[oldi][oldj];
					}
				}
			}
			return ret;
		}
	}

	/**
	 *
	 * @return a new matrix that contains the boolean representation of adjacent
	 *         between vertices.
	 */
	public AdjMatrix<T> toMatrix() {
		return new AdjMatrix<>(comparator, vertices(), this::isAdjacent);
	}

	//
	// useful graphs
	//

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
		int length = (int) Math.ceil(Math.log(towers * 3) / Math.log(26));
		String first = null;
		String last = null;
		for (int i = 0; i < towers; i++) {
			String base = name(i * 3, length);
			if (last != null) {
				ret.addEdge(last, base);
			} else {
				first = base;
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
