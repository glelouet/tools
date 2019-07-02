package fr.lelouet.collectionholders.interfaces.numbers;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

import fr.lelouet.collectionholders.impl.ObsObjHolderImpl;
import fr.lelouet.collectionholders.impl.numbers.ObsBoolHolderImpl;
import fr.lelouet.collectionholders.interfaces.ObsObjHolder;
import javafx.beans.value.ObservableValue;

public interface ObsNumberHolder<U extends Number, S extends ObsNumberHolder<U, S>> extends ObsObjHolder<U> {

	public S create(ObservableValue<U> var);

	public U add(U a, U b);

	public U sub(U a, U b);

	public U div(U a, U b);

	public U mult(U a, U b);

	public boolean gt(U a, U b);

	public boolean ge(U a, U b);

	public boolean lt(U a, U b);

	public boolean le(U a, U b);

	public boolean eq(U a, U b);

	public default S add(S other) {
		return ObsObjHolderImpl.join(this, other, this::create, this::add);
	}

	public default S sub(S other) {
		return ObsObjHolderImpl.join(this, other, this::create, this::sub);
	}

	public default S mult(S other) {
		return ObsObjHolderImpl.join(this, other, this::create, this::mult);
	}

	public default S div(S other) {
		return ObsObjHolderImpl.join(this, other, this::create, this::div);
	}

	public default S scale(S mult, S add) {
		return add(add).mult(mult);
	}

	public default S add(U b) {
		return ObsObjHolderImpl.map(this, this::create, a -> add(a, b));
	}

	public default S sub(U b) {
		return ObsObjHolderImpl.map(this, this::create, a -> sub(a, b));
	}

	public default S mult(U b) {
		return ObsObjHolderImpl.map(this, this::create, a -> mult(a, b));
	}

	public default S div(U b) {
		return ObsObjHolderImpl.map(this, this::create, a -> div(a, b));
	}

	public default S scale(U mult, U add) {
		return add(add).mult(mult);
	}

	public default ObsBoolHolder test(Predicate<U> test) {
		return ObsObjHolderImpl.map(this, ObsBoolHolderImpl::new, a -> test.test(a));
	}

	public default ObsBoolHolder test(BiPredicate<U, U> test, S b) {
		return ObsObjHolderImpl.join(this, b, ObsBoolHolderImpl::new, (u, v) -> test.test(u, v));
	}

	public default ObsBoolHolder gt(S other) {
		return test(this::gt, other);
	}

	public default ObsBoolHolder ge(S other) {
		return test(this::ge, other);
	}

	public default ObsBoolHolder lt(S other) {
		return test(this::lt, other);
	}

	public default ObsBoolHolder lte(S other) {
		return test(this::le, other);
	}

	public default ObsBoolHolder eq(S other) {
		return test(this::eq, other);
	}

}
