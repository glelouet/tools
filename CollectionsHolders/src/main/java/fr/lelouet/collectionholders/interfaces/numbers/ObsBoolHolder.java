package fr.lelouet.collectionholders.interfaces.numbers;

import fr.lelouet.collectionholders.impl.ObsObjHolderImpl;
import fr.lelouet.collectionholders.interfaces.ObsObjHolder;
import javafx.beans.value.ObservableValue;

public interface ObsBoolHolder extends ObsObjHolder<Boolean> {

	public ObsBoolHolder create(ObservableValue<Boolean> var);

	public default ObsBoolHolder or(ObsBoolHolder other) {
		return ObsObjHolderImpl.join(this, other, this::create, (a, b) -> a || b);
	}

	public default ObsBoolHolder and(ObsBoolHolder other) {
		return ObsObjHolderImpl.join(this, other, this::create, (a, b) -> a && b);
	}

	public default ObsBoolHolder xor(ObsBoolHolder other) {
		return ObsObjHolderImpl.join(this, other, this::create, (a, b) -> a != b);
	}

	public default ObsBoolHolder or(boolean b) {
		return ObsObjHolderImpl.map(this, this::create, (a) -> a || b);
	}

	public default ObsBoolHolder and(boolean b) {
		return ObsObjHolderImpl.map(this, this::create, (a) -> a && b);
	}

	public default ObsBoolHolder xor(boolean b) {
		return ObsObjHolderImpl.map(this, this::create, (a) -> a != b);
	}

	public ObsBoolHolder not();

}
