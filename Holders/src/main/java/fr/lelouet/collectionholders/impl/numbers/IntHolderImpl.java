package fr.lelouet.collectionholders.impl.numbers;

import fr.lelouet.collectionholders.interfaces.numbers.IntHolder;

public class IntHolderImpl extends ANumberHolderImpl<Integer, IntHolder> implements IntHolder {

	public static IntHolderImpl of(Number value) {
		return new IntHolderImpl(value.intValue());
	}

	public IntHolderImpl() {
	}

	public IntHolderImpl(int value) {
		super(value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public IntHolderImpl create() {
		return new IntHolderImpl();
	}

	@Override
	public Integer add(Integer a, Integer b) {
		return a + b;
	}

	@Override
	public Integer sub(Integer a, Integer b) {
		return a - b;
	}

	@Override
	public Integer div(Integer a, Integer b) {
		return a / b;
	}

	@Override
	public Integer mult(Integer a, Integer b) {
		return a * b;
	}

	@Override
	public boolean gt(Integer a, Integer b) {
		return a > b;
	}

	@Override
	public boolean ge(Integer a, Integer b) {
		return a >= b;
	}

	@Override
	public boolean lt(Integer a, Integer b) {
		return a < b;
	}

	@Override
	public boolean le(Integer a, Integer b) {
		return a <= b;
	}

	@Override
	public boolean eq(Integer a, Integer b) {
		return a == b;
	}

}
