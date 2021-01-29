package fr.lelouet.collectionholders.interfaces.numbers;

import java.util.function.Consumer;

public interface ObsFloatHolder extends ObsNumberHolder<Float, ObsFloatHolder> {

	public ObsIntHolder ceil();

	public ObsIntHolder floor();

	@Override
	ObsFloatHolder peek(Consumer<Float> observer);

}
