package fr.lelouet.tools.regression.simple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** contains informations about the values stored in a dimension */
public class DimensionInformations {

	private static final Logger logger = LoggerFactory
			.getLogger(DimensionInformations.class);

	double minVal = Double.POSITIVE_INFINITY,
			maxVal = Double.NEGATIVE_INFINITY;

	/** to call when a value has been added to the dimension */
	public void addContainedValue(double d) {
		if (d < minVal) {
			minVal = d;
		}
		if (d > maxVal) {
			maxVal = d;
		}
	}

	/** @return true if there are at least two values in the dimension */
	public boolean hasEnoughRange() {
		boolean ret = minVal != Double.POSITIVE_INFINITY
				&& maxVal != Double.NEGATIVE_INFINITY && maxVal != minVal;
		if (!ret) {
			logger.debug(" bad value : min:" + minVal + " , max:" + maxVal);
		}
		return ret;
	}

	public double minVal() {
		return minVal;
	}

	public double maxVal() {
		return maxVal;
	}

	@Override
	public String toString() {
		return "[" + minVal + ";" + maxVal + "]";
	}

}
