package fr.lelouet.collectionholders.interfaces.numbers;

import java.util.function.Consumer;

/**
 * Double holder. It has a ceil, floor functions to translate into int value and
 * return an {@link ObservableDoubleValue} for its {@link #asObservableNumber()}
 */
public interface ObsDoubleHolder extends ObsNumberHolder<Double, ObsDoubleHolder> {

	public ObsIntHolder ceil();

	public ObsIntHolder floor();

	@Override
	ObsDoubleHolder peek(Consumer<Double> observer);

}
