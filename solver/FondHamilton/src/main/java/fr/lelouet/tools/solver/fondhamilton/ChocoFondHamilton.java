package fr.lelouet.tools.solver.fondhamilton;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.variables.InputOrder;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.search.strategy.strategy.FindAndProve;
import org.chocosolver.solver.search.strategy.strategy.IntStrategy;
import org.chocosolver.solver.search.strategy.strategy.StrategiesSequencer;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.lelouet.tools.solver.IFondHamilton;
import fr.lelouet.tools.solver.Indexer;

public class ChocoFondHamilton implements IFondHamilton {

	private static final Logger logger = LoggerFactory.getLogger(ChocoFondHamilton.class);

	public static final ChocoFondHamilton INSTANCE = new ChocoFondHamilton();

	public static class Modeled<T> {
		public Model choco = new Model();

		//
		// static data
		//
		public Indexer<T> idx;
		public int source;
		public int[][] distances;

		public int[] edgeLength;

		/**
		 * for each vertex, the cluster it is added in, that is the vertex root of
		 * that cluster
		 */
		public int[] clusters;

		/**
		 * the array of vertex indices that are root of a cluster.
		 */
		public int[] clustersRoot;

		public int[] clusterMinIntra;

		public int[] clusterMinInter;

		public int minDistFromClusters;

		//
		// variable data
		//

		/** the route is the series of vertices index */
		public IntVar[] route;

		/**
		 * for each vertex, its position in the route.
		 */
		public IntVar[] positions;

		/**
		 * for each vertex, its previous vertex in the route.
		 */
		public IntVar[] previousIdx;

		/**
		 * for each edge , set to 1 when this edge is used. the edge from vertex i
		 * to vertex j, with j<i, is indexed with (i-1)*i/2+j.
		 */
		public IntVar[] edgeUsed;

		/**
		 * sum of the total distances of the edge used.
		 */
		public IntVar totalDist;
	}

	@Override
	public <T> ResultList<T> solve(Indexer<T> idx, int[][] distances, int sourceIdx) {
		long timeStart = System.currentTimeMillis();
		Modeled<T> model = new Modeled<>();
		model.idx = idx;
		model.distances = distances;
		model.source = sourceIdx;
		addRoute(model);
		addPositions(model);
		addEdges(model);
		addClusters(model);
		addObjective(model);

		model.choco.getSolver().setSearch(addSearch(model));

		ResultList<T> ret = new ResultList<>();
		Solver solver = model.choco.getSolver();
		solver.plugMonitor((IMonitorSolution) () -> {
			if (ret.msFirst == 0l) {
				ret.msFirst = System.currentTimeMillis() - timeStart;
			}
		});
		// solver.showSolutions();
		// solver.showDecisions();
		// solver.showContradiction();
		Solution solution = solver.findOptimalSolution(model.totalDist, false);
		ret.msBest = System.currentTimeMillis() - timeStart;
		Stream.of(model.route).map(iv -> idx.item(solution.getIntVal(iv))).forEach(ret::add);
		return ret;
	}

	@SuppressWarnings("rawtypes")
	protected <T> AbstractStrategy[] addSearch(Modeled<T> model) {
		var choco = model.choco;

		IntStrategy nextRouteClosest = stratNextRouteClosest(model);
		StrategiesSequencer optimalSearch = new StrategiesSequencer(
				// removeHighEdges,
				nextRouteClosest, Search.defaultSearch(choco));
		@SuppressWarnings("unchecked")
		FindAndProve<Variable> fap = new FindAndProve<Variable>(choco.getVars(), (AbstractStrategy) nextRouteClosest,
				optimalSearch);
		return new AbstractStrategy[] { fap };
	}

