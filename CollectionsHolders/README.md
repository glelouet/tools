# CollectionsHolders

A library containing several reactive collections. Those collections mimic the usual java collections, but are designed to allow formal expression on variables collections. 

## Target usage

Those collections are designed, to separate formal expression and resolution on instanciation. This way you can define your relations between variables, and only when all the required variables are actually set, will the evaluation be performed.

Typically you create a observable list of prices, and you define the minimum price variable, as being a  function of that list : 
`minPrice = prices.min()`. It does not matter if the list of prices is already fetched or not : once it is fetched, it sends it to  minimum price variable, and this variable becomes updated and can update other variables that depend on it. If you have two items, you want one of the first and two of the second, you can do something like`totalprices = prices(item1).min().add(prices(item2).min().mult(2)) ;`.
Once an item has its prices list updated, the minimum price variable becomes updated, and once the two minimum prices are updated, the totalprices variable is also updated. If later the prices of an item is changed, then the totalprice will be updated to reflect those new prices.

This usage is very close to java observable collections, however those collections need to be returned instanciated, and thus required to wait for the fetch() to end. This is also close to a "future", however the futures feature does not handle streams of data, that is variable that will evolve later.  This can be done but requires lot of work with synchronization. This library basically handles mostof the synchronization for such a kind of feature.



## Original use case

This original use case is : when you fetch resources over internet with a cache system. If you want to use thousands of internet resources at the same time, you don't want to wait for the completion of a request to start the next request. Instead you want to start as much requests as you can, asynchronously put the answers in holders, and when all the data are received process the full set of results. This however is still an issue if there is a cache : you don't want to start all the cache at the same time, rather keep updating the resources in an external process. What's more you want to express the usage of the resources in the same way, whether the data is already cached or not.

This leads to inversion of control : instead of requesting the resource and working with it, you register handlers to be called when the resource is fetched. The cache manager is responsible for fetching the resource, caching it, and giving it to your handlers whenever the resource is updated.

If the resource is represented by a single object, you just need need a holder on that object as well as way to know when the object is ready (ObservableObject in java are always ready) ; If your resource is actually eg an array, then you need a bit more powerful tools to work with them.

One of the original use case was : given a resource that is an array of prices for an item, how do I get the lower price ? Typically I want a way to do something like
`getPrices(item).min()` that gets me a future/observable value ; what's more I want to be able to make the lowest price of a list of items, something like 
`prices(items).mapInteger(prices->prices.min()).sum()`.

### Parallel and sequential calls

For example you want to get the lower price of one item, given several stores.

#### Sequential code

Typically you would do something like 

~~~~java
int min = Integer.MAX_VALUE;
for( Store store : stores)
  min=Math.min(min, store.getPrice(item));
~~~~

The issue with this sequential code is that the time to get the price is linear with the number of stores you have. If you have a huge number of stores, you will have a huge delay between the moment you start this code and the moment it exits.

#### Parallel code

So you usually want to get them in parallel. Using streams seems a good idea : 

~~~~java
int min = stores.parallelStream().maptToInt(s->s.getPrice(item)).min().orElseGet(()->Integer.MAX_VALUE)
~~~~

This way you are limited by only one request time (constant time). But is it really ? Actually, the parallelStream() works over a fixed amount of threads, eg 10. We decreased the time but it's still linear. Whatever is our number of threads, this algorithm still scales linearly with the number of stores times the amount of time needed to perform one request.

#### Data holder

What would be better, is to start the fetch in a new thread for each store ; then once all stores have returned, we iterate over the prices. Assuming spawning a new thread is done by the store, that would do : 

~~~~java
List<Holder<Integer>> holders = storeHolders.parallelStream().map(s->s.getPriceHolder(item)).collect(Collectors.toList());
int min = holders.stream().mapToInt(h->h.get()).min().orElseGet(()->Integer.MAX_VALUE);
~~~~

Of course we need to have a correct Store implementation, that creates the holder when needed, start a thread, and put the data in the holder when the thread terminates. However this can lead to some performances issues with a high number of requests, especially if we use threads (because threads are actually limited).

### Caching the data

In case we do a lot of requests at the same time, it may be wise to use caching. Caching allows us to keep a resource in memory once we request it, so next calls to that resource will return the cached value instead of fetching it again.

The resource is kept as long as it's relevant, typically the store returns not only the price, but also the expiration date.

#### Lazy caching

Lazy caching consists in fetching the resource again when we need that data and the expiration is reached. Since the data may be requested by several concurrent threads, we need to synchronize to be sure we don't fetch the same resource several times.
Removing the synchronization, it becomes

~~~~java
int getPrice(Item item) {
	if (!cachedPrices().containsKey(item)) {
	  Requested<Integer> ret = fetchPrice(item);
	  cachedPrices().put(item, ret);
	  return ret.get();
	}
	Requested<Integer> ret = cachedPrices().get(item);
	if (ret.getExpiry().getTime() <= System.currentTimeMillis()) {
	  ret = fetchPrice(item);
	  cachedPrices().put(item, ret);
	  return ret.get();
	}
	return ret.get();
}
~~~~

But now we have another issue : each call is done in sequential order, so we can't use parallelism, unless we actually return a holder on the price. Which means, we need to cache a holder on the price, also containing the expiration date.

#### Active caching

Active caching consists in fetching the resource once when needed, and then fetching it again when the expiration date is reached. Typically this is done by a dedicated updater manager.
The interest lies on the idea that after the first request, all call will be almost instantaneous. 

The issue is that the first call can still block the process for a long time. For this reason, caching and returning not the data but a holder is the way to go.
In this case, it may be convenient to also be notified whenever the data stored in the holder is modified.

The collections in this project aim at providing the holders for such a use case.