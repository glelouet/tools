package fr.lelouet.tools.application.settings;

/**
 * a class implementing this interface should have an empty constructor, in
 * order to be loaded and instantiated by the classLoader dynamically.
 */
public interface SettingsLoader {

	public SettingsDescription load();

}
