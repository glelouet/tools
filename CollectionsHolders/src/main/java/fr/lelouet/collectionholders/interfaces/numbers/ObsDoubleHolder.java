package fr.lelouet.collectionholders.interfaces.numbers;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

import fr.lelouet.collectionholders.impl.ObsObjHolderImpl;
import fr.lelouet.collectionholders.impl.numbers.ObsBoolHolderImpl;
import fr.lelouet.collectionholders.interfaces.ObsObjHolder;
import javafx.beans.value.ObservableValue;

public interface ObsDoubleHolder extends ObsObjHolder<Double> {

	public ObsDoubleHolder create(ObservableValue<Double> var);

	public default ObsDoubleHolder add(ObsDoubleHolder other) {
		return ObsObjHolderImpl.join(this, other, this::create, (a, b) -> a + b);
	}

	public default ObsDoubleHolder sub(ObsDoubleHolder other) {
		return ObsObjHolderImpl.join(this, other, this::create, (a, b) -> a - b);
	}

	public default ObsDoubleHolder mult(ObsDoubleHolder other) {
		return ObsObjHolderImpl.join(this, other, this::create, (a, b) -> a * b);
	}

	public default ObsDoubleHolder div(ObsDoubleHolder other) {
		return ObsObjHolderImpl.join(this, other, this::create, (a, b) -> a / b);
	}

	public default ObsDoubleHolder scale(ObsDoubleHolder mult, ObsDoubleHolder add) {
		return add(add).mult(mult);
	}

	public default ObsDoubleHolder add(double b) {
		return ObsObjHolderImpl.map(this, this::create, a -> a + b);
	}

	public default ObsDoubleHolder sub(double b) {
		return ObsObjHolderImpl.map(this, this::create, a -> a - b);
	}

	public default ObsDoubleHolder mult(double b) {
		return ObsObjHolderImpl.map(this, this::create, a -> a * b);
	}

	public default ObsDoubleHolder div(double b) {
		return ObsObjHolderImpl.map(this, this::create, a -> a / b);
	}

	public default ObsDoubleHolder scale(double mult, double add) {
		return add(add).mult(mult);
	}

	public default ObsBoolHolder test(Predicate<Double> test) {
		return ObsObjHolderImpl.map(this, ObsBoolHolderImpl::new, a -> test.test(a));
	}

	public default ObsBoolHolder test(BiPredicate<Double, Double> test, ObsDoubleHolder b) {
		return ObsObjHolderImpl.join(this, b, ObsBoolHolderImpl::new, (u, v) -> test.test(u, v));
	}

	public default ObsBoolHolder gt(ObsDoubleHolder other) {
		return test((a, b) -> a > b, other);
	}

	public default ObsBoolHolder gte(ObsDoubleHolder other) {
		return test((a, b) -> a >= b, other);
	}

	public default ObsBoolHolder lt(ObsDoubleHolder other) {
		return test((a, b) -> a < b, other);
	}

	public default ObsBoolHolder lte(ObsDoubleHolder other) {
		return test((a, b) -> a <= b, other);
	}

	public default ObsBoolHolder eq(ObsDoubleHolder other) {
		return test((a, b) -> a == b, other);
	}

	public ObsIntHolder ceil();

	public ObsIntHolder floor();
}
