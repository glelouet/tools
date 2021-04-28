package fr.lelouet.tools.holders.interfaces.collections;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import fr.lelouet.tools.holders.interfaces.ObjHolder;
import fr.lelouet.tools.holders.interfaces.numbers.BoolHolder;

public interface SetHolder<U> extends CollectionHolder<U, Set<U>> {

	@Override
	default SetHolder<U> follow(Consumer<Set<U>> listener) {
		CollectionHolder.super.follow(listener);
		return this;
	}

	/**
	 * create a variable which is set to true whenever this contains a specific
	 * value
	 *
	 * @param value
	 *          a value
	 * @return a new variable
	 */
	public BoolHolder contains(U value);

	/**
	 * create a variable which is set to true whenever this contains the value
	 * hold in a variable. The returned variable is updated whenever the value
	 * variable is modified or this receives data.
	 *
	 * @param value
	 *          a value variable
	 * @return a new variable
	 */
	public BoolHolder contains(ObjHolder<U> value);

	@Override
	SetHolder<U> filter(Predicate<? super U> predicate);

	@Override
	SetHolder<U> filterWhen(Function<? super U, BoolHolder> filterer);


}
