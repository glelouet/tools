package fr.lelouet.holders.interfaces.numbers;

import java.util.function.BiPredicate;
import java.util.function.Consumer;

import fr.lelouet.holders.interfaces.ObjHolder;
import fr.lelouet.holders.interfaces.RWObjHolder;

/**
 * common methods for double, long and int observable holders. <br />
 * This class is parameterized by the SelfClass that is returned when using
 * combination methods (add, sub, etc.). This allows to have IntHolder when
 * adding two IntHolders.
 *
 * @param Contained
 *          the class of the boxified number to hold
 * @param SelfClass
 *          self class. It is used for the combination methods, eg a double
 *          holder would return a double holder, not a obsNumberHolder, for the
 *          add etc. method.
 *
 */
public interface NumberHolder<Contained extends Number, SelfClass extends NumberHolder<Contained, SelfClass>>
extends ObjHolder<Contained> {

	@Override
	default SelfClass follow(Consumer<Contained> listener) {
		ObjHolder.super.follow(listener);
		return self();
	}

	@SuppressWarnings("unchecked")
	default SelfClass self() {
		return (SelfClass) this;
	}

	/**
	 * internal function to create a copy (useful for binding modification, eg
	 * sum)
	 *
	 * @param var
	 * @return
	 */
	public <RWClass extends NumberHolder<Contained, SelfClass> & RWObjHolder<Contained>> RWClass create();

	//
	// combination methods over numbers
	//

	public Contained add(Contained a, Contained b);

	public Contained sub(Contained a, Contained b);

	public Contained div(Contained a, Contained b);

	public Contained mult(Contained a, Contained b);

	public boolean gt(Contained a, Contained b);

	public boolean ge(Contained a, Contained b);

	public boolean lt(Contained a, Contained b);

	public boolean le(Contained a, Contained b);

	public boolean eq(Contained a, Contained b);

	//
	// combination methods over SelfClass
	//

	public default SelfClass add(SelfClass other) {
		return ObjHolder.combine(this, other, this::create, this::add);
	}

	public default SelfClass sub(SelfClass other) {
		return ObjHolder.combine(this, other, this::create, this::sub);
	}

	public default SelfClass mult(SelfClass other) {
		return ObjHolder.combine(this, other, this::create, this::mult);
	}

	public default SelfClass div(SelfClass other) {
		return ObjHolder.combine(this, other, this::create, this::div);
	}

	public default SelfClass scale(SelfClass mult, SelfClass add) {
		return add(add).mult(mult);
	}

	public default SelfClass add(Contained b) {
		return ObjHolder.map(this, this::create, a -> add(a, b));
	}

	public default SelfClass sub(Contained b) {
		return ObjHolder.map(this, this::create, a -> sub(a, b));
	}

	public default SelfClass mult(Contained b) {
		return ObjHolder.map(this, this::create, a -> mult(a, b));
	}

	public default SelfClass div(Contained b) {
		return ObjHolder.map(this, this::create, a -> div(a, b));
	}

	public default SelfClass scale(Contained mult, Contained add) {
		return add(add).mult(mult);
	}

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
	public BoolHolder test(BiPredicate<Contained, Contained> test, SelfClass b);

	/**
	 * create a new variable containing a test over this variable's value and
	 * another fixed value
	 *
	 * @param test
	 *          test over the two value
	 * @param b
	 *          the other value
	 * @return a new variable
	 */
	public default BoolHolder test(BiPredicate<Contained, Contained> test, Contained b) {
		return test(value -> test.test(value, b));
	}

	public default BoolHolder gt(SelfClass other) {
		return test(this::gt, other);
	}

	public default BoolHolder gt(Contained other) {
		return test(this::gt, other);
	}

	public default BoolHolder ge(SelfClass other) {
		return test(this::ge, other);
	}

	public default BoolHolder ge(Contained other) {
		return test(this::ge, other);
	}

	public default BoolHolder lt(SelfClass other) {
		return test(this::lt, other);
	}

	public default BoolHolder lt(Contained other) {
		return test(this::lt, other);
	}

	public default BoolHolder le(SelfClass other) {
		return test(this::le, other);
	}

	public default BoolHolder le(Contained other) {
		return test(this::le, other);
	}

	public default BoolHolder eq(SelfClass other) {
		return test(this::eq, other);
	}

	public default BoolHolder eq(Contained other) {
		return test(this::eq, other);
	}

}
