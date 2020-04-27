package fr.lelouet.tools.lambdaref;

import java.lang.ref.WeakReference;

/**
 * A Reference to an object, that reaches strongly this object until the holder
 * is weak referenced. This is typically used to store lambda listeners, as
 * those are weak referenced as soon as the method they were created in is
 * exited.
 *
 * @param <T>
 */
public class LambdaRef<T> {

	WeakReference<Object> holderref;

	T lambda;

	public LambdaRef(T lambda, Object holder) {
		this.holderref = new WeakReference<>(holder);
	}

	public T get() {
		if (lambda == null || holderref.get() == null) {
			lambda = null;
		}
		return lambda;
	}

}
