package fr.lelouet.collectionholders.impl.numbers;

import fr.lelouet.collectionholders.impl.NotNullObsObjHolderImpl;
import fr.lelouet.collectionholders.impl.ObsObjHolderImpl;
import fr.lelouet.collectionholders.interfaces.numbers.ObsBoolHolder;
import javafx.beans.value.ObservableValue;

public class ObsBoolHolderImpl extends NotNullObsObjHolderImpl<Boolean> implements ObsBoolHolder {

	public ObsBoolHolderImpl(ObservableValue<Boolean> underlying) {
		super(underlying);
	}

	private ObsBoolHolderImpl(ObservableValue<Boolean> underlying, ObsBoolHolder not) {
		super(underlying);
		this.not = not;
	}

	@Override
	public ObsBoolHolder create(ObservableValue<Boolean> var) {
		return new ObsBoolHolderImpl(var);
	}

	ObsBoolHolder not = null;

	@Override
	public ObsBoolHolder not() {
		if (not == null) {
			synchronized (this) {
				if (not == null) {
					not = ObsObjHolderImpl.map(this, var -> new ObsBoolHolderImpl(var, this), (a) -> !a);
				}
			}
		}
		return not;
	}

}
