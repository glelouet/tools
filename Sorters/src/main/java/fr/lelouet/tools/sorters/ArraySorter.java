package fr.lelouet.tools.sorters;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Sort elements that can be projected on a bounded range of IN, such as ages,
 * dates, weights. The projection is done with an {@link IntEvaluator evaluator}
 * <br />
 * The principle is to store items, indexed by their projection on IN, then to
 * get the sorted list by parsing the storage.
 * </p>
 * <h2>Implementation</h2>
 * <p>
 * Contains an array of arrays of arraylists, and an evaluator.<br />
 * When an element is added, it is evaluated and then stored in the correct
 * arraylist. <br />
 * Since this is supposed to have very few elements compared to the range size,
 * most arraylists are null.<br />
 * </p>
 * <p>
 * to optimize size and reading, the arralylists are not directly indexed, but
 * indexed in subarrays, expecting most of the subarrays to be empty they are
 * null.
 * </p>
 */
public class ArraySorter<E> {

	/** makes the relation between the elements and an range of int */
	public static interface IntEvaluator<E> {

		/**
		 * evaluate an element. The value must always be between
		 * {@link #minval()} and {@link #maxval()}, included
		 */
		int eval(E element);

		/** the lowest value an element can be evaluated. Must be constant. */
		int minval();

		/** the highest value an element can be evaluated. Must be constant */
		int maxval();

	}

	protected static class SubArray<E> {
		private final ArrayList<E>[] array;

		@SuppressWarnings("unchecked")
		public SubArray(int size) {
			array = new ArrayList[size];
		}

		public boolean add(E element, int pos) {
			try {
				return array[pos].add(element);
			} catch (NullPointerException npe) {
				array[pos] = new ArrayList<E>();
				return array[pos].add(element);
			}
		}

		public void addTo(List<E> l) {
			for (ArrayList<E> element : array) {
				if (element != null) {
					l.addAll(element);
				}
			}
		}
	}

	private final int subArraySize;
	private final IntEvaluator<E> evaluator;
	private final SubArray<E>[] array;

	protected int size = 0;

	public int size() {
		return size;
	}

	@SuppressWarnings("unchecked")
	public ArraySorter(IntEvaluator<E> eval) {
		this.evaluator = eval;
		int range = evaluator.maxval() - evaluator.minval() + 1;
		subArraySize = (int) Math.floor(Math.sqrt(range));
		int nbSubarray = (int) Math.ceil((double) range / subArraySize);
		array = new SubArray[nbSubarray];
	}

	/**
	 * add an element to the sorted
	 * 
	 * @param elem
	 *            the element to add
	 * @return this to chain addings
	 */
	public ArraySorter<E> add(E elem) {
		int offset = evaluator.eval(elem) - evaluator.minval();
		int subpos = offset / subArraySize;
		int suboff = offset % subArraySize;
		SubArray<E> subarray = array[subpos];
		if (subarray == null) {
			subarray = new SubArray<E>(subArraySize);
			array[subpos] = subarray;
		}
		subarray.add(elem, suboff);
		size++;
		return this;
	}

	/**
	 * compresses this to a list of sorted items<br />
	 * 
	 * @return a snapshot of the present items
	 */
	public ArrayList<E> toList() {
		ArrayList<E> ret = new ArrayList<E>(size());
		for (SubArray<E> element : array) {
			if (element != null) {
				element.addTo(ret);
			}
		}
		return ret;
	}

}
