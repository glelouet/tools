package fr.lelouet.tools.containers;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * a container that memorizes added elements in a list.
 * <p>
 * The list can be iterated over once, using {@link #hasVal()} and {@link #popNextVal()}
 * </p>
 * <p>
 * the {@link #read()} method allows blocking wait, till the data is set. like a
 * {@link #popNextVal()} after some data has been added
 * </p>
 */
public class ListContainer<E> extends Container<E> {

    private static final Logger logger = LoggerFactory
            .getLogger(ListContainer.class);

    List<E> data = new LinkedList<E>();

    public ListContainer() {
        super();
    }

    /**
     * creates a container with a startup element. This element is not
     * retrievedthrough {@link #read()}, as it does not replace an old value.
     */
    public ListContainer(E element) {
        super(element);
    }

    protected Semaphore waitingSemaphore = new Semaphore(0);

    @Override
    public void onReplace(E before, E after) {
        super.onReplace(before, after);
        synchronized (data) {
            data.add(after);
        }
        waitingSemaphore.release();
    }

    /**
     * Blocks until a new value is received and removes it from the list.
     * <p>
     * It is not synchronized for simultaneous multiple read, but it is synched
     * for one read and several sets
     * </p>
     * 
     * @return the first value received since last {@link #read()}. If there was
     *         no valued {@link #set(Object)} since last read, wait for a value
     *         to be set.
     */
    public E read() {
        E ret = null;
        while (ret == null) {
            try {
                waitingSemaphore.acquire();
                ret = popNextVal();
            } catch (InterruptedException e) {
                logger.warn("", e);
            }
        }
        return ret;
    }

    /**
     * check if there is a new String received. Used for loop in non blocking
     * operations
     */
    public boolean hasVal() {
        return !data.isEmpty();
    }

    /**
     * removes the next fifo String received.
     * 
     * @throws IndexOutOfBoundsException if there is a bug in this.
     */
    public E popNextVal() {
        synchronized (data) {
            E ret = data.remove(0);
            return ret;
        }
    }

}
