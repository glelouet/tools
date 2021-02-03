package fr.lelouet.collectionholders.examples.direct;

import fr.lelouet.collectionholders.examples.Item;
import fr.lelouet.collectionholders.interfaces.numbers.ObsIntHolder;

public interface StoreHolder {

	public ObsIntHolder getPriceHolder(Item item);

}
