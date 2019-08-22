package fr.lelouet.collectionholders.impl.numbers;

import fr.lelouet.collectionholders.interfaces.numbers.ObsIntHolder;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableIntegerValue;

public class ObsIntHolderImpl extends AObsNumberHolderImpl<Integer, ObsIntHolder> implements ObsIntHolder {

	public ObsIntHolderImpl() {
	}

	public ObsIntHolderImpl(int value) {
		this();
		set(value);
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

	private SimpleIntegerProperty obs = null;

	@Override
	public ObservableIntegerValue asObservableNumber() {
		if (obs == null) {
			waitData();
			synchronized (this) {
				if (obs == null) {
					obs = new SimpleIntegerProperty();
					follow(obs::set);
				}
			}
		}
		return obs;
	}

}
