package fr.lelouet.collectionholders.impl.numbers;

import fr.lelouet.collectionholders.impl.NotNullObsObjHolderImpl;
import fr.lelouet.collectionholders.interfaces.numbers.ObsIntHolder;
import javafx.beans.value.ObservableValue;

public class ObsIntHolderImpl extends NotNullObsObjHolderImpl<Integer> implements ObsIntHolder {

	public ObsIntHolderImpl(ObservableValue<Integer> underlying) {
		super(underlying);
	}

	@Override
	public ObsIntHolder create(ObservableValue<Integer> var) {
		return new ObsIntHolderImpl(var);
	}

}
