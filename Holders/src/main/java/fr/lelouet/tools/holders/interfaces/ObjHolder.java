package fr.lelouet.tools.holders.interfaces;

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

import fr.lelouet.tools.holders.impl.ObjHolderSimple;
import fr.lelouet.tools.holders.interfaces.collections.ListHolder;
import fr.lelouet.tools.holders.interfaces.collections.MapHolder;
import fr.lelouet.tools.holders.interfaces.collections.SetHolder;
import fr.lelouet.tools.holders.interfaces.numbers.BoolHolder;
import fr.lelouet.tools.holders.interfaces.numbers.DoubleHolder;
import fr.lelouet.tools.holders.interfaces.numbers.FloatHolder;
import fr.lelouet.tools.holders.interfaces.numbers.IntHolder;
import fr.lelouet.tools.holders.interfaces.numbers.LongHolder;

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
public interface ObjHolder<U> {

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
	public ObjHolder<U> or(U defaultValue);

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
	public default <V> ObjHolder<V> when(Predicate<U> condition, Function<U, V> whentrue, Function<U, V> whenFalse) {
		return map(o -> condition.test(o) ? whentrue.apply(o) : whenFalse.apply(o));
	}

	/**
	 * add a consumer that follows the data inside. if there is already data, the
	 * consumer receives that data before exiting this method.
	 * <p>
	 * Implementation notes : for implementation in
	 * {@link ObjHolderSimple#follow(Consumer)}<br />
	 * This implementation stores a weak reference over the listener, if at least
	 * one holder is provided. The holders are also asked to keep a strong
	 * reference on both this and the consumer. This ensures both this, and the
	 * consumer, won't be garbage collected unless the holders already are.<br />
	 * If no holder is provided, then the strong reference is hold on this and the
	 * consumer through an external class' static fields. This will lead to memory
	 * leak, that's why providing a holder is VERY recommended. However in some
	 * cases, there is no way to do otherwise, such as printing data to a debuger,
	 * etc since those are not consumer <Object> to maintain the ref.
	 * </p>
	 * <p>
	 * For example, with holder specified, if I have a source holder over an int,
	 * a mult2 holder defined as mult2=source.mult(2), and a dest holder defined
	 * as dest=mult2.plus(1), in a direct way :
	 * <code>dest = source.mult(2).plus(1);</code> ; then it's clear that the
	 * mult2 holder should not be GC before the dest holder, since the later
	 * relies on the former. So dest should hold a strong ref to mult2, as well as
	 * a strong ref to the lambda i->i+1 . When dest is GC, then if mult2 is not
	 * used anywhere else it can be GC ; and if it's used, the lambda it stored as
	 * listener can still be GC.
	 * </p>
	 * <p>
	 * In the case no example is provided, eg
	 * <code>source.mult(2).follow(System.err::println);</code>, then there is no
	 * way to deduce when the listener and the intermediate mult2 should be GC.
	 * Maybe the code is started in another class loader, in another thread.
	 * Therefore, what we do is force intermediate object mult2 to never be GC.
	 * Any static manager would make it possible to be a total mess, therefore we
	 * kepp it simple.
	 * </p>
	 *
	 * @param listener
	 *          the listener that will receive new values.
	 * @param holder
	 *          the holder that ensure the consumer is useful. Once the holder is
	 *          no more Strong reachable, the listener may be removed from this.
	 *          If set to null, a specific class field is used to consider the
	 *          always strong reachable.
	 */
	public ObjHolder<U> follow(Consumer<U> listener, Consumer<Object> holder);

	/**
	 * add a consumer that follows the data inside. if there is already data, the
	 * consumer receives that data before exiting this method. This redirects to
	 * {@link #follow(Consumer, Consumer)} with a null holder.
	 *
	 * @param cons
	 */
	public default ObjHolder<U> follow(Consumer<U> listener) {
		return follow(listener, null);
	}

	/**
	 * remove a follower added with {@link #follow(Consumer)} or
	 * {@link #follow(Consumer, Consumer)}
	 *
	 * @param listener
	 */
	public void unfollow(Consumer<U> listener);

