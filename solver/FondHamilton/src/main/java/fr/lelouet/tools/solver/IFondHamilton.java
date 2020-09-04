package fr.lelouet.tools.solver;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Predicate;

import fr.lelouet.tools.solver.SimpleGraph.Completion;

/**
 *
 * @author glelouet
 *
 */
public interface IFondHamilton {

	/**
	 * find a cycle starting from a source in a graph. The goal is usually to find
	 * the cycle with smallest distance. The result is a {@link LinkedHashMap} for
	 * which the keys are stored in the order of the cycle.
	 *
	 * @param <T>
	 *          type of the vertices
	 * @param graph
	 *          the graph to find a cycle in
	 * @param source
	 *          a vertex of the graph to start the exploration from.
	 * @param allowed
	 *          the predicate on vertices besides the source to be allowed in the
	 *          graph. all vertices are allowed if null.
	 * @return a new {@link LinkedHashMap} of the vertices, to the distance from
	 *         the previous one in the cycle. The map is linked so iterating it
	 *         will be done in the order of the vertices in the cycle. the source
	 *         is always at the last position.
	 */
	public default <T> LinkedHashMap<T, Integer> solve(T source, SimpleGraph<T> graph, Predicate<T> allowed) {
		Completion<T> complete = graph.complete(source, allowed);
		Indexer<T> idx = complete.index;
		int[][] distances = complete.distances;
		int sourceIdx = idx.position(source);
		List<T> list = solve(complete.index, complete.distances, sourceIdx);
		LinkedHashMap<T, Integer> ret = new LinkedHashMap<>();
		int lastIdx = idx.position(source);
		for (T vertex : list) {
			if (vertex == source) {
				continue;
			}
			int index = idx.position(vertex);
			ret.put(vertex, distances[index][lastIdx]);
			lastIdx = index;
		}
		ret.put(source, distances[lastIdx][sourceIdx]);
		return ret;
	}

	/**
	 * find a cycle starting from the first vertex of the graph.
	 *
	 * @param <T>
	 *          type of the vertex (eg String)
	 * @param graph
	 *          the graph to find a cycle in
	 * @param allowed
	 *          list of the vertices that need to be in the cycle.
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public default <T> LinkedHashMap<T, Integer> solve(SimpleGraph<T> graph, T... allowed) {
		return solve(graph.vertices().findFirst().get(), graph,
				allowed == null || allowed.length == 0 ? null : new HashSet(Arrays.asList(allowed))::contains);
	}

	/**
	 * actual implementation of the resolution.
	 *
	 * @param <T>
	 * @param index
	 * @param distances
	 * @return
	 */
	public <T> List<T> solve(Indexer<T> index, int[][] distances, int sourceIndex);


}
