# CollectionsHolders

A library containing reactive holders. Those holders are a representation of data, and mimic the functional processing of stream (eg map, flatmap), but are designed to express formal transformation on varying data, including collections. 

They are especially suited to the locally cached representation of a remote resource, and its transformation using functional programing.

## Target usage

Those holders are designed, to separate the formal expression of a variable, from the actual acquisition of the data. This way you can define your relations between variables, and only when all the required variables are actually acquired, will the evaluation be performed.

### Chaing variables

Typically you create a observable list of prices, and you define the minimum price variable, as being a  function of that list : 

```java

minPrice = prices.min()
````
It does not matter if the list of prices is already fetched or not, you will be returned another variable. Once the list is actually fetched, it will be received by the  minimum price variable, and this variable becomes updated and can update other variables that depend on it.

### Multiple chain

If you have two items, you want the price of one of the first and two of the second, you can do something like

```java
var totalprices =
  pricesOf(item1).min()
  .add(
  pricesOf(item2).min().mult(2)
  );
```
Once the prices list updated, the minimum price variable becomes updated, and once the two minimum prices are updated, the totalprices variable is also updated. If later the prices of an item is changed in the list, then the totalprice will be updated to reflect those new prices.

### Difference with existing libraries.

This lib allows to apply functions to data, so it's close in that way to the java streams it mimics. However, streams are not observable.

This observable pattern is very close to javafx observable collections, however those collections need to be returned instanciated, and thus required to wait for the fetch() to end (blocking calls)

The java Future/CompletableFuture allow to work with data that is being fetched in a async way, however the futures feature does not handle data that will evolve later.

Finally, this is also close to the java Flux (reactive stream), which is a mix of java stream and Future ; however the flux aim at managing streams of data, while in the case of remote resource we handle a resource that evolves with time.


## Real use case

The original use case is to cache efficiently a resource that is fetched over internet.

If you want to access thousands of remote resources at the same time, you don't want to wait for the completion of a given request to start its following request. Instead you want to start as much requests as you can and when all the data are received process the full set of results. This is often dealt with thank to reactive/asyc libraries.

However, if the processing of the resources is expensive, you want to cache that result and also consider the product of that processing as being handled as a remote resource, that is non blocking. For example, if your first request access the list of mails you have on a remote mail provider, and the process is to access the sender of each mail, then the process part can be longer than the fetch of the mail list.

The solution is "inversion of control" : instead of requesting the resource and working with it, you express your need for the resource, and register handlers to be called when the resource is fetched. A cache manager is responsible for fetching the resource, caching it, and transmits to your handlers whenever the resource is updated.

This library addresses the problem of storing handlers associated to a representation, as well as synchronization issues, memory holes associated with the storage of lambdas.

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