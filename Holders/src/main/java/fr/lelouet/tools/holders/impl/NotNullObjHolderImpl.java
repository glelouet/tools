package fr.lelouet.tools.holders.impl;

/**
 * Object holders that throw an exception when set to null. Used for numbers.
 *
 * @param <U>
 */
public class NotNullObjHolderImpl<U> extends ObjHolderSimple<U> {

	public NotNullObjHolderImpl() {
	}

	public NotNullObjHolderImpl(U u) {
		super(u);
	}

	@Override
	public synchronized void set(U newitem) {
		if(newitem==null) {
			throw new UnsupportedOperationException("null item forbidden");
		}
		super.set(newitem);
	}

}
