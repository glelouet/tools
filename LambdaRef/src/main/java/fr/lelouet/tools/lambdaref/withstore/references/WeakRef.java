package fr.lelouet.tools.lambdaref.withstore.references;

import java.lang.ref.WeakReference;

/**
 * reference that does not keep a strong reference on the item refered.
 *
 * @author
 *
 * @param <U>
 */
public class WeakRef<U> extends WeakReference<U> implements IRef<U> {

	public WeakRef(U referent) {
		super(referent);
	}
}