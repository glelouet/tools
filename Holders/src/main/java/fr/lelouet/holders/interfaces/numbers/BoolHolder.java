package fr.lelouet.holders.interfaces.numbers;

import fr.lelouet.holders.interfaces.ObjHolder;
import fr.lelouet.holders.interfaces.RWObjHolder;

/**
 * holder over a boolean. Contains logical values, like and or not xor.
 */
public interface BoolHolder extends ObjHolder<Boolean> {

	public <RWClass extends BoolHolder & RWObjHolder<Boolean>> RWClass create();

	public default BoolHolder or(BoolHolder other) {
		return ObjHolder.combine(this, other, this::create, (a, b) -> a || b);
	}

	public default BoolHolder and(BoolHolder other) {
		return ObjHolder.combine(this, other, this::create, (a, b) -> a && b);
	}

	public default BoolHolder xor(BoolHolder other) {
		return ObjHolder.combine(this, other, this::create, (a, b) -> a != b);
	}

	public default BoolHolder or(boolean b) {
		return ObjHolder.map(this, this::create, (a) -> a || b);
	}

	public default BoolHolder and(boolean b) {
		return ObjHolder.map(this, this::create, (a) -> a && b);
	}

	public default BoolHolder xor(boolean b) {
		return ObjHolder.map(this, this::create, (a) -> a != b);
	}

	/**
	 * create and cache a boolean
	 *
	 * @return
	 */
	public BoolHolder not();


}
