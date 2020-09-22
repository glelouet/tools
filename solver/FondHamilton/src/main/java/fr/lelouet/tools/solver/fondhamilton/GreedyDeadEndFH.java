package fr.lelouet.tools.solver.fondhamilton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.lelouet.tools.solver.Indexer;

/**
 * A {@link GreedyFH} that uses the deadends to sort the edges, as well as the
 * smallest degree of the vertices of an edge.
 *
 * @author glelouet
 *
 */
public class GreedyDeadEndFH extends GreedyFH {

	public static final GreedyDeadEndFH INSTANCE = new GreedyDeadEndFH();

	public static class Edge extends GreedyFH.Edge {
		/**
		 * smallest size of a deadend the two vertices are in.
		 */
		public int deadEndSize = Integer.MAX_VALUE;

		public int lowestDegree = Integer.MAX_VALUE;
	}

	@Override
	public <T> List<Edge> makeEdgesList(Indexer<T> idx, int[][] distances, Set<Set<Integer>> deadends) {
		List<Edge> edges = new ArrayList<>();
		List<Set<Integer>> sortedDeadends = new ArrayList<>(deadends);
		Collections.sort(sortedDeadends, Comparator.comparing(Set::size));
		Map<Integer, Integer> monoDeadendRoot = new HashMap<>();
		for (Set<Integer> set : sortedDeadends) {
			if (set.size() > 1) {
				break;
			}
			int index = set.iterator().next();
			int closestDist = Integer.MAX_VALUE;
			int closestIndex = index;
			for (int j = 0; j < distances.length; j++) {
				if (j != index && distances[index][j] < closestDist) {
					closestDist = distances[index][j];
					closestIndex = j;
				}
			}
			monoDeadendRoot.put(index, closestIndex);
		}
		for (int i = 0; i < idx.size(); i++) {
			for (int j = 0; j < i; j++) {
				Edge edge = new Edge();
				edge.i = i;
				edge.j = j;

				// distance between the two vertices
				edge.dist = distances[i][j];

				// smallest size of the dead end group that contains both vertices.
				if (monoDeadendRoot.getOrDefault(i, -1) == j || monoDeadendRoot.getOrDefault(j, -1) == i) {
					edge.deadEndSize = 1;
				} else {
					for (Set<Integer> deadend : sortedDeadends) {
						if (deadend.contains(i) && deadend.contains(j)) {
							edge.deadEndSize = deadend.size();
							break;
						}
					}
				}

				// smallest degree of the two vertices. degree is thenumber of vertices
				// distant by one from a vertex.
				int di = 0;
				int dj = 0;
				for (int possibleadj = 0; possibleadj < idx.size(); possibleadj++) {
					if (distances[i][possibleadj] == 1) {
						di++;
					}
					if (distances[j][possibleadj] == 1) {
						dj++;
					}
				}
				edge.lowestDegree = Math.min(di, dj);

				edges.add(edge);
			}
		}
		Comparator<Edge> comp = Comparator.comparing(edge -> edge.deadEndSize);
		comp = comp.thenComparing(edge -> edge.dist);
		comp = comp.thenComparing(edge -> edge.lowestDegree);
		Collections.sort(edges,comp);
		return edges;
	}

}
