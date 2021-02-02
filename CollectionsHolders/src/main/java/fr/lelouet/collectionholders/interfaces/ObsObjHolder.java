package fr.lelouet.collectionholders.interfaces;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import fr.lelouet.collectionholders.interfaces.collections.ObsListHolder;
import fr.lelouet.collectionholders.interfaces.collections.ObsMapHolder;
import fr.lelouet.collectionholders.interfaces.collections.ObsSetHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsBoolHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsDoubleHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsFloatHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsIntHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsLongHolder;

/**
 * holder on a single object.
 * <p>
 * call to listeners should be synchronized in the implementation, especially
 * over the hold data so that no follower can be added while a new data is being
 * set.
 * </p>
 *
 * <p>
 * This structure allow to manipulate future values, whatever be the current
 * state of the value generation.<br />
 * eg if you have a sensor, that retrieve data once every 30s when invoked, you
 * don't want to wait 30s for the sensor to retrieve the first data, especially
 * if you have several of them.<br />
 * Instead, such a sensor should return a ObsObjHolder&lt;value&gt; which you
 * can follow to trigger event.<br />
 *
 * </p>
 *
 * @param <U>
 *          the type that is hold. Specific implementation exist for basic
 *          numbers in the numbers package
 */
public interface ObsObjHolder<U> {

	/**
	 * return the internal object once it's available. If this is not available,
	 * blocks until then
	 */
	public U get();

	/**
	 *
	 * @param defaultValue
	 *          value to hold until this' data is available
	 * @return a new object holder that contains the default value until it's
	 *         replaced by this' value.
	 */
	public ObsObjHolder<U> or(U defaultValue);

	/**
	 *
	 * @param <V>
	 *          returned hold type
	 * @param condition
	 *          a condition on this' data
	 * @param whentrue
	 *          converter of the data when the condition is met.
	 * @param whenFalse
	 *          converter of the data when the condition is not met.
	 * @return a new object holder that will contain a the application of whentrue
	 *         to this' data when the condition is met, or the application of
	 *         whenfalse otherwise
	 */
	public default <V> ObsObjHolder<V> when(Predicate<U> condition, Function<U, V> whentrue, Function<U, V> whenFalse) {
		return map(o -> condition.test(o) ? whentrue.apply(o) : whenFalse.apply(o));
	}

	/**
	 * add a consumer that follows the data inside. if there is already data, the
	 * consumer receives that data before exiting this method
	 *
	 * @param cons
	 *          the consumer that will receive new values.
	 * @param holder
	 *          the holder that ensure the consumer is useful. if omitted or set
	 *          to null, the class is used instead. Once the holder is no more
	 *          weak reachable, the listener will be removed.
	 */
	@SuppressWarnings("unchecked")
	public void follow(Consumer<U> cons, Consumer<Object>... holders);

	/**
	 * add a consumer that follows the data inside. if there is already data, the
	 * consumer receives that data before exiting this method
	 *
	 * @param cons
	 */
	public default void follow(Consumer<U> cons) {
		follow(cons, (Consumer<Object>[]) null);
	}

	default ObsObjHolder<U> peek(Consumer<U> observer) {
		follow(observer, (Consumer<Object>[]) null);
		return this;
	}

	/**
	 * remove a follower added with {@link #follow(Consumer)}
	 *
	 * @param cons
	 */
	public void unfollow(Consumer<U> cons);

	/**
	 * create a new obsObjHolder that mirros the value contained after
	 * transforming it.
	 *
	 * @param <V>
	 *          the type of the value contained
	 * @param mapper
	 *          the method to transform the value
	 * @return a new holder.
	 */
	<V> ObsObjHolder<V> map(Function<U, V> mapper);

	/**
	 * create a new object that mirrors the value hold in this, by transforming it
	 * into an int.
	 *
	 * @param mapper
	 *          the method to transform the value
	 * @return a new holder
	 */
	ObsIntHolder mapInt(ToIntFunction<U> mapper);

	/**
	 * create a new object that mirrors the value hold in this, by transforming it
	 * into a long.
	 *
	 * @param mapper
	 *          the method to transform the value
	 * @return a new holder
	 */
	ObsLongHolder mapLong(ToLongFunction<U> mapper);

