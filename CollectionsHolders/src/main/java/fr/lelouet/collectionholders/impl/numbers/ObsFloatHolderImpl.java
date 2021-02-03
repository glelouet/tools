package fr.lelouet.collectionholders.impl.numbers;

import fr.lelouet.collectionholders.interfaces.numbers.ObsFloatHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsIntHolder;
import lombok.Getter;
import lombok.experimental.Accessors;

public class ObsFloatHolderImpl extends AObsNumberHolderImpl<Float, ObsFloatHolder> implements ObsFloatHolder {

	public static ObsFloatHolderImpl of(Number value) {
		return new ObsFloatHolderImpl(value.floatValue());
	}

	public ObsFloatHolderImpl() {
	}

	public ObsFloatHolderImpl(float value) {
		super(value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ObsFloatHolderImpl create() {
		return new ObsFloatHolderImpl();
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
	private final ObsIntHolder ceil = mapInt(d -> (int) Math.ceil(d));

	@Getter(lazy = true)
	@Accessors(fluent = true)
	private final ObsIntHolder floor = mapInt(d -> (int) Math.floor(d));

}
