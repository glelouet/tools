package fr.lelouet.tools.settings.yaml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import fr.lelouet.tools.settings.ASettings;
import fr.lelouet.tools.settings.xdg.XDGApp;

public class YamlSettings extends ASettings {

	public final XDGApp appStorage;

	public YamlSettings() {
		appStorage = new XDGApp(getAppName());
	}

	/**
	 * load stored settings if exists, or default settings
	 *
	 * @return
	 * @throws FileNotFoundException
	 */
	public static <T extends YamlSettings> T load(Class<T> clazz) {
		T inst = null;
		try {
			inst = clazz.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e1) {
			throw new UnsupportedOperationException("catch this", e1);
		}
		File f = inst.findStoreFile();
		if (f != null && f.exists()) {
			try {
				T newInst = inst.makeLoadYaml().loadAs(new FileReader(f), clazz);
				if (newInst != null) {
					inst = newInst;
				}
			} catch (FileNotFoundException e) {
				throw new UnsupportedOperationException("catch this", e);
			}
		} else {
			logger.warn("can't load settings for " + inst.getAppName() + " from " + f);
		}
		return inst;
	}

	/**
	 * store this settings locally, overriding previous stored settings
	 */
	@Override
	public void store() {
		File f = getStoreFile();
		f.getParentFile().mkdirs();
		try {
			makeDumpYaml().dump(this, new FileWriter(f));
		} catch (IOException e) {
			throw new UnsupportedOperationException("catch this", e);
		}
	}

	@Override
	public File getStoreFile() {
		File ret = appStorage.configFile("settings.yml");
		return ret;
	}

	/**
	 * xdg spec : the config for an app can be in several places. While we can
	 * write only in one place, we can read from several potential files.
	 *
	 * @return the first available file to read config from.
	 */
	public File findStoreFile() {
		return appStorage.findConfigFile("settings.yml");
	}

}
