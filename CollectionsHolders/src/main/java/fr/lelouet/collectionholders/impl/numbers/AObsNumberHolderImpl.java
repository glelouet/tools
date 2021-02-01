package fr.lelouet.collectionholders.impl.numbers;

import java.util.function.BiPredicate;

import fr.lelouet.collectionholders.impl.AObsObjHolder;
import fr.lelouet.collectionholders.impl.NotNullObsObjHolderImpl;
import fr.lelouet.collectionholders.interfaces.numbers.ObsBoolHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsNumberHolder;

public abstract class AObsNumberHolderImpl<Contained extends Number, Self extends ObsNumberHolder<Contained, Self>>
extends NotNullObsObjHolderImpl<Contained> implements ObsNumberHolder<Contained, Self> {

	public AObsNumberHolderImpl() {
	}

	public AObsNumberHolderImpl(Contained value) {
		super(value);
	}

	@Override
	public ObsBoolHolder test(BiPredicate<Contained, Contained> test, Self b) {
		return AObsObjHolder.reduce(this, b, ObsBoolHolderImpl::new, (u, v) -> test.test(u, v));
	}
}
