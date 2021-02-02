package fr.lelouet.collectionholders.impl.numbers;

import fr.lelouet.collectionholders.interfaces.numbers.ObsLongHolder;

public class ObsLongHolderImpl extends AObsNumberHolderImpl<Long, ObsLongHolder> implements ObsLongHolder {

	public static ObsLongHolderImpl of(long value) {
		return new ObsLongHolderImpl(value);
	}
	public ObsLongHolderImpl() {
	}

	public ObsLongHolderImpl(long value) {
		super(value);
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
}
