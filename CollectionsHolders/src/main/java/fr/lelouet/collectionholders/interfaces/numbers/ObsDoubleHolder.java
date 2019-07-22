package fr.lelouet.collectionholders.interfaces.numbers;

import javafx.beans.value.ObservableDoubleValue;

public interface ObsDoubleHolder extends ObsNumberHolder<Double, ObsDoubleHolder> {

	public ObsIntHolder ceil();

	public ObsIntHolder floor();

	@Override
	public ObservableDoubleValue asObservableNumber();
}
