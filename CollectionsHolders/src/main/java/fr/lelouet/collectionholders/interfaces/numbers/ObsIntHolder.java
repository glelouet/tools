package fr.lelouet.collectionholders.interfaces.numbers;

import java.util.function.BiPredicate;
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

	public default ObsIntHolder scale(ObsIntHolder mult, ObsIntHolder add) {
		return add(add).mult(mult);
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

	public default ObsIntHolder scale(int mult, int add) {
		return add(add).mult(mult);
	}

	public default ObsBoolHolder test(Predicate<Integer> test) {
		return ObsObjHolderImpl.map(this, ObsBoolHolderImpl::new, a -> test.test(a));
	}

	public default ObsBoolHolder test(BiPredicate<Integer, Integer> test, ObsIntHolder b) {
		return ObsObjHolderImpl.join(this, b, ObsBoolHolderImpl::new, (u, v) -> test.test(u, v));
	}

	public default ObsBoolHolder gt(ObsIntHolder other) {
		return test((a, b)->a>b,other);
	}

	public default ObsBoolHolder gte(ObsIntHolder other) {
		return test((a, b) -> a >= b, other);
	}

	public default ObsBoolHolder lt(ObsIntHolder other) {
		return test((a, b) -> a < b, other);
	}

	public default ObsBoolHolder lte(ObsIntHolder other) {
		return test((a, b) -> a <= b, other);
	}

	public default ObsBoolHolder eq(ObsIntHolder other) {
		return test((a, b) -> a == b, other);
	}

}
