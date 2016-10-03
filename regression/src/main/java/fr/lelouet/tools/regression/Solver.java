package fr.lelouet.tools.regression;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * solve the linear relations between dimensions and a result
 * <p>
 * The use case is :
 * <ol>
 * <li></li>
 * </ol>
 * </p>
 * 
 * @author Guillaume Le Louët
 * 
 */
public interface Solver {

	/**
	 * set the dimensions to use. Note that this can discard any added data that
	 * does not contain any of those dimensions, or reduce the added data.
	 * 
	 * @param dimensions
	 */
	void setDimensions(String... dimensions);

	/**
	 * 
	 * @return a new set of dimensions that are used in the model.
	 */
	Set<String> getDimensions();

	/** remove a set of dimensions from the used dimensions */
	void removeDimensions(Set<String> toRemove);

	/**
	 * add the data and modifies the internal list of dimensions used
	 * 
	 * @param input
	 *            the input vector
	 * @param output
	 *            the result of the input vector
	 * 
	 * @return the corresponding added data, or null if not correct
	 */
	Data addData(Map<String, Double> input, double output);

	/**
	 * @return the number of added data
	 */
	int getDataNumber();

	/**
	 * @return the internal list of data added. Note that the returned list can
	 *         be directly modified, and potentially result in a loss of
	 *         consistency
	 */
	List<Data> getData();

	/**
	 * @return a new set of the dimensions that have not enough data and should
	 *         be discarded to be able to solve the model
	 */
	Set<String> getUncompleteDimensions();

	/** @return true if there are enough values in the data output */
	boolean enoughOutputValues();

	/** solve the problem with the smallest sum of square error it can find. */
	Result solve();

}
