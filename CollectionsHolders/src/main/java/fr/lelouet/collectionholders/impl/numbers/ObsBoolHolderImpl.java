package fr.lelouet.collectionholders.impl.numbers;

import java.util.function.Consumer;

import fr.lelouet.collectionholders.impl.AObsObjHolder;
import fr.lelouet.collectionholders.impl.NotNullObsObjHolderImpl;
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
					ObsBoolHolderImpl other = AObsObjHolder.map(this, ObsBoolHolderImpl::new, (a) -> !a);
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

	@Override
	public ObsBoolHolderImpl peek(Consumer<Boolean> observer) {
		follow(observer);
		return this;
	}

}
