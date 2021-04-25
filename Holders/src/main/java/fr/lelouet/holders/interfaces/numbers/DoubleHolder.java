package fr.lelouet.holders.interfaces.numbers;

/**
 * Double holder. It has a ceil, floor functions to translate into int value and
 * return an {@link ObservableDoubleValue} for its {@link #asObservableNumber()}
 */
public interface DoubleHolder extends NumberHolder<Double, DoubleHolder> {

	public IntHolder ceil();

	public IntHolder floor();

}
