package fr.lelouet.tools.application.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

import org.yaml.snakeyaml.Yaml;

import fr.lelouet.tools.application.yaml.CleanRepresenter;

public class SettingsDescription {

	/**
	 * name of the application. This will lead to the name of the settings to
	 * represent it.
	 */
	public String name;

	/**
	 * package of the application
	 */
	public String path;

	public HashMap<String, TypeDescription> types;

	public HashMap<String, TypeDescription> contains;

	public static SettingsDescription load(File f) throws FileNotFoundException {
		return load(new FileInputStream(f));
	}

	public static SettingsDescription load(InputStream is) {
		CleanRepresenter repr = new CleanRepresenter();
		repr.getPropertyUtils().setSkipMissingProperties(true);
		Yaml yaml = new Yaml(repr);
		return yaml.loadAs(is, SettingsDescription.class);
	}

}
