package fr.lelouet.tools.application.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import fr.lelouet.tools.application.settings.beanmakers.PublicField;
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

	/**
	 * bean maker for fields in a class. can be one of the default classes, eg
	 * field for {@link PublicField}, or a selected class that implements
	 * {@link BeanMaker}
	 */
	public String bean;

	/**
	 * if true, all types will be generated, even those that are not reachable
	 * from the contains
	 */
	public boolean forceAllTypes = false;

	public Map<String, TypeDescription> types = new LinkedHashMap<>();

	public Map<String, TypeDescription> contains = new LinkedHashMap<>();

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
