package fr.lelouet.tools.regression.simple;

import java.util.*;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import flanagan.analysis.Regression;
import fr.lelouet.tools.regression.Data;
import fr.lelouet.tools.regression.Result;
import fr.lelouet.tools.regression.Solver;

public class SimpleSolver implements Solver {

	private static final Logger logger = LoggerFactory
			.getLogger(SimpleSolver.class);

	protected Map<String, DimensionInformations> dimensions = null;

	private DimensionInformations outputInfos = new DimensionInformations();

	protected DimensionInformations getOutputInfos() {
		return outputInfos;
	}

	private List<Data> data = new ArrayList<Data>();

	/**
	 * add a created data, and uses its internal values
	 * 
	 * @param dat
	 *            the data to add. Directly copied in memory
	 */
	private void addCopiedData(Data dat) {
		for (Entry<String, Double> e : dat.getInput().entrySet()) {
			dimensions.get(e.getKey()).addContainedValue(e.getValue());
		}
		outputInfos.addContainedValue(dat.getOutput());
		data.add(dat);
	}

	/**
	 * copy a map of input dimensions, and an output value, into the internal
	 * memory with no check
	 * 
	 * @param input
	 *            the input of the data to add.
	 * @param output
	 *            the output of the data to add.
	 */
	protected Data directAddData(Map<String, Double> input, double output) {
		Data ret = new Data();
		Map<String, Double> vals = new HashMap<String, Double>();
		vals.putAll(input);
		ret.setInput(vals);
		ret.setOutput(output);
		addCopiedData(ret);
		return ret;
	}

	/**
	 * copy data that may contain dimensions not to be registered. The excessive
	 * dimensions are discarded.
	 * 
	 * @param input
	 *            the map of dimensions to values as the input of the data to
	 *            add.
	 * @param output
	 *            the output of the data to add
	 * @return the real added data, that is the data with discarded dimensions.
	 */
	protected Data addExcessiveData(Map<String, Double> input, double output) {
		Data ret = new Data();
		Map<String, Double> vals = new HashMap<String, Double>();
		vals.putAll(input);
		vals.keySet().retainAll(dimensions.keySet());
		ret.setInput(vals);
		ret.setOutput(output);
		addCopiedData(ret);
		return ret;
	}

	@Override
	public void removeDimensions(Set<String> toRemove) {
		dimensions.keySet().removeAll(toRemove);
	}

	protected void handleInputSet(Map<String, Double> input) {
		if (dimensions == null) {
			setDimensions(input.keySet().toArray(
					new String[input.keySet().size()]));
		} else {
			Set<String> lostDimensions = new HashSet<String>(getDimensions());
			lostDimensions.removeAll(input.keySet());
			if (!lostDimensions.isEmpty()) {
				logger.debug(
						"discarding dimensions <{}> when adding input <{}> ",
						lostDimensions, input);
			}
			removeDimensions(lostDimensions);
		}
	}

	@Override
	public Data addData(Map<String, Double> input, double output) {
		handleInputSet(input);
		Data added = addExcessiveData(input, output);
		return added;
	}

	@Override
	public List<Data> getData() {
		return data;
	}

	@Override
	public int getDataNumber() {
		return getData().size();
	}

	@Override
	public Set<String> getDimensions() {
		if (dimensions == null) {
			return Collections.emptySet();
		}
		return Collections.unmodifiableSet(dimensions.keySet());
	}

