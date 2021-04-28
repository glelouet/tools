package fr.lelouet.tools.holders.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import fr.lelouet.tools.holders.impl.collections.ListHolderImpl;
import fr.lelouet.tools.holders.impl.collections.MapHolderImpl;
import fr.lelouet.tools.holders.impl.collections.SetHolderImpl;
import fr.lelouet.tools.holders.impl.numbers.BoolHolderImpl;
import fr.lelouet.tools.holders.impl.numbers.DoubleHolderImpl;
import fr.lelouet.tools.holders.impl.numbers.FloatHolderImpl;
import fr.lelouet.tools.holders.impl.numbers.IntHolderImpl;
import fr.lelouet.tools.holders.impl.numbers.LongHolderImpl;
import fr.lelouet.tools.holders.interfaces.ObjHolder;
import fr.lelouet.tools.holders.interfaces.collections.ListHolder;
import fr.lelouet.tools.holders.interfaces.collections.MapHolder;
import fr.lelouet.tools.holders.interfaces.collections.SetHolder;
import fr.lelouet.tools.holders.interfaces.numbers.BoolHolder;
import fr.lelouet.tools.holders.interfaces.numbers.DoubleHolder;
import fr.lelouet.tools.holders.interfaces.numbers.FloatHolder;
import fr.lelouet.tools.holders.interfaces.numbers.IntHolder;
import fr.lelouet.tools.holders.interfaces.numbers.LongHolder;

/**
 * basic abstract methods that do not depend on the implementation.
 *
 * @param <U>
 */
public abstract class AObjHolder<U> implements ObjHolder<U> {

	@Override
	public <V> ObjHolder<V> map(Function<U, V> mapper) {
		ObjHolderSimple<V> ret = new ObjHolderSimple<>();
		follow(v -> ret.set(mapper.apply(v)), ret);
		return ret;
	}

	@Override
	public BoolHolder test(Predicate<U> test) {
		BoolHolderImpl ret = new BoolHolderImpl();
		follow((newValue) -> ret.set(test.test(newValue)), ret);
		return ret;
	}

	@Override
	public IntHolder mapInt(ToIntFunction<U> mapper) {
		IntHolderImpl ret = new IntHolderImpl();
		follow(newValue -> ret.set(mapper.applyAsInt(newValue)), ret);
		return ret;
	}

	@Override
	public LongHolder mapLong(ToLongFunction<U> mapper) {
		LongHolderImpl ret = new LongHolderImpl();
		follow((newValue) -> ret.set(mapper.applyAsLong(newValue)), ret);
		return ret;
	}

	@Override
	public FloatHolder mapFloat(ToDoubleFunction<U> mapper) {
		FloatHolderImpl ret = new FloatHolderImpl();
		follow((newValue) -> ret.set((float) mapper.applyAsDouble(newValue)), ret);
		return ret;
	}

	@Override
	public DoubleHolder mapDouble(ToDoubleFunction<U> mapper) {
		DoubleHolderImpl ret = new DoubleHolderImpl();
		follow((newValue) -> ret.set(mapper.applyAsDouble(newValue)), ret);
		return ret;
	}

	@Override
	public <K> ListHolder<K> mapList(Function<U, List<K>> mapper) {
		ListHolderImpl<K> ret = new ListHolderImpl<>();
		follow((newValue) -> {
			List<K> newlist = mapper.apply(newValue);
			ret.set(newlist);
		}, ret);
		return ret;
	}

	@Override
	public <K, V> MapHolder<K, V> mapMap(Function<U, Map<K, V>> mapper) {
		MapHolderImpl<K, V> ret = new MapHolderImpl<>();
		follow((newValue) -> {
			Map<K, V> newlist = mapper.apply(newValue);
			ret.set(newlist);
		}, ret);
		return ret;
	}

	@Override
	public <V> ListHolder<V> toList(Function<U, Iterable<V>> generator) {
		ListHolderImpl<V> ret = new ListHolderImpl<>();
		follow((newValue) -> {
			List<V> newlist = StreamSupport.stream(generator.apply(newValue).spliterator(), false)
					.collect(Collectors.toList());
			ret.set(newlist);
		}, ret);
		return ret;
	}

	@Override
	public <V> SetHolder<V> toSet(Function<U, Iterable<V>> generator) {
		SetHolderImpl<V> ret = new SetHolderImpl<>();
		follow((newValue) -> {
			Set<V> newlist = StreamSupport.stream(generator.apply(newValue).spliterator(), false).collect(Collectors.toSet());
			ret.set(newlist);
		}, ret);
		return ret;
	}

	@Override
	public <V, R> ObjHolder<R> combine(ObjHolder<V> other, BiFunction<U, V, R> mapper) {
		return ObjHolder.combine(this, other, ObjHolderSimple::new, mapper);
	}

	@Override
	public <V> ObjHolder<V> unPack(Function<U, ObjHolder<V>> unpacker) {
		return ObjHolder.unPack(this, ObjHolderSimple::new, unpacker);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V> ObjHolderSimple<V> reduce(Function<List<? extends U>, V> reducer,
			ObjHolder<? extends U> first, ObjHolder<? extends U>... others) {
		List<ObjHolder<? extends U>> list = Stream
				.concat(Stream.of(this, first), others == null ? Stream.empty() : Stream.of(others))
				.collect(Collectors.toList());
		return ObjHolder.reduce(list, ObjHolderSimple<V>::new, reducer);
	}

}
