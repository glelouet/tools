package fr.lelouet.tools.lambdaref.withstore;

import java.util.function.Consumer;

public interface WeakListener<T> {

	public void listen(Consumer<T> call, Consumer<Object> holder);

	public void listen(Consumer<T> call);

}