	/**
	 * create a new object that mirrors the value hold in this, by transforming it
	 * into a double.
	 *
	 * @param mapper
	 *          the method to transform the value
	 * @return a new holder
	 */
	ObsDoubleHolder mapDouble(ToDoubleFunction<U> mapper);

	ObsFloatHolder mapFloat(ToDoubleFunction<U> mapper);

	<K, V> ObsMapHolder<K, V> mapMap(Function<U, Map<K, V>> mapper);

	<K> ObsListHolder<K> mapList(Function<U, List<K>> mapper);

	/**
	 * create a new object that mirrors the value hold in this, by transforming it
	 * into a boolean.
	 *
	 * @param mapper
	 *          the method to transform the value
	 * @return a new holder
	 */
	ObsBoolHolder test(Predicate<U> test);

	/**
	 * create a new observable list that mirrors the value hold in this, after
	 * transforming it into a list
	 *
	 * @param <V>
	 *          the internal type of the list we generate
	 * @param generator
	 *          the function that transforms the value hold, in an iterable over
	 *          the list type.
	 * @return a new list holder.
	 */
	<V> ObsListHolder<V> toList(Function<U, Iterable<V>> generator);

	/**
	 * create a new observable set that mirrors the value hold in this, after
	 * transforming it into a set
	 *
	 * @param <V>
	 *          the internal type of the set we generate
	 * @param generator
	 *          the function that transforms the value hold, in an iterable over
	 *          the set type.
	 * @return a new set holder.
	 */
	<V> ObsSetHolder<V> toSet(Function<U, Iterable<V>> generator);

	/**
	 * unpack the internal value into an internal observable field.
	 * <p>
	 * eg if a holder contains an access, and that access can give an observable
	 * String lastConnect, we can have a obsHolder on the lastConnect value that
	 * is updated when the access or its lastConnect is updated
	 * </p>
	 *
	 * @param <V>
	 * @param unpacker
	 * @return
	 */
	<V> ObsObjHolder<V> unPack(Function<U, ObsObjHolder<V>> unpacker);

	public <V, R> ObsObjHolder<R> combine(ObsObjHolder<V> other, BiFunction<U, V, R> mapper);

	@SuppressWarnings("unchecked")
	public <V> ObsObjHolder<V> reduce(Function<List<? extends U>, V> reducer, ObsObjHolder<? extends U> first,
			ObsObjHolder<? extends U>... others);

	//
	// static utility methods that require the specification of a constructor
	//

	/**
	 * map an obs object into another one through a mapping function.
	 *
	 * @param <U>
	 *          The type of the object hold
	 * @param <V>
	 *          The type of the object mapped
	 * @param <C>
	 *          The Object holder type on V that we create and return
	 * @param from
	 *          the original object holder
	 * @param creator
	 *          the supplier for the observable holder returned. Typically the
	 *          constructor.
	 * @param mapper
	 *          the function to translate a U into a V
	 * @return a new constrained variable.
	 */
	@SuppressWarnings("unchecked")
	public static <U, V, C extends RWObsObjHolder<V> & Consumer<Object>> C map(ObsObjHolder<U> from, Supplier<C> creator,
			Function<U, V> mapper) {
		C ret = creator.get();
		from.follow((newValue) -> {
			ret.set(mapper.apply(newValue));
		}, ret);
		return ret;
	}

