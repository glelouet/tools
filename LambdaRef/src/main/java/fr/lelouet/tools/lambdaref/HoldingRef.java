package fr.lelouet.tools.lambdaref;

import java.lang.ref.WeakReference;

/**
 * A Reference to an object, that holds on this object until another object,
 * called the holder, is no more weak reachable. This is typically used to store
 * listeners that are lambdas, as a lambda is weak referenced as soon as the
 * method it was created in is exited.
 * <p>
 * for example if I have a listenable of Integer li, so I can register a
 * listener, which will print the integer : <br />
 * <pre>Listenable<Integer> li;
 *li.addListener(i->System.out.println("received "+i);</pre> If the listenable
 * stores the lambda under a weak reference, then the weak reference will return
 * null as soon as the method in which that code is will be exited(or even when
 * the block is exited). <br />
 * In that case it's not useful to store the listener under a weak reference, it
 * becomes interesting when a listenable is listeneed to by several objects
 * which may be destroyed. Example if I have several clients who connects, and
 * want to listen to a listenable.<br />
 * <pre>for( Client cl : clients) li.addListener(i->cl.sendData("received "+i));</pre><br/>
 * When the clients become disconnected, they should be removed, which means
 * they should be stored as weak references to avoid taking memory. But storing
 * the lambda as weak ref means it will be GC as soon as the function exits, so
 * the client will never receive new data.
 * </p>
 *
 * @param <T>
 *          type of the object hold
 */
public class HoldingRef<T> {

	WeakReference<Object> holderRef;

	T referent;

	public HoldingRef(T ref, Object holder) {

		this.holderRef = new WeakReference<>(holder);
		this.referent = ref;
	}

	public T get() {
		if (referent == null) {
			return null;
		}
		if (holderRef.get() == null) {
			referent = null;
		}
		return referent;
	}

}
