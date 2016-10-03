package fr.lelouet.tools.regression;

import java.util.Map;
import java.util.Map.Entry;

public class Data {

	private double output;

	public void setOutput(double val) {
		output = val;
	}

	public double getOutput() {
		return output;
	}

	private Map<String, Double> input;

	public void setInput(Map<String, Double> input) {
		this.input = input;
	}

	public Map<String, Double> getInput() {
		return input;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("[ ");
		sb.append(getOutput()).append(" = _base_");
		for (Entry<String, Double> e : input.entrySet()) {
			sb.append(" + " + e.getValue() + " * " + e.getKey());
		}
		return sb.append(" ]").toString();
	}

}
