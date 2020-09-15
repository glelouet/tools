package fr.lelouet.collectionholders.impl;

/**
 * Object holders that throw an exception when set to null. Used for numbers.
 *
 * @param <U>
 */
public class NotNullObsObjHolderImpl<U> extends ObsObjHolderSimple<U> {

	public NotNullObsObjHolderImpl() {
		peek(newValue -> {
			if (newValue == null) {
				throw new NullPointerException("in observable " + this + " set value to " + newValue);
			}
		});
	}

}
