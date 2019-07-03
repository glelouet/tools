package fr.lelouet.collectionholders.impl.numbers;

import fr.lelouet.collectionholders.interfaces.numbers.ObsLongHolder;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.LongBinding;
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

	LongBinding observableNumber = null;

	@Override
	public LongBinding asObservableNumber() {
		if (observableNumber == null) {
			synchronized (this) {
				if (observableNumber == null) {
					observableNumber = Bindings.createLongBinding(() -> underlying.getValue(), underlying);
				}
			}
		}
		return observableNumber;
	}
}
