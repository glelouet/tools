package fr.lelouet.collectionholders.impl.numbers;

import fr.lelouet.collectionholders.interfaces.numbers.ObsDoubleHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsIntHolder;
import lombok.Getter;
import lombok.experimental.Accessors;

public class ObsDoubleHolderImpl extends AObsNumberHolderImpl<Double, ObsDoubleHolder> implements ObsDoubleHolder {

	public static ObsDoubleHolderImpl of(double value) {
		return new ObsDoubleHolderImpl(value);
	}

	public ObsDoubleHolderImpl() {
	}

	public ObsDoubleHolderImpl(double value) {
		super(value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ObsDoubleHolderImpl create() {
		return new ObsDoubleHolderImpl();
	}

	@Getter(lazy = true)
	@Accessors(fluent = true)
	private final ObsIntHolder ceil = mapInt(d -> (int) Math.ceil(d));

	@Getter(lazy = true)
	@Accessors(fluent = true)
	private final ObsIntHolder floor = mapInt(d -> (int) Math.floor(d));


	@Override
	public Double add(Double a, Double b) {
		return a + b;
	}

	@Override
	public Double sub(Double a, Double b) {
		return a - b;
	}

	@Override
	public Double div(Double a, Double b) {
		return a / b;
	}

	@Override
	public Double mult(Double a, Double b) {
		return a * b;
	}

	@Override
	public boolean gt(Double a, Double b) {
		return a > b;
	}

	@Override
	public boolean ge(Double a, Double b) {
		return a >= b;
	}

	@Override
	public boolean lt(Double a, Double b) {
		return a < b;
	}

	@Override
	public boolean le(Double a, Double b) {
		return a <= b;
	}

	@Override
	public boolean eq(Double a, Double b) {
		return a == b;
	}
}
