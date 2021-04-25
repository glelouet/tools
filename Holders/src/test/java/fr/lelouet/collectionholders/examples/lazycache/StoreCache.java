package fr.lelouet.collectionholders.examples.lazycache;

import java.util.HashMap;

import fr.lelouet.collectionholders.examples.Item;

public interface StoreCache {

	public HashMap<Item, Requested<Integer>> cachedPrices();

	public Requested<Integer> fetchPrice(Item item);

}
