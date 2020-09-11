package fr.lelouet.tools.solver.fondhamilton.choco;

import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.lelouet.tools.solver.fondhamilton.ChocoFondHamilton;

public class GreedyOnly extends ChocoFondHamilton {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(GreedyOnly.class);

	public static final GreedyOnly INSTANCE = new GreedyOnly();

	@SuppressWarnings("rawtypes")
	@Override
	protected <T> AbstractStrategy[] addSearch(Modeled<T> model) {
		return new AbstractStrategy[] { stratNextRouteClosest(model), Search.defaultSearch(model.choco) };
	}

}
