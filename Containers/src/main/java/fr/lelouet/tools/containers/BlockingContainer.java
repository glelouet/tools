package fr.lelouet.tools.containers;

import fr.lelouet.tools.synchronization.Starter;

/**
 * a container for which each call to {@link #get()} will result in the calling
 * thread to be stopped till a new value is {@link #set(Object)}
 */
public class BlockingContainer<E> extends Container<E> {

	Starter sync = new Starter();

	@Override
	public void onReplace(E before, E after) {
		super.onReplace(before, after);
		sync.start();
	};

	@Override
	public void beforeGet(E accessed) {
		sync.waitForStart();
		super.beforeGet(accessed);
	};

	/** wait till enough threads are requiring the data */
	public void waitFor(int nbWaitingThreads) {
		sync.waitFor(nbWaitingThreads);
	}

}
