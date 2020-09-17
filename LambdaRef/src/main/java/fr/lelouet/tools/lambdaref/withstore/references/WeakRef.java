package fr.lelouet.tools.lambdaref.withstore.references;

import java.lang.ref.WeakReference;

public class WeakRef<U> extends WeakReference<U> implements IRef<U> {

	public WeakRef(U referent) {
		super(referent);
	}
}