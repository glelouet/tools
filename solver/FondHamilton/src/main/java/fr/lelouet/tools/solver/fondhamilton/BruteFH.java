package fr.lelouet.tools.solver.fondhamilton;

import java.util.Set;

import fr.lelouet.tools.solver.IFondHamilton;
import fr.lelouet.tools.solver.Indexer;

/**
 * enumerates all the possible permutation of the index
 *
 * If you have N vertex with a source, then first item is source, you need to
 * iterate over the N-1 other. so that makes (N-1)! possible enumerations.
 *
 */
public class BruteFH implements IFondHamilton {


	@Override
	public <T> ResultList<T> solve(Indexer<T> index, int[][] distances, int sourceIndex, Set<Set<Integer>> deadends) {
		// TODO Auto-generated method stub
		return null;
	}

}
