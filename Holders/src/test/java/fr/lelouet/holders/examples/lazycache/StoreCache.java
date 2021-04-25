package fr.lelouet.holders.examples.lazycache;

import java.util.HashMap;

import fr.lelouet.holders.examples.Item;

public interface StoreCache {

	public HashMap<Item, Requested<Integer>> cachedPrices();

	public Requested<Integer> fetchPrice(Item item);

}
