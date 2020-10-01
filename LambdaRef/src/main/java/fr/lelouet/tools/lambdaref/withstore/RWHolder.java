package fr.lelouet.tools.lambdaref.withstore;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import fr.lelouet.tools.lambdaref.withstore.references.IRef;
import fr.lelouet.tools.lambdaref.withstore.references.UsualRef;
import fr.lelouet.tools.lambdaref.withstore.references.WeakRef;

public class RWHolder<U> implements WeakListener<U>, Consumer<Object> {

	private U value;

	private transient LinkedList<Object> stored = new LinkedList<>();

	@Override
	public void accept(Object o) {
		stored.add(o);
	}

	public U get() {
		return value;
	}

	public void set(U val) {
		value = val;
		for (Iterator<IRef<Consumer<U>>> it = listeners.iterator(); it.hasNext();) {
			Consumer<U> call = it.next().get();
			if (call == null) {
				it.remove();
			} else {
				call.accept(val);
			}
		}
	}

	private List<IRef<Consumer<U>>> listeners = new ArrayList<>();

	public int listeners() {
		return listeners.size();
	}

	@Override
	public void listen(Consumer<U> listener, Consumer<Object> holder) {
		holder.accept(listener);
		listeners.add(new WeakRef<>(listener));
	}

	@Override
	public void listen(Consumer<U> listener) {
		listeners.add(new UsualRef<>(listener));
	}

	public <V> RWHolder<V> map(Function<U, V> mapper) {
		RWHolder<V> ret = new RWHolder<>();
		listen(value -> ret.set(mapper.apply(value)));
		return ret;
	}

}