	protected <T> IntStrategy stratRemoveHighEdges(Modeled<T> model) {
		var edgeLength = model.edgeLength;
		var edgeUsed = model.edgeUsed;

		// remove high edges strategy removes all the edges, from the highest to the
		// lowest edge distance excluxed.
		int lowestEdge = IntStream.of(edgeLength).sorted().limit(1).sum();
		IntVar[] edges_by_length_desc = IntStream.range(0, edgeLength.length).boxed()
				.filter(i -> edgeLength[i] > lowestEdge).sorted((i, j) -> edgeLength[j] - edgeLength[i]).map(i -> edgeUsed[i])
				.toArray(IntVar[]::new);
		return Search.inputOrderLBSearch(edges_by_length_desc);
	}

	protected <T> IntStrategy stratNextRouteClosest(Modeled<T> model) {
		var route = model.route;
		var choco = model.choco;
		var distances = model.distances;

		return Search.intVarSearch(new InputOrder<>(choco), var -> {
			// find the actual index of the variable
			int varIdx = 0;
			for (; varIdx < route.length && route[varIdx] != var; varIdx++) {
			}
			int previous = route[varIdx - 1].getValue();
			int ret = 0;
			int dist = Integer.MAX_VALUE;
			for (int j = 0; j < distances.length; j++) {
				if (var.contains(j) && distances[previous][j] < dist) {
					dist = distances[previous][j];
					ret = j;
				}
			}
			return ret;
		}, route);
	}

	protected <T> IntStrategy stratNextRouteFast(Modeled<T> model) {
		return Search.inputOrderLBSearch(model.route);
	}

	protected <T> void addObjective(Modeled<T> model) {
		var distances = model.distances;
		var idx = model.idx;
		var choco = model.choco;
		var edgeUsed = model.edgeUsed;
		var edgeLength = model.edgeLength;
		var minDistFromClusters = model.minDistFromClusters;

		// find a good min and max values for the objective.
		// get the nth lowest, highest edges with n being the number of systems.
		int sumLowEdges = IntStream.of(edgeLength).sorted().limit(idx.size()).sum();
		int sumHighEdges = IntStream.of(edgeLength).boxed().sorted((i, j) -> j - i).mapToInt(i -> i).limit(idx.size())
				.sum();
		// also, for each system the highest/lowest proximity with another system
		int sumLowProx = 0;
		int sumHigProx = 0;
		for (int i = 0; i < distances.length; i++) {
			int further = 0, closer = Integer.MAX_VALUE;
			for (int j = 0; j < distances.length; j++) {
				if (i != j) {
					int distance = distances[i][j];
					if (distance > further) {
						further = distance;
					}
					if (distance < closer) {
						closer = distance;
					}
				}
			}
			sumLowProx += closer;
			sumHigProx += further;
		}

		int lb = Math.max(sumLowEdges, sumLowProx);
		if (minDistFromClusters > lb) {
			lb = minDistFromClusters;
		}
		int ub = Math.min(sumHighEdges, sumHigProx);
		logger.debug("variable objectif from " + lb + " (prox=" + sumLowProx + " edges=" + sumLowEdges + " clusters="
				+ minDistFromClusters + ") to " + ub + " (prox=" + sumHigProx + " edges=" + sumHighEdges + ")");

		IntVar totalDist = model.totalDist = choco.intVar("totaldist", lb, ub);
		choco.scalar(edgeUsed, edgeLength, "=", totalDist).post();
		choco.sum(edgeUsed, "=", idx.size()).post();
	}

