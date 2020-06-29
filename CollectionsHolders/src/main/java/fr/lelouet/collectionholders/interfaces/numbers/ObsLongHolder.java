package fr.lelouet.collectionholders.interfaces.numbers;

import java.util.function.Consumer;

import javafx.beans.value.ObservableLongValue;

/**
 * Long holder. Only difference is, its {@link #asObservableNumber()} returns an
 * {@link ObservableLongValue} .
 *
 */
public interface ObsLongHolder extends ObsNumberHolder<Long, ObsLongHolder> {

	@Override
	public ObservableLongValue asObservableNumber();

	@Override
	ObsLongHolder peek(Consumer<Long> observer);

}
