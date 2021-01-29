package fr.lelouet.collectionholders.interfaces.numbers;

import java.util.function.Consumer;

/**
 * Integer holder. Only difference is, its {@link #asObservableNumber()} returns
 * an {@link ObservableIntegerValue} .
 *
 */
public interface ObsIntHolder extends ObsNumberHolder<Integer, ObsIntHolder> {

	@Override
	ObsIntHolder peek(Consumer<Integer> observer);

}
