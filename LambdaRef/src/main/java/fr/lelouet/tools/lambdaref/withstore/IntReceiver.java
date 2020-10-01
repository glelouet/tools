package fr.lelouet.tools.lambdaref.withstore;

import java.util.LinkedList;
import java.util.function.Consumer;

public class IntReceiver implements Consumer<Object> {

	private int value;

	private transient LinkedList<Object> stored = new LinkedList<>();

	@Override
	public void accept(Object o) {
		synchronized (stored) {
			stored.add(o);
		}
	}

	public void set(int val) {
		value = val;
	}

	public int get() {
		return value;
	}

}