package fr.lelouet.collectionholders.interfaces.numbers;

import java.util.function.Consumer;

import fr.lelouet.collectionholders.interfaces.ObsObjHolder;
import fr.lelouet.collectionholders.interfaces.RWObsObjHolder;

/**
 * holder over a boolean. Contains logical values, like and or not xor.
 */
public interface ObsBoolHolder extends ObsObjHolder<Boolean> {

	public <RWClass extends ObsBoolHolder & RWObsObjHolder<Boolean>> RWClass create();

	public default ObsBoolHolder or(ObsBoolHolder other) {
		return ObsObjHolder.combine(this, other, this::create, (a, b) -> a || b);
	}

	public default ObsBoolHolder and(ObsBoolHolder other) {
		return ObsObjHolder.combine(this, other, this::create, (a, b) -> a && b);
	}

	public default ObsBoolHolder xor(ObsBoolHolder other) {
		return ObsObjHolder.combine(this, other, this::create, (a, b) -> a != b);
	}

	public default ObsBoolHolder or(boolean b) {
		return ObsObjHolder.map(this, this::create, (a) -> a || b);
	}

	public default ObsBoolHolder and(boolean b) {
		return ObsObjHolder.map(this, this::create, (a) -> a && b);
	}

	public default ObsBoolHolder xor(boolean b) {
		return ObsObjHolder.map(this, this::create, (a) -> a != b);
	}

	/**
	 * create and cache a boolean
	 *
	 * @return
	 */
	public ObsBoolHolder not();

	@Override
	ObsBoolHolder peek(Consumer<Boolean> observer);

}
