package fr.lelouet.collectionholders.interfaces;

import java.util.Set;

import javafx.collections.SetChangeListener;

public interface ObsSetHolder<U> extends ObsCollectionHolder<U, Set<U>, SetChangeListener<? super U>> {

	public ObsObjHolder<Boolean> contains(U value);

	public ObsObjHolder<Boolean> contains(ObsObjHolder<U> value);

}
