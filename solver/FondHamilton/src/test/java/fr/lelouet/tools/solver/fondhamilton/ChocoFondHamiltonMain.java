package fr.lelouet.tools.solver.fondhamilton;

import fr.lelouet.tools.solver.SimpleGraph;

public class ChocoFondHamiltonMain {

	public static void main(String[] args) {
		SimpleGraph<String> graph = SimpleGraph.castle(3);
		ChocoFondHamilton.INSTANCE.solve(graph);
	}

}
