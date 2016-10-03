package fr.lelouet.tools.regression.simple;

import java.util.*;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import fr.lelouet.tools.regression.Data;
import fr.lelouet.tools.regression.Result;
import fr.lelouet.tools.regression.Solver;

public class SimpleSolverTest {

	private static final Logger logger = LoggerFactory
			.getLogger(SimpleSolverTest.class);

	public static interface MapEvaluator {
		double eval(Map<String, Double> vector);
	}

	/** generate a list of ("x", "y", "z") values. */
	public static List<Map<String, Double>> generateSimpleXYZValues() {
		List<Map<String, Double>> xvals = new ArrayList<Map<String, Double>>();
		Map<String, Double> xdata = new HashMap<String, Double>();
		xdata.put("x", 0.0);
		xdata.put("y", 0.0);
		xdata.put("z", 0.0);
		xvals.add(xdata);
		xdata = new HashMap<String, Double>();
		xdata.put("x", 2.0);
		xdata.put("y", 0.0);
		xdata.put("z", 0.0);
		xvals.add(xdata);
		xdata = new HashMap<String, Double>();
		xdata.put("x", 0.0);
		xdata.put("y", 2.0);
		xdata.put("z", 0.0);
		xvals.add(xdata);
		xdata = new HashMap<String, Double>();
		xdata.put("x", 0.0);
		xdata.put("y", 0.0);
		xdata.put("z", 2.0);
		xvals.add(xdata);
		xdata = new HashMap<String, Double>();
		xdata.put("x", 2.0);
		xdata.put("y", 2.0);
		xdata.put("z", 2.0);
		xvals.add(xdata);
		return xvals;
	}

	public static void feedSolverWithValues(Solver s, MapEvaluator e,
			Map<String, Double>... values) {
		for (Map<String, Double> data : values) {
			s.addData(data, e.eval(data));
		}
	}

	/**
	 * generate an evaluator that makes linear sum. ie, if the coeffs is "x:2",
	 * "y:3", it will return an evaluator which will evaluate a map m to
	 * 2*m.get("x") + 3*m.get("y") + constval;
	 */
	public static MapEvaluator generateLinearEvaluator(final double constval,
			String... coeffs) {
		final HashMap<String, Double> coefsmap = new HashMap<String, Double>();
		for (String s : coeffs) {
			int pos = s.indexOf(":");
			if (pos > 0) {
				String dimension = s.substring(0, pos);
				String coeff = s.substring(pos + 1, s.length());
				double dcoeff = Double.parseDouble(coeff);
				coefsmap.put(dimension, dcoeff);
			}
		}
		logger.debug("generating linear coeff map : " + coefsmap
				+ " with const val : " + constval);
		return new MapEvaluator() {

			@Override
			public double eval(Map<String, Double> vector) {
				double val = constval;
				for (Entry<String, Double> e : coefsmap.entrySet()) {
					if (e.getValue() != null) {
						Double dimval = vector.get(e.getKey());
						if (dimval != null) {
							val += e.getValue() * dimval;
						}
					}
				}
				return val;
			}

		};
	}

	/** simple test with f=0.5*x+2*y+z */
	@SuppressWarnings("unchecked")
	@Test
	public void simpleLinear() {
		SimpleSolver s = new SimpleSolver();
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

		Result r = s.solve();

		double xcoef = r.getEstimates().get("x");
		double ycoef = r.getEstimates().get("y");
		double zcoef = r.getEstimates().get("z");
		double constant = r.getConstantEstimate();

		logger.debug(
				"solving linear multivar regression with f= x*{} + y*{} + z*{} + {} gives : f = x*{} + y*{} + z*{} + {}",
				new Object[] { realXCoef, realYCoef, realZCoef, realConstant,
						xcoef, ycoef, zcoef, constant });

		Assert.assertTrue(xcoef < realXCoef + 0.1 && xcoef > realXCoef - 0.1);
		Assert.assertTrue(ycoef < realYCoef + 0.1 && ycoef > realYCoef - 0.1);
		Assert.assertTrue(zcoef < realZCoef + 0.1 && zcoef > realZCoef - 0.1);
		Assert.assertTrue(constant < realConstant + 0.1
				&& constant > realConstant - 0.1);

	}

	@Test
	public void withNotEnoughDimensions() {
		SimpleSolver s = new SimpleSolver();
		s.setDimensions("x", "y", "z");
		Assert.assertEquals(s.getDimensions().size(), 3);
		Map<String, Double> incompleteData = new HashMap<String, Double>();
		incompleteData.put("x", 0.0);
		incompleteData.put("y", 0.0);
		Data added = s.addData(incompleteData, 0.0);
		Assert.assertFalse(added == null);
		Assert.assertEquals(s.getDimensions().size(), 2);
		Assert.assertEquals(s.getDataNumber(), 1);
	}

	/** test if a solver with incomplete data knows it can't complete itself */
	@Test
	public void unCompleteDimensions() {
		SimpleSolver s = new SimpleSolver();
		s.setDimensions("x", "y", "z");
		Assert.assertEquals(s.getUncompleteDimensions(), new HashSet<String>(
				Arrays.asList(new String[] { "x", "y", "z" })));
		Map<String, Double> input;

		// add (0.0, 0.0, 0.0)-> 0.0
		input = new HashMap<String, Double>();
		input.put("x", 0.0);
		input.put("y", 0.0);
		input.put("z", 0.0);
		s.addData(input, 0.0);
		Assert.assertEquals(s.getUncompleteDimensions(), new HashSet<String>(
				Arrays.asList(new String[] { "x", "y", "z" })));
		Assert.assertFalse(s.enoughOutputValues());
		// add (5.0, 0.0, 0.0)-> 0.0
		input.put("x", 5.0);
		s.addData(input, 0.0);
		Assert.assertEquals(s.getUncompleteDimensions(), new HashSet<String>(
				Arrays.asList(new String[] { "y", "z" })));
		Assert.assertFalse(s.enoughOutputValues());

		// add (5.0, 3.0, 0.0)-> 5.0
		input.put("y", 3.0);
		s.addData(input, 5.0);
		Assert.assertEquals(s.getUncompleteDimensions(), new HashSet<String>(
				Arrays.asList(new String[] { "z" })));
		Assert.assertTrue(s.enoughOutputValues());

		// add (6.0, 3.0, 0.0)-> 7.0
		input.put("x", 6.0);
		s.addData(input, 7.0);
		Assert.assertEquals(s.getUncompleteDimensions(), new HashSet<String>(
				Arrays.asList(new String[] { "z" })));
		Assert.assertTrue(s.enoughOutputValues());

		s.removeDimensions(s.getUncompleteDimensions());
		Assert.assertTrue(s.getUncompleteDimensions().isEmpty());
		Result r = s.solve();
		logger.debug(
				"solving returns weights {}, constant {}, and errors {}",
				new Object[] { r.getEstimates(), r.getConstantEstimate(),
						r.getErrors() });
	}

}
