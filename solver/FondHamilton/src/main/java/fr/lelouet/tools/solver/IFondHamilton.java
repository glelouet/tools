package fr.lelouet.tools.solver;

import java.util.LinkedHashMap;
import java.util.List;

import fr.lelouet.tools.solver.SimpleGraph.Completion;

/**
 *
 * @author glelouet
 *
 */
public interface IFondHamilton {

	/**
	 * find a cycle starting from a source in a graph. The goal is usually to find
	 * the cycle with smallest distance. The result is a {@link LinkedHashMap} so
	 * that the keys are stored in the order of the cycle.
	 *
	 * @param <T>
	 *          type of the vertices
	 * @param graph
	 *          the graph to find a cycle in
	 * @param source
	 *          a vertex of the graph to start the exploration from.
	 * @return a new {@link LinkedHashMap} of the vertices, to the distance from
	 *         the previous one in the cycle. The map is linked so iterating it
	 *         will be done in the order of the vertices in the cycle. the source
	 *         is always at the last position.
	 */
	public default <T> LinkedHashMap<T, Integer> solve(SimpleGraph<T> graph, T source) {
		Completion<T> complete = graph.complete(source);
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
	 * actual implementation of the resolution.
	 *
	 * @param <T>
	 * @param index
	 * @param distances
	 * @return
	 */
	public <T> List<T> solve(Indexer<T> index, int[][] distances, int sourceIndex);


}
