package fr.lelouet.collectionholders.impl.numbers;

import fr.lelouet.collectionholders.interfaces.numbers.ObsLongHolder;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.value.ObservableLongValue;
import javafx.beans.value.ObservableValue;

public class ObsLongHolderImpl extends AObsNumberHolderImpl<Long, ObsLongHolder> implements ObsLongHolder {

	public ObsLongHolderImpl(ObservableValue<Long> underlying) {
		super(underlying);
	}

	@Override
	public ObsLongHolder create(ObservableValue<Long> var) {
		return new ObsLongHolderImpl(var);
	}

	@Override
	public Long add(Long a, Long b) {
		return a + b;
	}

	@Override
	public Long sub(Long a, Long b) {
		return a - b;
	}

	@Override
	public Long div(Long a, Long b) {
		return a / b;
	}

	@Override
	public Long mult(Long a, Long b) {
		return a * b;
	}

	@Override
	public boolean gt(Long a, Long b) {
		return a > b;
	}

	@Override
	public boolean ge(Long a, Long b) {
		return a >= b;
	}

	@Override
	public boolean lt(Long a, Long b) {
		return a < b;
	}

	@Override
	public boolean le(Long a, Long b) {
		return a <= b;
	}

	@Override
	public boolean eq(Long a, Long b) {
		return a == b;
	}

	private SimpleLongProperty obs = null;

	@Override
	public ObservableLongValue asObservableNumber() {
		if (obs == null) {
			waitData();
			synchronized (this) {
				if (obs == null) {
					obs = new SimpleLongProperty();
					obs.bind(underlying);
				}
			}
		}
		return obs;
	}
}
