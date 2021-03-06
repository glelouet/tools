package fr.lelouet.tools.holders.cache;

import java.lang.ref.WeakReference;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToLongFunction;

import fr.lelouet.tools.holders.interfaces.ObjHolder;
import fr.lelouet.tools.holders.interfaces.RWObjHolder;
import fr.lelouet.tools.holders.interfaces.collections.MapHolder;
import fr.lelouet.tools.holders.interfaces.numbers.IntHolder;

/**
 * common class for caching various fetching methods.
 *
 * @author glelouet
 *
 * @param <Resource>
 *          the resource that is cached for a given URI.
 * @param <Hold>
 *          the type of holder that is created for each resource. It's important
 *          since a cache can produce an {@link IntHolder} to store an int
 *          instead of an Holder&lt;Integer&gt;, or a {@link MapHolder} instead
 *          of a Holder&lt;Map&gt; because the former allows more expressive
 *          manipulation.
 */
public class URIBasedCache<Resource, Hold extends ObjHolder<Resource>>
extends WeakCache<String, Hold> {

	/**
	 * private because it's better to use the {@link WeakCache} directly in that
	 * case.
	 *
	 * @param generator
	 */

	private URIBasedCache(Function<String, Hold> generator) {
		super(generator);
	}

	/**
	 * create a new object.
	 *
	 * @param <Intermediate>
	 *          The intermediate type. eg if the resource is fetched through http,
	 *          it can be an HTTPRespone. It can also be the same as the Resource.
	 * @param <RWHold>
	 *          the actual RW holder type (which must be then converted in an RO
	 *          holder type)
	 * @param executor
	 *          The object that will execute a Callable<?> after given delay in
	 *          ms.
	 * @param init
	 *          The constructor of the RW holder, eg ListHolderImpl::new
	 * @param convert
	 *          the converter from a RW type to a RO type, eg i->i sice most of
	 *          the time the RW types implements RO.
	 * @param fetch
	 *          function to convert an URI and the last received Intermediate type
	 *          into the next value. The intermediate type is used eg when the
	 *          request is http and requires the ETag from the last entity
	 *          received.
	 * @param newValue
	 *          test if the new entity received is modified. Typically this should
	 *          be o->true unless the resource is complex and there is a etag-like
	 *          feature to test difference.
	 * @param extractor
	 *          convert the received entity to the Resource. If the code directly
	 *          fetches a resource, then this can be r->r.
	 * @param reschedule
	 *          test if the received entity requires a new fetch. Most of the time
	 *          should be o->true, unless we have an indication the resource will
	 *          never be fetchable later, eg our rights have been revoked or the
	 *          resource was being built and is now complete
	 * @param nextSchedule
	 *          called when reschedule is true, indicates the delay in ms after
	 *          which the executor should run the fecth method again.
	 */
	public <Intermediate, RWHold extends RWObjHolder<Resource>> URIBasedCache(BiConsumer<Callable<?>, Long> executor,
			Supplier<RWHold> init, Function<RWHold, Hold> convert, BiFunction<String, Intermediate, Intermediate> fetch,
			Predicate<Intermediate> newValue, Function<Intermediate, Resource> extractor, Predicate<Intermediate> reschedule,
			ToLongFunction<Intermediate> nextSchedule) {
		this(generatorSync(executor, init, convert, fetch, newValue, extractor, reschedule, nextSchedule));
	}

	public static <Resource, Hold extends ObjHolder<Resource>, Intermediate, RWHold extends RWObjHolder<Resource>> URIBasedCache<Resource, Hold> sync(
			BiConsumer<Callable<?>, Long> executor, Supplier<RWHold> init, Function<RWHold, Hold> convert,
			BiFunction<String, Intermediate, Intermediate> fetch, Predicate<Intermediate> newValue,
			Function<Intermediate, Resource> extractor, Predicate<Intermediate> reschedule,
			ToLongFunction<Intermediate> nextSchedule) {
		return new URIBasedCache<>(
				generatorSync(executor, init, convert, fetch, newValue, extractor, reschedule, nextSchedule));
	}

	protected static <
	Resource,
	Intermediate,
	Hold extends ObjHolder<Resource>,
	RWHold extends RWObjHolder<Resource>
	> Function<String, Hold> generatorSync(
			BiConsumer<Callable<?>, Long> executor,
			Supplier<RWHold> init,
			Function<RWHold, Hold> convert,
			BiFunction<String, Intermediate, Intermediate> fetch,
			Predicate<Intermediate> newValue,
			Function<Intermediate, Resource> extractor,
			Predicate<Intermediate> reschedule,
			ToLongFunction<Intermediate> nextSchedule
			) {
		return uri -> generateSync(executor, init, convert, last -> fetch.apply(uri, last), newValue, extractor,
				reschedule,
				nextSchedule);
	}

	protected static <
	Resource,
	Intermediate,
	Hold extends ObjHolder<Resource>,
	RWHold extends RWObjHolder<Resource>
	> Hold generateSync(
			BiConsumer<Callable<?>, Long> executor,
			Supplier<RWHold> init,
			Function<RWHold, Hold> convert,
			Function<Intermediate, Intermediate> fetch,
			Predicate<Intermediate> newValue,
			Function<Intermediate, Resource> extractor,
			Predicate<Intermediate> reschedule,
			ToLongFunction<Intermediate> nextSchedule
			) {
		RWHold store = init.get();
		return generateSync(executor, store, convert.apply(store), fetch, newValue, extractor, reschedule, nextSchedule);

	}

	protected static <
	Resource,
	Intermediate,
	Hold extends ObjHolder<Resource>,
	RWHold extends RWObjHolder<Resource>
	> Hold generateSync(
			BiConsumer<Callable<?>, Long> executor,
			RWHold store,
			Hold cached,
			Function<Intermediate, Intermediate> fetch,
			Predicate<Intermediate> newValue,
			Function<Intermediate, Resource> extractor,
			Predicate<Intermediate> reschedule,
			ToLongFunction<Intermediate> nextSchedule
			) {
				Callable<Void> exec = new SelfSchedule<>(executor, store, fetch, newValue, extractor, reschedule, nextSchedule);
		executor.accept(exec, 0l);
		return cached;
	}

	/**
	 * Once executed, will fetch the resource and self schedule itself to update
	 * the resource later.
	 * <p>
	 * The holder to set the resource is actually internally a WeakReference so
	 * that this class does not hold it. If the store becomes null later, then
	 * this stops the scheduling. This is required because otherwise the
	 * SelfSchedule being strong refered in the executor, then the holder would be
	 * also strong referenced and this could lead to memory leakage when the
	 * selfschedule is the only one strongly referencing it.
	 * </p>
	 *
	 * @author glelouet
	 *
	 * @param <Resource>
	 * @param <Intermediate>
	 * @param <RWHold>
	 */
	protected static class SelfSchedule<
	Resource,
	Intermediate,
	RWHold extends RWObjHolder<Resource>
	> implements Callable<Void> {

		private final BiConsumer<Callable<?>, Long> executor;
		private final WeakReference<RWHold> store;
		private final Function<Intermediate, Intermediate> fetch;
		private final Predicate<Intermediate> newValue;
		private final Function<Intermediate, Resource> extractor;
		private final Predicate<Intermediate> reschedule;
		private final ToLongFunction<Intermediate> nextSchedule;
		private Intermediate last = null;

		public SelfSchedule(
				BiConsumer<Callable<?>, Long> executor,
				RWHold store,
				Function<Intermediate, Intermediate> fetch,
				Predicate<Intermediate> newValue,
				Function<Intermediate, Resource> extractor,
				Predicate<Intermediate> reschedule,
				ToLongFunction<Intermediate> nextSchedule) {
			this.executor = executor;
			this.store = new WeakReference<>(store);
			this.fetch = fetch;
			this.newValue = newValue;
			this.extractor = extractor;
			this.reschedule = reschedule;
			this.nextSchedule = nextSchedule;
		}

		@Override
		public Void call() {
			RWHold storeRef = store.get();
			if (storeRef == null) {
				return null;
			}
			last = fetch.apply(last);
			if (newValue.test(last)) {
				storeRef.set(extractor.apply(last));
			}
			if (reschedule.test(last)) {
				executor.accept(this, nextSchedule.applyAsLong(last));
			}
			return null;
		}

	}

}
