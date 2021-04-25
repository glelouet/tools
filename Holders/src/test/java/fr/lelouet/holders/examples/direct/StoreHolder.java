package fr.lelouet.holders.examples.direct;

import fr.lelouet.holders.examples.Item;
import fr.lelouet.holders.interfaces.numbers.IntHolder;

public interface StoreHolder {

	public IntHolder getPriceHolder(Item item);

}
