package fr.lelouet.collectionholders.impl.numbers;

import fr.lelouet.collectionholders.impl.NotNullObsObjHolderImpl;
import fr.lelouet.collectionholders.impl.ObsObjHolderImpl;
import fr.lelouet.collectionholders.interfaces.numbers.ObsDoubleHolder;
import javafx.beans.value.ObservableValue;

public class ObsDoubleHolderImpl extends NotNullObsObjHolderImpl<Double> implements ObsDoubleHolder {

	public ObsDoubleHolderImpl(ObservableValue<Double> underlying) {
		super(underlying);
	}

	@Override
	public ObsDoubleHolderImpl create(ObservableValue<Double> var) {
		return new ObsDoubleHolderImpl(var);
	}

	private ObsIntHolderImpl ceil = null;

	@Override
	public ObsIntHolderImpl ceil() {
		if (ceil == null) {
			synchronized (this) {
				if (ceil == null) {
					ceil = ObsObjHolderImpl.map(this, ObsIntHolderImpl::new, a -> (int) Math.ceil(a));
				}
			}
		}
		return ceil;
	}

	private ObsIntHolderImpl floor = null;

	@Override
	public ObsIntHolderImpl floor() {
		if (floor == null) {
			synchronized (this) {
				if (floor == null) {
					floor = ObsObjHolderImpl.map(this, ObsIntHolderImpl::new, a -> (int) Math.floor(a));
				}
			}
		}
		return floor;
	}
}
