package fr.lelouet.tools.solver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import fr.lelouet.tools.solver.SimpleGraph.Completion;

/**
 *
 * @author glelouet
 *
 */
public interface IFondHamilton {

	public static class ResultList<T> extends ArrayList<T> {
		private static final long serialVersionUID = 1L;
		public long msFirst = 0l;
		public long msBest = 0l;
	}

	public static class ResultMap<T> extends LinkedHashMap<T, Integer> {
		private static final long serialVersionUID = 1L;
		public long msFirst = 0l;
		public long msBest = 0l;


		public ResultMap(ResultList<T> list) {
			msFirst = list.msFirst;
			msBest = list.msBest;
		}

		@Override
		public String toString() {
			return msFirst + "/" + msBest + "ms " + super.toString();
		}
	}

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
	 * @param important
	 *          the predicate on vertices besides the source to be present in the
	 *          graph. all vertices are allowed if null. If a vertex is not
	 *          important, it's not in the returned route
	 * @return a new {@link LinkedHashMap} of the vertices, to the distance from
	 *         the previous one in the cycle. The map is linked so iterating it
	 *         will be done in the order of the vertices in the cycle. the source
	 *         is always at the last position.
	 */
	public default <T> ResultMap<T> solve(T source, SimpleGraph<T> graph, Predicate<T> important) {

		Completion<T> complete = graph.complete(source, null, important);
		Indexer<T> idx = complete.index;
		int[][] distances = complete.distances;
		int sourceIdx = idx.position(source);

		Set<Set<T>> deadendsBase = graph.deadEnds();
		Set<Set<Integer>> deadendsComplete = deadendsBase.stream()
				.map(set -> set.stream().filter(important).map(idx::position).collect(Collectors.toSet()))
				.filter(set -> set.size() > 0)
				.collect(Collectors.toSet());

		ResultList<T> list = solve(complete.index, complete.distances, sourceIdx, deadendsComplete);
		ResultMap<T> ret = new ResultMap<>(list);
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
	public default <T> ResultMap<T> solve(SimpleGraph<T> graph, T... allowed) {
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
	public <T> ResultList<T> solve(Indexer<T> index, int[][] distances, int sourceIndex, Set<Set<Integer>> deadends);


}
