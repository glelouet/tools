/**
 * 
 */
package fr.lelouet.tools.containers;

/**
 * container that waits till one item has been set. All calls to get() will be
 * blocked untill this moment.
 * 
 * @author Guillaume Le Louët
 * 
 */
public class DelayingContainer<E> extends Container<E> {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(DelayingContainer.class);

	/** set to true when receiving an element */
	boolean received = false;

	@Override
	public void onReplace(E before, E after) {
		super.onReplace(before, after);
		synchronized (this) {
			received = true;
			this.notifyAll();
		}
	}

	@Override
	public void beforeGet(E accessed) {
		synchronized (this) {
			while (!received) {
				try {
					wait();
				} catch (InterruptedException e) {
					logger.warn("while waiting for item", e);
				}
			}
		}
		super.beforeGet(accessed);
	}

	public boolean contains() {
		return received;
	}

	public void reset() {
		synchronized (this) {
			received = false;
		}
	}
}
