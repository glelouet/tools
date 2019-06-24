package fr.lelouet.collectionholders.interfaces.numbers;

import java.util.function.Predicate;

import fr.lelouet.collectionholders.impl.ObsObjHolderImpl;
import fr.lelouet.collectionholders.impl.numbers.ObsBoolHolderImpl;
import fr.lelouet.collectionholders.interfaces.ObsObjHolder;
import javafx.beans.value.ObservableValue;

public interface ObsIntHolder extends ObsObjHolder<Integer> {

	public ObsIntHolder create(ObservableValue<Integer> var);

	public default ObsIntHolder add(ObsIntHolder other) {
		return ObsObjHolderImpl.join(this, other, this::create, (a, b) -> a + b);
	}

	public default ObsIntHolder sub(ObsIntHolder other) {
		return ObsObjHolderImpl.join(this, other, this::create, (a, b) -> a - b);
	}

	public default ObsIntHolder mult(ObsIntHolder other) {
		return ObsObjHolderImpl.join(this, other, this::create, (a, b) -> a * b);
	}

	public default ObsIntHolder div(ObsIntHolder other) {
		return ObsObjHolderImpl.join(this, other, this::create, (a, b) -> a / b);
	}

	public default ObsIntHolder add(int b) {
		return ObsObjHolderImpl.map(this, this::create, a -> a + b);
	}

	public default ObsIntHolder sub(int b) {
		return ObsObjHolderImpl.map(this, this::create, a -> a - b);
	}

	public default ObsIntHolder mult(int b) {
		return ObsObjHolderImpl.map(this, this::create, a -> a * b);
	}

	public default ObsIntHolder div(int b) {
		return ObsObjHolderImpl.map(this, this::create, a -> a / b);
	}

	public default ObsBoolHolder test(Predicate<Integer> test) {
		return ObsObjHolderImpl.map(this, ObsBoolHolderImpl::new, a -> test.test(a));
	}

}
