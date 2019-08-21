package fr.lelouet.collectionholders.interfaces.numbers;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

import fr.lelouet.collectionholders.impl.AObsObjHolder;
import fr.lelouet.collectionholders.interfaces.ObsObjHolder;
import javafx.beans.value.ObservableValue;

/**
 * common methods for double and int observable holders
 *
 * @param U
 *          the class of the number to hold
 * @param S
 *          self class
 *
 */
public interface ObsNumberHolder<U extends Number, S extends ObsNumberHolder<U, S>> extends ObsObjHolder<U> {

	/**
	 * internal function to create a copy (useful for binding modification, eg
	 * sum)
	 *
	 * @param var
	 * @return
	 */
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
		return AObsObjHolder.join(this, other, this::create, this::add);
	}

	public default S sub(S other) {
		return AObsObjHolder.join(this, other, this::create, this::sub);
	}

	public default S mult(S other) {
		return AObsObjHolder.join(this, other, this::create, this::mult);
	}

	public default S div(S other) {
		return AObsObjHolder.join(this, other, this::create, this::div);
	}

	public default S scale(S mult, S add) {
		return add(add).mult(mult);
	}

	public default S add(U b) {
		return AObsObjHolder.map(this, this::create, a -> add(a, b));
	}

	public default S sub(U b) {
		return AObsObjHolder.map(this, this::create, a -> sub(a, b));
	}

	public default S mult(U b) {
		return AObsObjHolder.map(this, this::create, a -> mult(a, b));
	}

	public default S div(U b) {
		return AObsObjHolder.map(this, this::create, a -> div(a, b));
	}

	public default S scale(U mult, U add) {
		return add(add).mult(mult);
	}

	/**
	 * create a variable containing a test over this variable value
	 *
	 * @param test
	 *          test over the value
	 * @return a new variable
	 */
	@Override
	public ObsBoolHolder test(Predicate<U> test);

	/**
	 * create a variable containing a test over this variable's, and another's,
	 * values
	 *
	 * @param test
	 *          test over the two values
	 * @param b
	 *          the other variable
	 * @return a new variable
	 */
	public ObsBoolHolder test(BiPredicate<U, U> test, S b);

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

	public ObservableValue<? extends Number> asObservableNumber();

}
