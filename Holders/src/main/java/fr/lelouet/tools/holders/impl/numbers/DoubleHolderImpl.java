package fr.lelouet.tools.holders.impl.numbers;

import fr.lelouet.tools.holders.interfaces.numbers.DoubleHolder;
import fr.lelouet.tools.holders.interfaces.numbers.IntHolder;
import lombok.Getter;
import lombok.experimental.Accessors;

public class DoubleHolderImpl extends ANumberHolderImpl<Double, DoubleHolder> implements DoubleHolder {

	public static DoubleHolderImpl of(Number value) {
		return new DoubleHolderImpl(value.doubleValue());
	}

	public DoubleHolderImpl() {
	}

	public DoubleHolderImpl(double value) {
		super(value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public DoubleHolderImpl create() {
		return new DoubleHolderImpl();
	}

	@Getter(lazy = true)
	@Accessors(fluent = true)
	private final IntHolder ceil = mapInt(d -> (int) Math.ceil(d));

	@Getter(lazy = true)
	@Accessors(fluent = true)
	private final IntHolder floor = mapInt(d -> (int) Math.floor(d));


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
