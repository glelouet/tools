package fr.lelouet.collectionholders.interfaces.numbers;

import javafx.beans.value.ObservableLongValue;

public interface ObsLongHolder extends ObsNumberHolder<Long, ObsLongHolder> {

	@Override
	public ObservableLongValue asObservableNumber();
}
