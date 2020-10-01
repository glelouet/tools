package fr.lelouet.tools.lambdaref.withstore;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import fr.lelouet.tools.lambdaref.withstore.references.IRef;
import fr.lelouet.tools.lambdaref.withstore.references.UsualRef;
import fr.lelouet.tools.lambdaref.withstore.references.WeakRef;

public class StringHolder implements WeakListener<String> {

	private List<IRef<Consumer<String>>> listeners = new ArrayList<>();

	public int listeners() {
		return listeners.size();
	}

	@Override
	public void listen(Consumer<String> listener, Consumer<Object> holder) {
		holder.accept(listener);
		listeners.add(new WeakRef<>(listener));
	}

	@Override
	public void listen(Consumer<String> listener) {
		listeners.add(new UsualRef<>(listener));
	}

	public void set(String data) {
		for (Iterator<IRef<Consumer<String>>> it = listeners.iterator(); it.hasNext();) {
			Consumer<String> call = it.next().get();
			if (call == null) {
				it.remove();
			} else {
				call.accept(data);
			}
		}
	}

}