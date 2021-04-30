package fr.lelouet.tools.holders.cache;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import fr.lelouet.tools.holders.impl.ObjHolderSimple;
import fr.lelouet.tools.holders.impl.collections.ListHolderImpl;
import fr.lelouet.tools.holders.impl.collections.MapHolderImpl;
import fr.lelouet.tools.holders.impl.collections.SetHolderImpl;
import fr.lelouet.tools.holders.impl.numbers.BoolHolderImpl;
import fr.lelouet.tools.holders.impl.numbers.DoubleHolderImpl;
import fr.lelouet.tools.holders.impl.numbers.FloatHolderImpl;
import fr.lelouet.tools.holders.impl.numbers.IntHolderImpl;
import fr.lelouet.tools.holders.impl.numbers.LongHolderImpl;
import fr.lelouet.tools.holders.interfaces.ObjHolder;
import fr.lelouet.tools.holders.interfaces.RWObjHolder;
import fr.lelouet.tools.holders.interfaces.collections.ListHolder;
import fr.lelouet.tools.holders.interfaces.collections.MapHolder;
import fr.lelouet.tools.holders.interfaces.collections.SetHolder;
import fr.lelouet.tools.holders.interfaces.numbers.BoolHolder;
import fr.lelouet.tools.holders.interfaces.numbers.DoubleHolder;
import fr.lelouet.tools.holders.interfaces.numbers.FloatHolder;
import fr.lelouet.tools.holders.interfaces.numbers.IntHolder;
import fr.lelouet.tools.holders.interfaces.numbers.LongHolder;

/**
 * Periodic execution cache helpers. Note that since the execution is AFTER the
 * given delay, the the actual period may be higher that the one provided.
 * <p>
 * one generic helper, which is helped by the helpers for int, long, double,
 * float, boolean, T, List, Set, Map.
 * </p>
 *
 * @author glelouet
 *
 */
public class PeriodicFetch {

	private PeriodicFetch() {
	};

	/**
	 * create a periodic fetcher cache.
	 *
	 * @param <Resource>
	 * @param <Hold>
	 * @param <RWHold>
	 * @param executor
	 *          the executor that handles the actual calls. Typically if you have
	 *          a {@link ScheduledThreadPoolExecutor} exec, this would be (r,l)->
	 *          exec.schedule(r, l, TimeUnit.millisecond) .
	 * @param init
	 *          constructor of a RW holder.
	 * @param convert
	 *          conversion from a RW to a RO holder, typically o->o
	 * @param access
	 *          fetch the resource for a given URI.
	 * @param delay_MS
	 *          the minimum delay in ms between the end of the fetch and the start
	 *          of the next.
	 * @return
	 */
	public static <
	Resource,
	Hold extends ObjHolder<Resource>,
	RWHold extends RWObjHolder<Resource>>
	URIBasedCache<Resource, Hold> cache(
					BiConsumer<Callable<?>, Long> executor,
			Supplier<RWHold> init,
			Function<RWHold, Hold> convert,
			Function<String, Resource> access,
			long delay_MS
			) {
		return new <Resource, RWHold>URIBasedCache<Resource, Hold>(executor, init, convert,
				(s, l) -> access.apply(s), o -> true, o -> o, o -> true, o -> delay_MS);
	}

	public static <
	Resource,
	Hold extends ObjHolder<Resource>,
	RWHold extends RWObjHolder<Resource>>
	URIBasedCache<Resource, Hold> cache(
			ScheduledExecutorService exec,
			Supplier<RWHold> init,
			Function<RWHold, Hold> convert,
			Function<String, Resource> access,
			long delay_MS
			) {
		return cache((r, l) -> exec.schedule(r, l, TimeUnit.MILLISECONDS), init, convert, access, delay_MS);
	}

	public static URIBasedCache<Integer, IntHolder> cacheToInt(ScheduledExecutorService exec,
			Function<String, Integer> access, long delay_MS) {
		return cache((r, l) -> exec.schedule(r, delay_MS, TimeUnit.MILLISECONDS), IntHolderImpl::new, i -> i, access,
				delay_MS);
	}

	public static URIBasedCache<Long, LongHolder> cacheToLong(ScheduledExecutorService exec,
			Function<String, Long> access, long delay_MS) {
		return cache((r, l) -> exec.schedule(r, delay_MS, TimeUnit.MILLISECONDS), LongHolderImpl::new, i -> i, access,
				delay_MS);
	}

	public static URIBasedCache<Float, FloatHolder> cacheToFloat(ScheduledExecutorService exec,
			Function<String, Float> access, long delay_MS) {
		return cache((r, l) -> exec.schedule(r, delay_MS, TimeUnit.MILLISECONDS), FloatHolderImpl::new, i -> i, access,
				delay_MS);
	}

	public static URIBasedCache<Double, DoubleHolder> cacheToDouble(ScheduledExecutorService exec,
			Function<String, Double> access, long delay_MS) {
		return cache((r, l) -> exec.schedule(r, delay_MS, TimeUnit.MILLISECONDS), DoubleHolderImpl::new, i -> i, access,
				delay_MS);
	}

	public static URIBasedCache<Boolean, BoolHolder> cacheToBool(ScheduledExecutorService exec,
			Function<String, Boolean> access, long delay_MS) {
		return cache((r, l) -> exec.schedule(r, delay_MS, TimeUnit.MILLISECONDS), BoolHolderImpl::new, i -> i, access,
				delay_MS);
	}

	public static <T> URIBasedCache<T, ObjHolder<T>> cacheToObj(ScheduledExecutorService exec,
			Function<String, T> access, long delay_MS) {
		return cache((r, l) -> exec.schedule(r, delay_MS, TimeUnit.MILLISECONDS), ObjHolderSimple::new, i -> i, access,
				delay_MS);
	}

	public static <T> URIBasedCache<List<T>, ListHolder<T>> cacheToList(ScheduledExecutorService exec,
			Function<String, List<T>> access, long delay_MS) {
		return cache((r, l) -> exec.schedule(r, delay_MS, TimeUnit.MILLISECONDS), ListHolderImpl::new, i -> i, access,
				delay_MS);
	}

	public static <T> URIBasedCache<Set<T>, SetHolder<T>> cacheToSet(ScheduledExecutorService exec,
			Function<String, Set<T>> access, long delay_MS) {
		return cache((r, l) -> exec.schedule(r, delay_MS, TimeUnit.MILLISECONDS), SetHolderImpl::new, i -> i, access,
				delay_MS);
	}

	public static <K, V> URIBasedCache<Map<K, V>, MapHolder<K, V>> cacheToMap(ScheduledExecutorService exec,
			Function<String, Map<K, V>> access, long delay_MS) {
		return cache((r, l) -> exec.schedule(r, delay_MS, TimeUnit.MILLISECONDS), MapHolderImpl::new, i -> i, access,
				delay_MS);
	}

}
