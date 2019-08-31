package fr.lelouet.collectionholders.impl.numbers;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

import fr.lelouet.collectionholders.impl.AObsObjHolder;
import fr.lelouet.collectionholders.impl.NotNullObsObjHolderImpl;
import fr.lelouet.collectionholders.interfaces.numbers.ObsBoolHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsNumberHolder;

public abstract class AObsNumberHolderImpl<Contained extends Number, Self extends ObsNumberHolder<Contained, Self>>
extends NotNullObsObjHolderImpl<Contained> implements ObsNumberHolder<Contained, Self> {


	@Override
	public ObsBoolHolder test(Predicate<Contained> test) {
		return AObsObjHolder.map(this, ObsBoolHolderImpl::new, a -> test.test(a));
	}

	@Override
	public ObsBoolHolder test(BiPredicate<Contained, Contained> test, Self b) {
		return AObsObjHolder.join(this, b, ObsBoolHolderImpl::new, (u, v) -> test.test(u, v));
	}
}
