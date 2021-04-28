package fr.lelouet.tools.lambdaref.references;

import java.util.IdentityHashMap;

/**
 * a reference that is hard linked by the classloader (trough a static field)
 * until it is explicitly disposed.
 * <p>
 * Note that this is the definition of a memory leak. Therefore this should
 * never be used out of a managed loop that ensures the disposal of the
 * references.
 * </p>
 * <p>
 * This can be useful though, when the Object is stored in a weak referenced
 * loop : since the loop may still need the item, this item may be GC, resulting
 * in NPE.
 * </p>
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
