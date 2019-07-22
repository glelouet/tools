package fr.lelouet.collectionholders.interfaces.numbers;

import javafx.beans.value.ObservableIntegerValue;

public interface ObsIntHolder extends ObsNumberHolder<Integer, ObsIntHolder> {

	@Override
	public ObservableIntegerValue asObservableNumber();

}
