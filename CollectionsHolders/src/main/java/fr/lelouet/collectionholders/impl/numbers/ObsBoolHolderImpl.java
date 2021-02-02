package fr.lelouet.collectionholders.impl.numbers;

import fr.lelouet.collectionholders.impl.NotNullObsObjHolderImpl;
import fr.lelouet.collectionholders.interfaces.ObsObjHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsBoolHolder;

public class ObsBoolHolderImpl extends NotNullObsObjHolderImpl<Boolean> implements ObsBoolHolder {

	public static ObsBoolHolderImpl of(boolean value) {
		return new ObsBoolHolderImpl(value);
	}

	public ObsBoolHolderImpl() {
	}

	public ObsBoolHolderImpl(boolean value) {
		super(value);
	}

	private ObsBoolHolder not = null;

	@Override
	public ObsBoolHolder not() {
		if (not == null) {
			synchronized (this) {
				if (not == null) {
					ObsBoolHolderImpl other = ObsObjHolder.map(this, ObsBoolHolderImpl::new, (a) -> !a);
					other.not = this;
					not = other;
				}
			}
		}
		return not;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ObsBoolHolderImpl create() {
		return new ObsBoolHolderImpl();
	}

}
