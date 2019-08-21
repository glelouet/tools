package fr.lelouet.collectionholders.impl;

import javafx.beans.value.ObservableValue;

/**
 * Object holders that throw an exception when set to null
 *
 * @param <U>
 */
public class NotNullObsObjHolderImpl<U> extends ObsObjHolderBack<U> {

	public NotNullObsObjHolderImpl(ObservableValue<U> underlying) {
		super(underlying);
		follow((observable, oldValue, newValue) -> {
			if (newValue == null) {
				throw new NullPointerException("in observable " + this + " replaced " + oldValue + " with " + newValue);
			}
		});
	}

}