	/**
	 * create a new obsObjHolder that contains the transformation of the value
	 * hold in this.
	 *
	 * @param <V>
	 *          the type of the value contained
	 * @param mapper
	 *          the method to transform the value
	 * @return a new holder.
	 */
	<V> ObjHolder<V> map(Function<U, V> mapper);

	/**
	 * create a new object that mirrors the value hold in this, by transforming it
	 * into an int.
	 *
	 * @param mapper
	 *          the method to transform the value
	 * @return a new holder
	 */
	IntHolder mapInt(ToIntFunction<U> mapper);

	/**
	 * create a new object that mirrors the value hold in this, by transforming it
	 * into a long.
	 *
	 * @param mapper
	 *          the method to transform the value
	 * @return a new holder
	 */
	LongHolder mapLong(ToLongFunction<U> mapper);

	/**
	 * create a new object that mirrors the value hold in this, by transforming it
	 * into a double.
	 *
	 * @param mapper
	 *          the method to transform the value
	 * @return a new holder
	 */
	DoubleHolder mapDouble(ToDoubleFunction<U> mapper);

	/**
	 * create a new object that mirrors the value hold in this, by transforming it
	 * into a float.
	 *
	 * @param mapper
	 *          the method to transform the value
	 * @return a new holder
	 */
	FloatHolder mapFloat(ToDoubleFunction<U> mapper);

	/**
	 * create a new object that mirrors the value hold in this, by transforming it
	 * into a map.
	 *
	 * @param mapper
	 *          the method to transform the value
	 * @return a new holder
	 */
	<K, V> MapHolder<K, V> mapMap(Function<U, Map<K, V>> mapper);

	/**
	 * create a new object that mirrors the value hold in this, by transforming it
	 * into a list.
	 *
	 * @param mapper
	 *          the method to transform the value
	 * @return a new holder
	 */
	<K> ListHolder<K> mapList(Function<U, List<K>> mapper);

	/**
	 * create a new object that mirrors the value hold in this, by transforming it
	 * into a boolean.
	 *
	 * @param mapper
	 *          the method to transform the value
	 * @return a new holder
	 */
	BoolHolder test(Predicate<U> test);

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
	<V> ListHolder<V> toList(Function<U, Iterable<V>> generator);

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
	<V> SetHolder<V> toSet(Function<U, Iterable<V>> generator);

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
	<V> ObjHolder<V> unPack(Function<U, ObjHolder<V>> unpacker);

	public <V, R> ObjHolder<R> combine(ObjHolder<V> other, BiFunction<U, V, R> mapper);

	@SuppressWarnings("unchecked")
	public <V> ObjHolder<V> reduce(Function<List<? extends U>, V> reducer, ObjHolder<? extends U> first,
			ObjHolder<? extends U>... others);

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
	public static <U, V, C extends RWObjHolder<V> & Consumer<Object>> C map(ObjHolder<U> from, Supplier<C> creator,
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
	public static <AType, BType, ResType, HolderType extends RWObjHolder<ResType> & Consumer<Object>> HolderType combine(
			ObjHolder<AType> a, ObjHolder<BType> b, Supplier<HolderType> creator,
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
	public static <U, V, HolderType extends RWObjHolder<V> & Consumer<Object>> HolderType reduce(
			List<? extends ObjHolder<? extends U>> vars, Supplier<HolderType> creator,
					Function<List<? extends U>, V> reducer) {
		HolderType ret = creator.get();
		if (vars == null || vars.isEmpty()) {
			ret.set(null);
			return ret;
		}
		ObjHolder<U>[] holders = vars.toArray(ObjHolder[]::new);
		HashMap<Integer, U> received = new HashMap<>();
		for (int i = 0; i < holders.length; i++) {
			int index = i;
			ObjHolder<U> h = holders[i];
			h.follow((newValue) -> {
				synchronized (received) {
					received.put(index, newValue);
					if (received.size() == holders.length) {
						V joined = reducer
								.apply(IntStream.range(0, received.size()).mapToObj(received::get).collect(Collectors.toList()));
						ret.set(joined);
					} else {
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
	public static <U, V, H extends RWObjHolder<V> & Consumer<Object>> H unPack(ObjHolder<U> target,
			Supplier<H> creator, Function<U, ObjHolder<V>> unpacker) {
		H ret = creator.get();
		ObjHolder<V>[] storeHolder = new ObjHolder[1];
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
