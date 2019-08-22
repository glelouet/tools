package fr.lelouet.collectionholders.impl.numbers;

import fr.lelouet.collectionholders.impl.AObsObjHolder;
import fr.lelouet.collectionholders.impl.NotNullObsObjHolderImpl;
import fr.lelouet.collectionholders.interfaces.numbers.ObsBoolHolder;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;

public class ObsBoolHolderImpl extends NotNullObsObjHolderImpl<Boolean> implements ObsBoolHolder {

	public ObsBoolHolderImpl() {
	}

	public ObsBoolHolderImpl(boolean value) {
		this();
		set(value);
	}

	ObsBoolHolder not = null;

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

	private SimpleBooleanProperty obs = null;

	@Override
	public ObservableBooleanValue asObservableBool() {
		if (obs == null) {
			waitData();
			synchronized (this) {
				if (obs == null) {
					obs = new SimpleBooleanProperty();
					follow(obs::set);
				}
			}
		}
		return obs;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ObsBoolHolderImpl create() {
		return new ObsBoolHolderImpl();
	}

}