	@Override
	public void setDimensions(String... newDimensions) {
		if (dimensions == null) {
			dimensions = new HashMap<String, DimensionInformations>();
		}
		Set<String> toRemove = new HashSet<String>();
		List<String> newDimensionsList = Arrays.asList(newDimensions);
		toRemove.addAll(getDimensions());
		toRemove.removeAll(newDimensionsList);
		removeDimensions(toRemove);
		/**
		 * we removed all the dimensions that were not in newDimensions : We
		 * have at most newDimensions.length dimensions remaining, so either we
		 * have that number of data, and then we can keep the list of data
		 * because we only have useless data (removed dimensions) in the data
		 * list, or we have less remaining dimensions, in this case we need to
		 * add dimensions, but the data may contain holes for already added data
		 * : we thus clear the data list.
		 */
		if (newDimensions.length != getDimensions().size()) {
			data.clear();
			dimensions.clear();
			for (String s : newDimensionsList) {
				dimensions.put(s, new DimensionInformations());
			}
		}

	}

	@Override
	public Result solve() {
		Map<String, Integer> positions = new HashMap<String, Integer>();
		for (String s : getDimensions()) {
			positions.put(s, positions.size());
		}
		logger.debug("positions when solving : {}", positions);
		double[][] xdata = convertInputTabular(positions);
		double[] ydata = convertOutputTabular();
		Regression reg = solveTabular(xdata, ydata);
		Result r = new Result();
		for (String s : getDimensions()) {
			int pos = positions.get(s) + 1;
			r.getEstimates().put(s, reg.getBestEstimates()[pos]);
			r.getErrors().put(s, reg.getBestEstimatesErrors()[pos]);
		}
		r.setConstantEstimate(reg.getBestEstimates()[0]);
		return r;
	}

	/**
	 * put the data outputs in an array of doubles.
	 * 
	 * @return a double dimension array of the inputs with format
	 *         outputs[dimensionNumber][snapshotNumber], snapshotNumber being
	 *         the numeral order of added data.(firs is 0, third is 2, etc…)
	 * @param positions
	 *            the map of each dimension to the index it must be stored in
	 *            the returned array (dimensionName -> dimensionNumber)
	 */
	protected double[][] convertInputTabular(Map<String, Integer> positions) {
		double[][] xdata = new double[positions.size()][getDataNumber()];
		for (int dataNum = 0; dataNum < data.size(); dataNum++) {
			Data d = data.get(dataNum);
			for (String s : getDimensions()) {
				int dimensionNum = positions.get(s);
				xdata[dimensionNum][dataNum] = d.getInput().get(s);
			}
		}
		return xdata;
	}

	/**
	 * put the data inputs in a array of doubles
	 * 
	 * @return the array of input values from the data, in the order they were
	 *         added
	 */
	protected double[] convertOutputTabular() {
		double[] ydata = new double[getDataNumber()];
		for (int dataNum = 0; dataNum < data.size(); dataNum++) {
			Data d = data.get(dataNum);
			ydata[dataNum] = d.getOutput();
		}
		return ydata;
	}

	protected Regression solveTabular(double[][] xdata, double[] ydata) {
		Regression reg = new Regression(xdata, ydata);
		// reg.ignoreDofFcheck();
		try {
			reg.linear();
		} catch (Exception e) {
			if (xdata.length > 0) {
				StringBuilder sb = new StringBuilder("<table>\n");
				for (int i = 0; i < xdata[0].length; i++) {
					sb.append(" <tr>");
					for (double[] element : xdata) {
						sb.append("<td>").append(element[i]).append("</td>");
					}
					sb.append("<td /><td>").append(ydata[i]).append("</td></tr>\n");
				}
				sb.append("</table>");
				logger.debug("while solving : \n" + sb, e);
				reg.ignoreDofFcheck();
				reg.linear();
			}
		}
		return reg;
	}

	@Override
	public Set<String> getUncompleteDimensions() {
		Set<String> ret = new HashSet<String>();
		for (Entry<String, DimensionInformations> e : dimensions.entrySet()) {
			if (!e.getValue().hasEnoughRange()) {
				ret.add(e.getKey());
			}
		}
		return ret;
	}

	@Override
	public boolean enoughOutputValues() {
		logger.trace("dimension info for ouput : {}", outputInfos);
		return outputInfos.hasEnoughRange();
	}

}
