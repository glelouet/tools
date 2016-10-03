package fr.lelouet.tools.regression.normalized;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import fr.lelouet.tools.regression.simple.SimpleSolverTest;

public class NormalizingSolverTest {

	private static final Logger logger = LoggerFactory
			.getLogger(NormalizingSolverTest.class);

	@SuppressWarnings("unchecked")
	@Test
	public void simpleTest() {

		NormalizingSolver s = new NormalizingSolver();
		s.setDimensions("x", "y", "z");

		final double realXCoef = 0.5;
		final double realYCoef = 5;
		final double realZCoef = 7;
		final double realConstant = -3;

		SimpleSolverTest.feedSolverWithValues(s, SimpleSolverTest
				.generateLinearEvaluator(realConstant, "x:" + realXCoef, "y:"
						+ realYCoef, "z:" + realZCoef), SimpleSolverTest
				.generateSimpleXYZValues().toArray(new Map[] {}));

		Assert.assertEquals(s.getDimensions().size(), 3);
		Assert.assertEquals(s.getDataNumber(), 5);

		RelativeResult r = s.solve();
		logger
				.debug(
						"solving linear multivar regression with f= x*{} + y*{} + z*{} + {} gives relative weigth {}",
						new Object[] { realXCoef, realYCoef, realZCoef,
								realConstant, r.getRelativeWeights() });
	}

}
