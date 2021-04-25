package fr.lelouet.collectionholders.examples.direct;

import fr.lelouet.collectionholders.examples.Item;
import fr.lelouet.collectionholders.interfaces.numbers.IntHolder;

public interface StoreHolder {

	public IntHolder getPriceHolder(Item item);

}
