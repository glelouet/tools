package fr.lelouet.collectionholders.interfaces.numbers;

import javafx.beans.binding.LongBinding;

public interface ObsLongHolder extends ObsNumberHolder<Long, ObsLongHolder> {

	@Override
	LongBinding asObservableNumber();

}
