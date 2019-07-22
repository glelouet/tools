package fr.lelouet.collectionholders.impl.numbers;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

import fr.lelouet.collectionholders.impl.NotNullObsObjHolderImpl;
import fr.lelouet.collectionholders.impl.ObsObjHolderImpl;
import fr.lelouet.collectionholders.interfaces.numbers.ObsBoolHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsNumberHolder;
import javafx.beans.value.ObservableValue;

public abstract class AObsNumberHolderImpl<U extends Number, S extends ObsNumberHolder<U, S>>
extends NotNullObsObjHolderImpl<U> implements ObsNumberHolder<U, S> {

	public AObsNumberHolderImpl(ObservableValue<U> underlying) {
		super(underlying);
	}

	@Override
	public ObsBoolHolder test(Predicate<U> test) {
		return ObsObjHolderImpl.map(this, ObsBoolHolderImpl::new, a -> test.test(a));
	}

	@Override
	public ObsBoolHolder test(BiPredicate<U, U> test, S b) {
		return ObsObjHolderImpl.join(this, b, ObsBoolHolderImpl::new, (u, v) -> test.test(u, v));
	}

	@Override
	public ObservableValue<? extends Number> asObservableNumber() {
		waitData();
		return underlying;
	}

}