	/**
	 * place constraints on the clusters.
	 * <p>
	 * Let C be a sub-group of the vertices.<br />
	 * Then there are at least two edges that exit this cluster that are used in
	 * the solution.<br />
	 * Also there are at most 2×size of this clusters edges that exit that
	 * cluster.
	 * </p>
	 * <p>
	 * Since with n systems, it means there are 2^n - 2 possible clusters (-2 to
	 * remove empty and full clusters) ; then we can't add constraints for
	 * 2^n.<br />
	 * Instead, we create the clusters of systems that are close together, by
	 * aggregating the systems that are close to a cluster by a distance less than
	 * the median edge distance.
	 * </p>
	 */
	protected <T> void addClusters(Modeled<T> model) {
		var distances = model.distances;
		var idx = model.idx;
		var choco = model.choco;
		var edgeUsed = model.edgeUsed;

		int[] clusters = model.clusters = createClusters(distances, (int) Math.ceil(Math.sqrt(idx.size())));
		int[] clustersRoot = model.clustersRoot = IntStream.of(clusters).distinct().toArray();
		model.minDistFromClusters = 0;
		if (clustersRoot.length > 1) {
			for (int clusterRoot : clustersRoot) {
				int[] clusterSystems = IntStream.range(clusterRoot, idx.size()).filter(sysi -> clusters[sysi] == clusterRoot)
						.toArray();
				logger.debug("cluster [" + idx.item(clusterRoot) + "] systems="
						+ IntStream.of(clusterSystems).mapToObj(idx::item).collect(Collectors.toList()));
				Set<IntVar> exitEdges = new HashSet<>();
				// for each couple i,j with i in the cluster and j not in the cluster.
				for (int i = 0; i < distances.length; i++) {
					if (clusters[i] == clusterRoot) {
						for (int j = 0; j < distances.length; j++) {
							if (clusters[j] != clusterRoot) {
								// same edge value as above
								int edgeIdx = j < i ? i * (i - 1) / 2 + j : j * (j - 1) / 2 + i;
								exitEdges.add(edgeUsed[edgeIdx]);
							}
						}
					}
				}
				logger.debug("cluster [" + idx.item(clusterRoot) + "] exits=" + exitEdges);
				IntVar exitEdgesUsed = choco.intVar("cl_" + idx.item(clusterRoot) + ".exitEdges", 2, distances.length);
				choco.sum(exitEdges.toArray(IntVar[]::new), "=", exitEdgesUsed).post();
			}

			// generate the min distance for systems internal to a cluster, and
			// between
			// system out of a cluster, for each cluster main system.
			int[] clusterMinIntra = model.clusterMinIntra = new int[idx.size()];
			int[] clusterMinInter = model.clusterMinInter = new int[idx.size()];
			for (int i = 0; i < idx.size(); i++) {
				clusterMinIntra[i] = clusterMinInter[i] = Integer.MAX_VALUE;
			}
			for (int i = 0; i < idx.size(); i++) {
				for (int j = 0; j < i; j++) {
					int cl1 = clusters[i], cl2 = clusters[j], dist = distances[i][j];
					if (cl1 == cl2) {
						// update intra if needed
						if (dist < clusterMinIntra[cl1]) {
							clusterMinIntra[cl1] = dist;
						}
					} else {
						// update intra for both if needed
						if (dist < clusterMinInter[cl1]) {
							clusterMinInter[cl1] = dist;
						}
						if (dist < clusterMinInter[cl2]) {
							clusterMinInter[cl2] = dist;
						}
					}
				}
			}
			// now we can sum : for each cluster, the number of systems inside, -1 ;
			// time the inter distance, plus the intra distance.
			// that sum gives us a new minimum for distances
			for (int clusterRoot : clustersRoot) {
				int nb = (int) IntStream.of(clusters).filter(i -> i == clusterRoot).count();
				int inter = clusterMinInter[clusterRoot];
				int intra = 0;
				if (nb > 1) {
					intra = (nb - 1) * clusterMinIntra[clusterRoot];
				}
				logger.debug("cluster " + idx.item(clusterRoot) + "(" + nb + ") add inter=" + inter + " intra=" + intra);
				model.minDistFromClusters += inter + intra;
			}
		}
	}