	/**
	 * combine two different observable type holder into a third one. If you want
	 * more than two items, then use {@link #reduce(Supplier, Function, List)}
	 *
	 * @param <AType>
	 *          type of the first object hold
	 * @param <BType>
	 *          type of second object hold
	 * @param <ResType>
	 *          joined type
	 * @param <HolderType>
	 *          holder implementation type to hold the joined value
	 * @param a
	 *          first object to listen
	 * @param b
	 *          second object to listen
	 * @param creator
	 *          function to create a holder on the result
	 * @param joiner
	 *          function to be called to join an Atype and a BType into a ResType.
	 * @return a new variable bound to the application of the joiner on a and b.
	 */
	@SuppressWarnings("unchecked")
	public static <AType, BType, ResType, HolderType extends RWObsObjHolder<ResType> & Consumer<Object>> HolderType combine(
			ObsObjHolder<AType> a, ObsObjHolder<BType> b, Supplier<HolderType> creator,
			BiFunction<AType, BType, ResType> joiner) {
		HolderType ret = creator.get();
		boolean[] receipt = new boolean[] { false, false };
		Object[] received = new Object[2];
		Runnable update = () -> {
			if (receipt[0] && receipt[1]) {
				ResType joined = joiner.apply((AType) received[0], (BType) received[1]);
				ret.set(joined);
			}
		};
		a.follow(newa -> {
			synchronized (received) {
				received[0] = newa;
				receipt[0] = true;
				update.run();
			}
		}, ret);
		b.follow(newb -> {
			synchronized (received) {
				received[1] = newb;
				receipt[1] = true;
				update.run();
			}
		}, ret);
		return ret;
	}

	/**
	 * reduces a list of several observable object holders of the same type, into
	 * another type.
	 * <p>
	 * If the types are different in the variables, then object will be used. Be
	 * sure to cast the items accordingly in
	 * </p>
	 *
	 * @param <U>
	 *          the internal type of the holders that are reduced. If various
	 *          types are hold, this should be Object.
	 * @param <V>
	 *          the type produced from the reduction.
	 * @param <HolderType>
	 *          the holder type we return.
	 * @param the
	 *          list of variables we want to use
	 * @param creator
	 *          typically constructor on the writable implementation.
	 * @param reducer
	 *          transforms the list in the hold type.
	 * @return a new variable bound to the reduction of the list.
	 */
	@SuppressWarnings("unchecked")
	public static <U, V, HolderType extends RWObsObjHolder<V> & Consumer<Object>> HolderType reduce(
			List<? extends ObsObjHolder<? extends U>> vars, Supplier<HolderType> creator,
			Function<List<? extends U>, V> reducer) {
		HolderType ret = creator.get();
		if (vars == null || vars.isEmpty()) {
			ret.set(null);
			return ret;
		}
		ObsObjHolder<U>[] holders = vars.toArray(ObsObjHolder[]::new);
		HashMap<Integer, U> received = new HashMap<>();
		for (int i = 0; i < holders.length; i++) {
			int index = i;
			ObsObjHolder<U> h = holders[i];
			h.follow((newValue) -> {
				synchronized (received) {
					received.put(index, newValue);
					if (received.size() == holders.length) {
						V joined = reducer
								.apply(IntStream.range(0, received.size()).mapToObj(received::get).collect(Collectors.toList()));
						ret.set(joined);
					}
				}
			}, ret);
		}
		return ret;
	}

	/**
	 * Unpack an observable into another one. Typically used to transform an
	 * obsobjhoder<obsobjhodler<U>> into the holder on the internal type
	 * obsobjhodler<U>, but can be used to other goals.
	 *
	 * @param <U>
	 *          internal type hold
	 * @param <V>
	 *          internal type mapped
	 * @param <H>
	 *          holder type returned.
	 * @param target
	 *          the holder on a type we want to unpack
	 * @param creator
	 *          creation of the holder type, typically the constructor
	 * @param unpacker
	 *          function to transform the type hold into the mapped type.
	 * @return a new variable that holds the mapped type.
	 */
	@SuppressWarnings("unchecked")
	public static <U, V, H extends RWObsObjHolder<V> & Consumer<Object>> H unPack(ObsObjHolder<U> target,
			Supplier<H> creator, Function<U, ObsObjHolder<V>> unpacker) {
		H ret = creator.get();
		ObsObjHolder<V>[] storeHolder = new ObsObjHolder[1];
		Consumer<V> cons = ret::set;
		target.follow(u -> {
			synchronized (storeHolder) {
				if (storeHolder[0] != null) {
					storeHolder[0].unfollow(cons);
				}
				storeHolder[0] = unpacker.apply(u);
				if (storeHolder[0] != null) {
					storeHolder[0].follow(cons, ret);
				}
			}
		}, ret);
		return ret;
	}
}
