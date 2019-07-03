package fr.lelouet.collectionholders.interfaces.numbers;

import javafx.beans.binding.DoubleBinding;

public interface ObsDoubleHolder extends ObsNumberHolder<Double, ObsDoubleHolder> {

	public ObsIntHolder ceil();

	public ObsIntHolder floor();

	@Override
	DoubleBinding asObservableNumber();
}