	protected <T> void addEdges(Modeled<T> model) {
		var idx = model.idx;
		var distances = model.distances;
		var previousIdx = model.previousIdx;
		var choco = model.choco;
		// work by edges. edge (i, j) is used if previous(i)=j or previous(j)=i.
		IntVar[] edgeUsed = model.edgeUsed = new IntVar[idx.size() * (idx.size() - 1) / 2];
		int[] edgeLength = model.edgeLength = new int[edgeUsed.length];
		for (int i = 1; i < idx.size(); i++) {
			for (int j = 0; j < i; j++) {
				// edge(1,0)=0
				// edge(2,0)=1
				// edge(2.1)=2
				int edgeIdx = i * (i - 1) / 2 + j;
				edgeLength[edgeIdx] = distances[i][j];
				edgeUsed[edgeIdx] = choco
						.boolVar(idx.item(i).toString() + "-" + idx.item(j).toString() + "[" + edgeLength[edgeIdx] + "]");
				edgeUsed[edgeIdx].eq(previousIdx[i].eq(j).or(previousIdx[j].eq(i))).post();
			}
		}
	}

	protected <T> void addPositions(Modeled<T> model) {
		var choco = model.choco;
		var idx = model.idx;
		var route = model.route;
		// create the positions for each system
		IntVar[] positions = model.positions = new IntVar[idx.size()];
		// the index of the system in previous position
		IntVar[] previousIdx = model.previousIdx = new IntVar[idx.size()];
		for (int i = 0; i < model.idx.size(); i++) {
			if (i == model.source) {
				positions[i] = choco.intVar(0);
				previousIdx[i] = route[model.idx.size() - 1];
			} else {
				positions[i] = choco.intVar(idx.item(i).toString() + "_index", 1, idx.size() - 1, false);
				choco.element(choco.intVar(i), route, positions[i], 0).post();
				previousIdx[i] = choco.intVar(0, idx.size() - 1, false);
				choco.element(previousIdx[i], route, positions[i], 1).post();
			}
		}
	}

	/** add the route to the choco model */
	protected <T> void addRoute(Modeled<T> model) {
		// make the route as the indexes. first one must be the source.
		IntVar[] route = model.route = new IntVar[model.idx.size()];
		for (int i = 0; i < model.idx.size(); i++) {
			if (i == 0) {
				route[i] = model.choco.intVar("route_" + i, model.source);
			} else {
				route[i] = model.choco.intVar("route_" + i, 0, model.idx.size() - 1, false);
			}
		}
		// force the ordering of the cycle : first index after the source is < last
		// index
		// that means we allow source, 1, 2 ; but not source, 2, 1.
		if (model.idx.size() > 2) {
			route[model.idx.size() - 1].lt(route[1]).post();
		}
		// each system appears only once
		model.choco.allDifferent(route).post();

	}

	public static int[] createClusters(int[][] distances, int maxClusters) {
		int[] clustering = new int[distances.length];
		// we start with each system being its own cluster
		for (int i = 0; i < clustering.length; i++) {
			clustering[i] = i;
		}
		int[] edgesLength = Stream.of(distances).flatMapToInt(iArr -> IntStream.of(iArr)).sorted().distinct()
				.filter(i -> i > 0).toArray();
		logger.debug("clustering " + distances.length + " systems into max " + maxClusters + " clusters ; edges length are "
				+ IntStream.of(edgesLength).boxed().collect(Collectors.toList()));
		long nbClusters = IntStream.of(clustering).distinct().count();
		for (int edgeIndex = 0; edgeIndex < edgesLength.length && nbClusters > maxClusters; edgeIndex++) {
			int edgeLength = edgesLength[edgeIndex];
			for (int i = 0; i < distances.length; i++) {
				for (int j = 0; j < i; j++) {
					if (distances[i][j] == edgeLength && clustering[i] != clustering[j]) {
						// we merge the two clusters
						int replaced = Math.max(clustering[i], clustering[j]);
						int newval = Math.min(clustering[i], clustering[j]);
						for (int k = 0; k < clustering.length; k++) {
							if (clustering[k] == replaced) {
								clustering[k] = newval;
							}
						}
					}
				}
			}
			nbClusters = IntStream.of(clustering).distinct().count();
			logger.debug("after mergin all clusters distant by " + edgeLength + " ; keep " + nbClusters + " clusters");
		}
		return clustering;
	}

}
