package fr.lelouet.collectionholders.examples;

import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.lelouet.collectionholders.examples.direct.Store;
import fr.lelouet.collectionholders.examples.direct.StoreHolder;
import fr.lelouet.collectionholders.impl.collections.ObsListHolderImpl;
import fr.lelouet.collectionholders.impl.numbers.ObsIntHolderImpl;
import fr.lelouet.collectionholders.interfaces.numbers.ObsIntHolder;

public class Example1 {

	/** fetch the lowest price from a list of stores */
	@Test(timeOut = 500)
	public void test() {

		Item item = new Item();
		// create a list of stores, one returns the price 5 for all items, the other
		// returns 6.
		List<Store> stores = Arrays.asList(it -> 5, it -> 6);

		// direct sequential method
		{
			int min = Integer.MAX_VALUE;
			for (Store store : stores) {
				min = Math.min(min, store.getPrice(item));
			}
			Assert.assertEquals(min, 5);
		}
		// with parallel stream to be faster
		{
			int min = stores.parallelStream().mapToInt(s -> s.getPrice(item)).min().orElse(Integer.MAX_VALUE);
			Assert.assertEquals(min, 5);
		}
		// with holders. Here we create a store holder that return 12 as the price
		// for any
		// item, and another store holder that returns 14.
		List<StoreHolder> storeHolders = Arrays.asList(it -> ObsIntHolderImpl.of(12), it -> ObsIntHolderImpl.of(14));
		{
			ObsListHolderImpl<StoreHolder> holders = new ObsListHolderImpl<>(storeHolders);
			ObsIntHolder minPrice = holders.unpackItems(sh -> sh.getPriceHolder(item))
					.mapInt(l -> l.stream().mapToInt(i -> i).min().getAsInt());
			Assert.assertEquals(minPrice.get(), (Integer) 12);
		}
	}

}
