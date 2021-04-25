package fr.lelouet.holders.impl.numbers;

import fr.lelouet.holders.interfaces.numbers.FloatHolder;
import fr.lelouet.holders.interfaces.numbers.IntHolder;
import lombok.Getter;
import lombok.experimental.Accessors;

public class FloatHolderImpl extends ANumberHolderImpl<Float, FloatHolder> implements FloatHolder {

	public static FloatHolderImpl of(Number value) {
		return new FloatHolderImpl(value.floatValue());
	}

	public FloatHolderImpl() {
	}

	public FloatHolderImpl(float value) {
		super(value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public FloatHolderImpl create() {
		return new FloatHolderImpl();
	}

	@Override
	public Float add(Float a, Float b) {
		return a + b;
	}

	@Override
	public Float sub(Float a, Float b) {
		return a - b;
	}

	@Override
	public Float div(Float a, Float b) {
		return a / b;
	}

	@Override
	public Float mult(Float a, Float b) {
		return a * b;
	}

	@Override
	public boolean gt(Float a, Float b) {
		return a > b;
	}

	@Override
	public boolean ge(Float a, Float b) {
		return a >= b;
	}

	@Override
	public boolean lt(Float a, Float b) {
		return a < b;
	}

	@Override
	public boolean le(Float a, Float b) {
		return a <= b;
	}

	@Override
	public boolean eq(Float a, Float b) {
		return a == b;
	}

	@Getter(lazy = true)
	@Accessors(fluent = true)
	private final IntHolder ceil = mapInt(d -> (int) Math.ceil(d));

	@Getter(lazy = true)
	@Accessors(fluent = true)
	private final IntHolder floor = mapInt(d -> (int) Math.floor(d));

}
