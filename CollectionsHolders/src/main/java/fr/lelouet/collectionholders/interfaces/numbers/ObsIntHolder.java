package fr.lelouet.collectionholders.interfaces.numbers;

import java.util.function.Consumer;

import javafx.beans.value.ObservableIntegerValue;

/**
 * Integer holder. Only difference is, its {@link #asObservableNumber()} returns
 * an {@link ObservableIntegerValue} .
 *
 */
public interface ObsIntHolder extends ObsNumberHolder<Integer, ObsIntHolder> {

	@Override
	public ObservableIntegerValue asObservableNumber();

	@Override
	ObsIntHolder peek(Consumer<Integer> observer);

}
