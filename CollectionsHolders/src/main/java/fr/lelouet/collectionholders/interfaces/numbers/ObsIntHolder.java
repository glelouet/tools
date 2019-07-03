package fr.lelouet.collectionholders.interfaces.numbers;

import javafx.beans.binding.IntegerBinding;

public interface ObsIntHolder extends ObsNumberHolder<Integer, ObsIntHolder> {

	@Override
	IntegerBinding asObservableNumber();

}
