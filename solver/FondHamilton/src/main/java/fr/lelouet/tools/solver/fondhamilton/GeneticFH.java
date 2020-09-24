package fr.lelouet.tools.solver.fondhamilton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.lelouet.tools.solver.IFondHamilton;
import fr.lelouet.tools.solver.Indexer;

public class GeneticFH implements IFondHamilton {

	private static final Logger logger = LoggerFactory.getLogger(GeneticFH.class);

	public long seed = 1;

	public static class Solution {
		public int[] ordering;
		public int size;
		private int hashcode = 0;

		void evaluate(int source, int[][] distances) {
			// we ensure all solutions have their first index lower than their last.
			if (ordering[0] > ordering[ordering.length - 1]) {
				for (int i = 0; i < ordering.length / 2; i++) {
					int temp = ordering[i];
					ordering[i] = ordering[ordering.length - i - 1];
					ordering[ordering.length - i - 1] = temp;
				}
			}
			size = 0;
			hashcode = ordering.length;
			int last = source;
			for (int i : ordering) {
				size += distances[i][last];
				last = i;
				hashcode += i;
			}
			size += distances[last][source];
		}

		@Override
		public int hashCode() {
			return hashcode;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Solution) {
				Solution other = (Solution) obj;
				return Arrays.compare(ordering, other.ordering) == 0;
			}
			return false;
		}
	}


	public static Solution init(Random rd, int source, int[][] distances) {
		Solution ret = new Solution();
		ret.ordering = new int[distances.length - 1];
		for (int i = 0; i < ret.ordering.length; i++) {
			ret.ordering[i] = i >= source ? i + 1 : i;
		}
		ret.evaluate(source, distances);
		return ret;
	}

	public static <T> Solution fromGreedy(Indexer<T> idx, int[][] distances, int sourceIdx, Set<Set<Integer>> deadends) {
		Solution ret = new Solution();
		ret.ordering = GreedyDeadEndFH.INSTANCE.solve(idx, distances, sourceIdx, deadends).stream().skip(1)
				.mapToInt(idx::position)
				.toArray();
		ret.evaluate(sourceIdx, distances);
		return ret;
	}

	public static Solution mutate(Solution from, Random rd, int source, int[][] distances) {
		Solution ret = new Solution();
		ret.ordering = Arrays.copyOf(from.ordering, from.ordering.length);
		int i1 = rd.nextInt(ret.ordering.length - 1);
		int i2 = i1 + 1;
		int tmp = ret.ordering[i1];
		ret.ordering[i1] = ret.ordering[i2];
		ret.ordering[i2] = tmp;
		ret.evaluate(source, distances);
		return ret;
	}

	public static Solution merge(Solution start, Solution end, int source, int[][] distances) {
		Solution ret = new Solution();
		ret.ordering = Arrays.copyOf(start.ordering, start.ordering.length);
		int nextIndex = ret.ordering.length / 2;
		for (int toPlace : end.ordering) {
			boolean add = true;
			for (int i = 0; i < nextIndex; i++) {
				if (ret.ordering[i] == toPlace) {
					add = false;
					break;
				}
			}
			if (add) {
				ret.ordering[nextIndex] = toPlace;
				nextIndex++;
			}
		}
		ret.evaluate(source, distances);
		return ret;
	}

	@Override
	public <T> ResultList<T> solve(Indexer<T> idx, int[][] distances, int sourceIdx, Set<Set<Integer>> deadends) {
		ResultList<T> ret = new ResultList<>();
		long start = System.currentTimeMillis();
		Random rd = new Random(seed);
		List<Solution> pool = new ArrayList<>();
		pool.add(init(rd, sourceIdx, distances));
		pool.add(fromGreedy(idx, distances, sourceIdx, deadends));
		int poolSize = idx.size();
		Set<Solution> toAdd = new HashSet<>();
		Solution bestFound = null;
		int generationsWithoutImprovement = 0;
		int maxGenWithoutImprovement = 1 + (int) Math.sqrt(idx.size());
		for (int generation = 0; generationsWithoutImprovement <= maxGenWithoutImprovement; generation++) {
			toAdd.clear();
			for (int j = 0; j < poolSize; j++) {
				toAdd.add(mutate(pool.get(rd.nextInt(pool.size())), rd, sourceIdx, distances));
			}
			if (pool.size() > 1) {
				for (int j = 0; j < poolSize; j++) {
					toAdd.add(merge(pool.get(rd.nextInt(pool.size())), pool.get(rd.nextInt(pool.size())), sourceIdx, distances));
				}
			}
			logger.debug("generation " + generation + " children=" + toAdd.size());
			// ensure unicity
			toAdd.addAll(pool);
			pool.clear();
			pool.addAll(toAdd);
			// only keep the best
			Collections.sort(pool, Comparator.comparing(s -> s.size));
			while (pool.size() > poolSize) {
				pool.remove(poolSize);
			}
			Solution best = pool.get(0);
			logger.debug(" generation=" + generation + " population=" + pool.size() + " best=" + best.size
					+ " worse="
					+ pool.get(pool.size() - 1).size);
			if (bestFound == null) {
				bestFound = best;
				ret.msFirst = ret.msBest = System.currentTimeMillis() - start;
			}
			if (best.size < bestFound.size) {
				ret.msBest = System.currentTimeMillis() - start;
				bestFound = best;
				generationsWithoutImprovement = 0;
			} else {
				generationsWithoutImprovement++;
			}
		}
		IntStream.of(pool.get(0).ordering).mapToObj(idx::item).forEach(ret::add);
		return ret;
	}

}
