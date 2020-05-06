package fr.lelouet.tools.application.settings;

import java.io.InputStream;

/**
 * converts a Class into an {@link InputStream} , and an {@link InputStream}
 * into a class.
 */
public interface StoreFormat {

	public <T> T load(InputStream is, Class<T> clazz);

	public <T> InputStream store(T object);

}
