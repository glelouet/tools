package fr.lelouet.collectionholders.impl.numbers;

import fr.lelouet.collectionholders.impl.NotNullObsObjHolderImpl;
import fr.lelouet.collectionholders.interfaces.numbers.ObsIntHolder;
import javafx.beans.value.ObservableValue;

public class ObsIntHolderImpl extends NotNullObsObjHolderImpl<Integer> implements ObsIntHolder {

	public ObsIntHolderImpl(ObservableValue<Integer> underlying) {
		super(underlying);
	}

	@Override
	public ObsIntHolder create(ObservableValue<Integer> var) {
		return new ObsIntHolderImpl(var);
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
