package fr.lelouet.holders.impl.numbers;

import fr.lelouet.holders.interfaces.numbers.LongHolder;

public class LongHolderImpl extends ANumberHolderImpl<Long, LongHolder> implements LongHolder {

	public static LongHolderImpl of(Number value) {
		return new LongHolderImpl(value.longValue());
	}
	public LongHolderImpl() {
	}

	public LongHolderImpl(long value) {
		super(value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public LongHolderImpl create() {
		return new LongHolderImpl();
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
