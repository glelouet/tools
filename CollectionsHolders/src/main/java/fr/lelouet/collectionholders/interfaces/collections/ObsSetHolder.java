package fr.lelouet.collectionholders.interfaces.collections;

import java.util.Set;

import fr.lelouet.collectionholders.interfaces.ObsObjHolder;
import javafx.collections.SetChangeListener;

public interface ObsSetHolder<U> extends ObsCollectionHolder<U, Set<U>, SetChangeListener<? super U>> {

	public ObsObjHolder<Boolean> contains(U value);

	public ObsObjHolder<Boolean> contains(ObsObjHolder<U> value);

}
