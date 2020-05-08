package fr.lelouet.tools.application.settings.fieldaccess.storelocation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import fr.lelouet.tools.application.settings.fieldaccess.StoreLocation;
import fr.lelouet.tools.application.xdg.XDGApp;

public class XDGLocation implements StoreLocation {

	@Override
	public InputStream load(Class<?> cl) {
		File configFile = new XDGApp(cl.getName()).findConfigFile();
		if (configFile != null && configFile.exists() && configFile.isFile()) {
			try {
				return new FileInputStream(configFile);
			} catch (FileNotFoundException e) {
				throw new UnsupportedOperationException("catch this", e);
			}
		}
		return null;
	}

	@Override
	public OutputStream store(Class<?> cl) {
		try {
			return new FileOutputStream(new XDGApp(cl.getName()).configFile());
		} catch (FileNotFoundException e) {
			throw new UnsupportedOperationException("catch this", e);
		}
	}

}
