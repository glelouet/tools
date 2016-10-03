package fr.lelouet.tools.regression.normalized;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.lelouet.tools.regression.Result;
import fr.lelouet.tools.regression.simple.DimensionInformations;
import fr.lelouet.tools.regression.simple.SimpleSolver;

/**
 * use the linear regression to solve the formula, and then add the importance
 * to the results.<br />
 * the importance is the coefficient from the linear regression, divided by the
 * effective range of the dimension data
 */
public class NormalizingSolver extends SimpleSolver {

	private static final Logger logger = LoggerFactory
			.getLogger(NormalizingSolver.class);

	@Override
	public RelativeResult solve() {
		Result res = super.solve();
		logger.trace("direct result of solving is {}", res);
		RelativeResult rr = new RelativeResult(res);
		for (String s : res.getEstimates().keySet()) {
			DimensionInformations di = dimensions.get(s);
			if (di != null && di.hasEnoughRange()) {
				rr.getRelativeWeights().put(s,
						rr.getEstimates().get(s) * (di.maxVal() - di.minVal()));
			}
		}
		if (getOutputOffset() != 0.0) {
			logger.trace("moving constantEstimate by " + getOutputOffset());
			rr.setConstantEstimate(rr.getConstantEstimate() - getOutputOffset());
		}
		return rr;
	}

	@Override
	public DimensionInformations getOutputInfos() {
		return super.getOutputInfos();
	}

	private double outputOffset = 0.0;

	@Override
	protected double[] convertOutputTabular() {
		/** add the outputoffset if not 0 */
		double[] ret = super.convertOutputTabular();
		if (getOutputOffset() != 0.0) {
			for (int pos = 0; pos < ret.length; pos++) {
				ret[pos] += getOutputOffset();
			}
		}
		return ret;
	}

	/**
	 * specify that all data output must be added a given value.
	 * 
	 * @param toAdd
	 *            the value to add to the data output.
	 */
	public void setOutputOffset(double outputOffset) {
		this.outputOffset = outputOffset;
	}

	public double getOutputOffset() {
		return outputOffset;
	}

}
