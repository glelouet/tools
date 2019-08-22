package fr.lelouet.collectionholders.impl.numbers;

import fr.lelouet.collectionholders.interfaces.numbers.ObsLongHolder;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.value.ObservableLongValue;

public class ObsLongHolderImpl extends AObsNumberHolderImpl<Long, ObsLongHolder> implements ObsLongHolder {

	public ObsLongHolderImpl() {
	}

	public ObsLongHolderImpl(long value) {
		this();
		set(value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ObsLongHolderImpl create() {
		return new ObsLongHolderImpl();
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
					follow(obs::set);
				}
			}
		}
		return obs;
	}
}
