package fr.lelouet.tools.lambdaref.withstore;

import java.util.LinkedList;

public class IntReceiver implements RefStore {

	private int value;

	private transient LinkedList<Object> stored = new LinkedList<>();

	@Override
	public void store(Object o) {
		stored.add(o);
	}

	public void set(int val) {
		value = val;
	}

	public int get() {
		return value;
	}

}