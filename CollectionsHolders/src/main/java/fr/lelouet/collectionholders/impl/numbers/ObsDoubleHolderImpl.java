package fr.lelouet.collectionholders.impl.numbers;

import java.util.function.Consumer;

import fr.lelouet.collectionholders.impl.AObsObjHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsDoubleHolder;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableDoubleValue;

public class ObsDoubleHolderImpl extends AObsNumberHolderImpl<Double, ObsDoubleHolder> implements ObsDoubleHolder {

	public ObsDoubleHolderImpl() {
	}

	public ObsDoubleHolderImpl(double value) {
		this();
		set(value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ObsDoubleHolderImpl create() {
		return new ObsDoubleHolderImpl();
	}

	private ObsIntHolderImpl ceil = null;

	@Override
	public ObsIntHolderImpl ceil() {
		if (ceil == null) {
			synchronized (this) {
				if (ceil == null) {
					ceil = AObsObjHolder.map(this, ObsIntHolderImpl::new, a -> (int) Math.ceil(a));
				}
			}
		}
		return ceil;
	}

	private ObsIntHolderImpl floor = null;

	@Override
	public ObsIntHolderImpl floor() {
		if (floor == null) {
			synchronized (this) {
				if (floor == null) {
					floor = AObsObjHolder.map(this, ObsIntHolderImpl::new, a -> (int) Math.floor(a));
				}
			}
		}
		return floor;
	}

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

	private SimpleDoubleProperty obs = null;

	@Override
	public ObservableDoubleValue asObservableNumber() {
		if (obs == null) {
			waitData();
			synchronized (this) {
				if (obs == null) {
					obs = new SimpleDoubleProperty();
					follow(obs::set);
				}
			}
		}
		return obs;
	}

	@Override
	public ObsDoubleHolderImpl peek(Consumer<Double> observer) {
		follow(observer);
		return this;
	}
}
