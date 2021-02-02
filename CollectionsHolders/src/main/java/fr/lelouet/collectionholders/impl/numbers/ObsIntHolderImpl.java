package fr.lelouet.collectionholders.impl.numbers;

import fr.lelouet.collectionholders.interfaces.numbers.ObsIntHolder;

public class ObsIntHolderImpl extends AObsNumberHolderImpl<Integer, ObsIntHolder> implements ObsIntHolder {

	public static ObsIntHolderImpl of(int value) {
		return new ObsIntHolderImpl(value);
	}

	public ObsIntHolderImpl() {
	}

	public ObsIntHolderImpl(int value) {
		super(value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ObsIntHolderImpl create() {
		return new ObsIntHolderImpl();
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
