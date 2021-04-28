package fr.lelouet.tools.holders.examples.lazycache;

import java.util.HashMap;

import fr.lelouet.tools.holders.examples.Item;

public interface StoreCache {

	public HashMap<Item, Requested<Integer>> cachedPrices();

	public Requested<Integer> fetchPrice(Item item);

}
