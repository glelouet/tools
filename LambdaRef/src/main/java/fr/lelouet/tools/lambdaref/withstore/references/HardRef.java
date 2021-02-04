package fr.lelouet.tools.lambdaref.withstore.references;

import java.util.IdentityHashMap;

/**
 * a reference that is hard linked by the classloader (trough a static field)
 * until it is explicitly disposed.
 *
 * @param <U>
 */
public class HardRef<U> extends UsualRef<U> {

	private static IdentityHashMap<HardRef<?>, Object> items = new IdentityHashMap<>();

	public HardRef(U referent) {
		super(referent);
		items.put(this, null);
	}

	public void dispose() {
		items.remove(this);
	}

}
