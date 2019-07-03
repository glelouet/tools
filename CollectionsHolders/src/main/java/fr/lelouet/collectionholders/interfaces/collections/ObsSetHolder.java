package fr.lelouet.collectionholders.interfaces.collections;

import java.util.Set;

import fr.lelouet.collectionholders.interfaces.ObsObjHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsBoolHolder;
import javafx.collections.SetChangeListener;

public interface ObsSetHolder<U> extends ObsCollectionHolder<U, Set<U>, SetChangeListener<? super U>> {

	/**
	 * create a variable which is set to true whenever this contains a specific
	 * value
	 *
	 * @param value
	 *          a value
	 * @return a new variable
	 */
	public ObsBoolHolder contains(U value);

	/**
	 * create a variable which is set to true whenever this contains the value
	 * hold in a variable. The returned variable is updated whenever the value
	 * variable is modified or this receives data.
	 *
	 * @param value
	 *          a value variable
	 * @return a new variable
	 */
	public ObsBoolHolder contains(ObsObjHolder<U> value);

}
