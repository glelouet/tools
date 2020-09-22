package fr.lelouet.tools.solver.fondhamilton;

import fr.lelouet.tools.solver.IFondHamilton;
import fr.lelouet.tools.solver.SimpleGraph;
import fr.lelouet.tools.solver.fondhamilton.choco.GreedyOnly;

public class ChocoFondHamiltonMain {

	public static void main(String[] args) {
		IFondHamilton[] solvers = { GreedyOnly.INSTANCE, ChocoFH.INSTANCE };
		for (int i = 2; i < 5; i++) {
			System.out.println("" + i + ":");
			SimpleGraph<String> graph = SimpleGraph.castle(i);
			String[] allowed = null;
			for (IFondHamilton s : solvers) {
				System.out.println("\t" + s.getClass().getSimpleName() + " : " + s.solve(graph, allowed));
			}
		}
	}

}
