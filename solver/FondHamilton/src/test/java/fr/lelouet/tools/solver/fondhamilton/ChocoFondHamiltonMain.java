package fr.lelouet.tools.solver.fondhamilton;

import fr.lelouet.tools.solver.SimpleGraph;

public class ChocoFondHamiltonMain {

	public static void main(String[] args) {
		SimpleGraph<String> graph = SimpleGraph.castle(2);
		System.out.println(ChocoFondHamilton.INSTANCE.solve(graph, "b", "c", "d", "f", "g", "h"));
	}

}
