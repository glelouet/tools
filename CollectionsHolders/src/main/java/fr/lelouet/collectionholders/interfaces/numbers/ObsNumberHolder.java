package fr.lelouet.collectionholders.interfaces.numbers;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

import fr.lelouet.collectionholders.impl.AObsObjHolder;
import fr.lelouet.collectionholders.interfaces.ObsObjHolder;
import fr.lelouet.collectionholders.interfaces.RWObsObjHolder;
import javafx.beans.value.ObservableValue;

/**
 * common methods for double and int observable holders
 *
 * @param Contained
 *          the class of the number to hold
 * @param SelfClass
 *          self class
 *
 */
public interface ObsNumberHolder<Contained extends Number, SelfClass extends ObsNumberHolder<Contained, SelfClass>>
extends ObsObjHolder<Contained> {

	/**
	 * internal function to create a copy (useful for binding modification, eg
	 * sum)
	 *
	 * @param var
	 * @return
	 */
	public <RWClass extends ObsNumberHolder<Contained, SelfClass> & RWObsObjHolder<Contained>> RWClass create();

	public Contained add(Contained a, Contained b);

	public Contained sub(Contained a, Contained b);

	public Contained div(Contained a, Contained b);

	public Contained mult(Contained a, Contained b);

	public boolean gt(Contained a, Contained b);

	public boolean ge(Contained a, Contained b);

	public boolean lt(Contained a, Contained b);

	public boolean le(Contained a, Contained b);

	public boolean eq(Contained a, Contained b);

	public default SelfClass add(SelfClass other) {
		return AObsObjHolder.join(this, other, this::create, this::add);
	}

	public default SelfClass sub(SelfClass other) {
		return AObsObjHolder.join(this, other, this::create, this::sub);
	}

	public default SelfClass mult(SelfClass other) {
		return AObsObjHolder.join(this, other, this::create, this::mult);
	}

	public default SelfClass div(SelfClass other) {
		return AObsObjHolder.join(this, other, this::create, this::div);
	}

	public default SelfClass scale(SelfClass mult, SelfClass add) {
		return add(add).mult(mult);
	}

	public default SelfClass add(Contained b) {
		return AObsObjHolder.map(this, this::create, a -> add(a, b));
	}

	public default SelfClass sub(Contained b) {
		return AObsObjHolder.map(this, this::create, a -> sub(a, b));
	}

	public default SelfClass mult(Contained b) {
		return AObsObjHolder.map(this, this::create, a -> mult(a, b));
	}

	public default SelfClass div(Contained b) {
		return AObsObjHolder.map(this, this::create, a -> div(a, b));
	}

	public default SelfClass scale(Contained mult, Contained add) {
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
	public ObsBoolHolder test(Predicate<Contained> test);

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
	public ObsBoolHolder test(BiPredicate<Contained, Contained> test, SelfClass b);

	public default ObsBoolHolder gt(SelfClass other) {
		return test(this::gt, other);
	}

	public default ObsBoolHolder ge(SelfClass other) {
		return test(this::ge, other);
	}

	public default ObsBoolHolder lt(SelfClass other) {
		return test(this::lt, other);
	}

	public default ObsBoolHolder lte(SelfClass other) {
		return test(this::le, other);
	}

	public default ObsBoolHolder eq(SelfClass other) {
		return test(this::eq, other);
	}

	public ObservableValue<? extends Number> asObservableNumber();

}
