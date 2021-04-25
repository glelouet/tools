package fr.lelouet.collectionholders.interfaces.collections;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import fr.lelouet.collectionholders.interfaces.ObjHolder;
import fr.lelouet.collectionholders.interfaces.numbers.BoolHolder;

/**
 * holder on an observable list.
 *
 */
public interface ListHolder<U> extends CollectionHolder<U, List<U>> {

	@Override
	default ListHolder<U> follow(Consumer<List<U>> listener) {
		CollectionHolder.super.follow(listener);
		return this;
	}

	/**
	 * map the list to a specific index, or a default value in case the list is
	 * too small.
	 *
	 * @param position
	 *          index in the list if positive, from the end if negative.
	 * @param oob
	 *          default value to return when the list size is too small.
	 * @return a new holder linked to the item at the given position.
	 */
	public default ObjHolder<U> pos(int position, U oob) {
		return map(
				l -> position < l.size() && -position <= l.size() ? l.get(position >= 0 ? position : l.size() - 1 - position)
						: oob);
	}

	/**
	 *
	 * @return a cached list containing the reverse order of this one. If the list
	 *         does not already exist, creates a new one whose reverse is this
	 */
	ListHolder<U> reverse();

	@Override
	ListHolder<U> filter(Predicate<? super U> predicate);

	@Override
	ListHolder<U> filterWhen(Function<? super U, BoolHolder> filterer);

	ListHolder<U> concat(ListHolder<? extends U> first,
			@SuppressWarnings("unchecked") ListHolder<? extends U>... lists);

	@Override
	<K> ListHolder<K> mapItems(Function<U, K> mapper);

}
