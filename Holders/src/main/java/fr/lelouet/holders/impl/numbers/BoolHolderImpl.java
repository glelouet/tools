package fr.lelouet.holders.impl.numbers;

import fr.lelouet.holders.impl.NotNullObjHolderImpl;
import fr.lelouet.holders.interfaces.ObjHolder;
import fr.lelouet.holders.interfaces.numbers.BoolHolder;

public class BoolHolderImpl extends NotNullObjHolderImpl<Boolean> implements BoolHolder {

	public static BoolHolderImpl of(boolean value) {
		return new BoolHolderImpl(value);
	}

	public BoolHolderImpl() {
	}

	public BoolHolderImpl(boolean value) {
		super(value);
	}

	private BoolHolder not = null;

	@Override
	public BoolHolder not() {
		if (not == null) {
			synchronized (this) {
				if (not == null) {
					BoolHolderImpl other = ObjHolder.map(this, BoolHolderImpl::new, (a) -> !a);
					other.not = this;
					not = other;
				}
			}
		}
		return not;
	}

	@SuppressWarnings("unchecked")
	@Override
	public BoolHolderImpl create() {
		return new BoolHolderImpl();
	}

}
