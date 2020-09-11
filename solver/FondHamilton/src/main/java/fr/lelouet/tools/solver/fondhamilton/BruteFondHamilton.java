package fr.lelouet.tools.solver.fondhamilton;

import fr.lelouet.tools.solver.IFondHamilton;
import fr.lelouet.tools.solver.Indexer;

/**
 * enumerates all the possible permutation of the index
 *
 * If you have N vertex with a source, then first item is source, you need to
 * iterate over the N-1 other. so that makes (N-1)! possible enumerations.
 *
 */
public class BruteFondHamilton implements IFondHamilton {


	@Override
	public <T> ResultList<T> solve(Indexer<T> index, int[][] distances, int sourceIndex) {
		// TODO Auto-generated method stub
		return null;
	}

}
