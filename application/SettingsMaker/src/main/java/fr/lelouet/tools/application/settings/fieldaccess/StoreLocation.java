package fr.lelouet.tools.application.settings.fieldaccess;

import java.io.InputStream;
import java.io.OutputStream;

/** get the inputstream to load a settings, and the outputstream to store it */
public interface StoreLocation {

	public InputStream load(Class<?> cl);

	public OutputStream store(Class<?> cl);

}
