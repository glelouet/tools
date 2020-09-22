package fr.lelouet.tools.solver.fondhamilton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fr.lelouet.tools.solver.IFondHamilton;
import fr.lelouet.tools.solver.Indexer;

public class GreedyFH implements IFondHamilton {

	public static final GreedyFH INSTANCE = new GreedyFH();

	public static class Edge {
		public int i, j;
		public int dist;
	}

	public <T> List<? extends Edge> makeEdgesList(Indexer<T> idx, int[][] distances, Set<Set<Integer>> deadends) {
		List<Edge> ret = new ArrayList<>();
		for (int i = 0; i < idx.size(); i++) {
			for (int j = 0; j < i; j++) {
				Edge edge = new Edge();
				edge.i = i;
				edge.j = j;
				edge.dist = distances[i][j];
				ret.add(edge);
			}
		}
		Comparator<Edge> comp = Comparator.comparing(edge -> edge.dist);
		Collections.sort(ret,comp);
		return ret;
	}

	@Override
	public <T> ResultList<T> solve(Indexer<T> idx, int[][] distances, int sourceIdx, Set<Set<Integer>> deadends) {
		long timeStart = System.currentTimeMillis();
		List<? extends Edge> edges = makeEdgesList(idx, distances, deadends);
		Set<Integer> reached = new HashSet<>();
		Set<Integer> inside = new HashSet<>();
		Edge edge = edges.get(0);
		reached.add(edge.i);
		reached.add(edge.j);
		List<Edge> accepted = new ArrayList<>();
		accepted.add(edge);
		edges.remove(edge);
		while (inside.size() < idx.size() - 2) {
			// System.err.println("searching edge for idx " + reached + " out of " +
			// inside);
			edge = edges.stream().filter(e -> reached.contains(e.i) && !reached.contains(e.j) && !inside.contains(e.j)
					|| reached.contains(e.j) && !reached.contains(e.i) && !inside.contains(e.i)).findFirst().get();
			accepted.add(edge);
			if (reached.contains(edge.i)) {
				reached.remove(edge.i);
				inside.add(edge.i);
				reached.add(edge.j);
			} else {
				reached.remove(edge.j);
				inside.add(edge.j);
				reached.add(edge.i);
			}
			edges.remove(edge);
		}
		edge = new Edge();
		Integer[] remain = reached.toArray(Integer[]::new);
		edge.i = remain[0];
		edge.j = remain[1];
		accepted.add(edge);
		ResultList<T> ret = new ResultList<>();
		ret.add(idx.item(sourceIdx));
		int lastIdx = sourceIdx;
		Edge lastEdge = null;
		while (ret.size() < idx.size()) {
			edge = null;
			for (Edge edge2 : accepted) {
				if (edge2 != lastEdge && (edge2.i == lastIdx || edge2.j == lastIdx)) {
					edge = edge2;
				}
			}
			if (edge.i == lastIdx) {
				lastIdx = edge.j;
			} else {
				lastIdx = edge.i;
			}
			lastEdge = edge;
			ret.add(idx.item(lastIdx));
		}
		ret.msBest = ret.msFirst = System.currentTimeMillis() - timeStart;
		return ret;
	}

}
