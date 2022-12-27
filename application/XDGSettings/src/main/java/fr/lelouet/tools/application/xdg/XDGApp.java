package fr.lelouet.tools.application.xdg;

import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * applications files following XDG spec
 *
 * @see https://specifications.freedesktop.org/basedir-spec/basedir-spec-latest.html
 */
public class XDGApp {

	public final String appName;
	private final Map<String, String> properties;

	/**
	 * called for tests only
	 *
	 * @param appName
	 * @param env
	 *          the properties to override system.getenv . This allow to test
	 *          reaction.
	 */
	public XDGApp(String appName, Map<String, String> env) {
		this.appName = appName;
		properties = env;
	}

	/**
	 * create a new application representation
	 *
	 * @param appName
	 *          the name fo this app
	 */
	public XDGApp(String appName) {
		this(appName, System.getenv());

	}

	//
	// data dir
	//


	protected static final String XDG_DATA_HOME_KEY = "XDG_DATA_HOME";
	protected static final String XDG_DATA_HOME_DEFAULT = ".local/share";
	protected static final String XDG_DATA_DIRS_KEY = "XDG_DATA_DIRS";
	protected static final String XDG_DATA_DIRS_DEFAULT = "/usr/local/share/:/usr/share/";

	/**
	 * find an existing data file for this app with given sub path
	 *
	 * @param subPath
	 * @return an existing file, or null
	 */
	public File findDataFile(String... subPath) {
		return findFile(XDG_DATA_HOME_KEY, getHome() + File.separator + XDG_DATA_HOME_DEFAULT, XDG_DATA_DIRS_KEY,
				XDG_DATA_DIRS_DEFAULT, subPath);
	}

	/**
	 * return a file where the given sub path SHOULD be stored.
	 *
	 * @param subPaths
	 *          sub path into application specific data folders, eg
	 *          ["option","interface","shortcuts.ini"]
	 * @return a new File. The result does not depend on the existence of such a
	 *         file, it may as well return to an absurd path if badly configured.
	 */
	public File dataFile(String... subPaths) {
		return makeFile(XDG_DATA_HOME_KEY, getHome() + File.separator + XDG_DATA_HOME_DEFAULT, subPaths);
	}

	//
	// config dir
	//

	protected static final String XDG_CONFIG_HOME_KEY = "XDG_CONFIG_HOME";
	protected static final String XDG_CONFIG_HOME_DEFAULT = ".config";
	protected static final String XDG_CONFIG_DIRS_KEY = "XDG_CONFIG_DIRS";
	protected static final String XDG_CONFIG_DIRS_DEFAULT = "/etc/xdg";

	public File findConfigFile(String... subPath) {
		return findFile(XDG_CONFIG_HOME_KEY, getHome() + File.separator + XDG_CONFIG_HOME_DEFAULT, XDG_CONFIG_DIRS_KEY,
				XDG_CONFIG_DIRS_DEFAULT, subPath);
	}

	/**
	 * return a file where the given sub path SHOULD be stored.
	 *
	 * @param subPaths
	 *          sub path into application specific config folders, eg
	 *          ["option","interface","shortcuts.ini"]
	 * @return a new File. The result does not depend on the existence of such a
	 *         file, it may as well return to an absurd path if badly configured.
	 */
	public File configFile(String... subPaths) {
		return makeFile(XDG_CONFIG_HOME_KEY, getHome() + File.separator + XDG_CONFIG_HOME_DEFAULT, subPaths);
	}

	//
	// cache
	//

	protected static final String XDG_CACHE_HOME_KEY = "XDG_CACHE_HOME";
	protected static final String XDG_CACHE_HOME_DEFAULT = ".cache";

	/**
	 * find the cache file for this app
	 *
	 * @param subPaths
	 *          path of the cache file relative to that app, eg
	 *          ["img","sun","1215.png"] would find img/sun/1215.png
	 * @return a new file relating to given path in the cache directory of that
	 *         app.
	 */
	public File cacheFile(String... subPaths) {
		return makeFile(XDG_CACHE_HOME_KEY, getHome() + File.separator + XDG_CACHE_HOME_DEFAULT, subPaths);
	}

	//
	// runtime dir
	//

	protected static final String XDG_RUNTIME_DIR_KEY = "XDG_RUNTIME_DIR";

	/**
	 * get the runtime dir. If possible find the one set to
	 * {@value #XDG_RUNTIME_DIR_KEY} , if not set try to use /run/usr/$USER, or
	 * /run/usr/$UID , returning the first one that exists and is a dir. Otherwise
	 * return null
	 *
	 * @return
	 */
	public File runtimeDir() {
		String target = properties.get(XDG_RUNTIME_DIR_KEY);
		if (target != null) {
			return new File(target, appName);
		}
		String user = properties.get("USER");
		if (user != null) {
			target = File.separator + "run" + File.separator + "usr" + File.separator + user;
			File ret = new File(target, appName);
			if (ret.exists() && ret.isDirectory()) {
				return ret;
			}
		}
		String uid = properties.get("UID");
		if (uid != null) {
			target = File.separator + "run" + File.separator + "usr" + File.separator + uid;
			File ret = new File(target, appName);
			if (ret.exists() && ret.isDirectory()) {
				return ret;
			}
		}
		return null;
	}

	//
	// shared methods
	//

	protected String getHome() {
		return properties.getOrDefault("HOME", "./");
	}

	/**
	 * find a file available.
	 * <p>
	 * the file must be in appName/subPaths[0]/subPaths[1]/.. in one of the
	 * following folders :
	 * <ol>
	 * <li>if home_key is set, search in the directory for this value</li>
	 * <li>if home_key is not set, search in home_default</li>
	 * <li>if dir_key is set, split it by semicolon ":" and search in this
	 * directory list</li>
	 * <li>if dir_key is not set, split dir_default by semicolon ":" and search in
	 * this directory list</li>
	 * <li>if no matching file, return null</li>
	 * </ol>
	 * </p>
	 *
	 * @param home_key
	 *          they key for system properties specifying the home
	 * @param home_default
	 *          the value to use if system properties does not specify home_key
	 * @param dir_key
	 *          they key for system properties specifying the dirs
	 * @param dir_default
	 *          the default value to use if system properties does not specify
	 *          dir_key
	 * @param appName
	 *          the name of the app to search in the potential folders
	 * @param subPaths
	 *          the list of subdirs to search for the file in
	 * @return an existing file, or null
	 */
	protected File findFile(String home_key, String home_default, String dir_key, String dir_default,
			String... subPaths) {
		return streamPossibleFile(home_key, home_default, dir_key, dir_default, subPaths).parallel()
				.map(p -> new File(p))
				.filter(File::exists).findFirst().orElse(null);
	}

	protected File makeFile(String home_key, String home_default, String... subPaths) {
		String beginPath = properties.getOrDefault(home_key, home_default);
		String endpath = null;
		if (subPaths != null) {
			endpath = Stream.concat(Stream.of(appName), Stream.of(subPaths)).filter(str -> str != null && str.length() > 0)
					.collect(Collectors.joining(File.separator));
		} else {
			endpath = appName == null ? "" : appName;
		}
		return new File(beginPath + File.separator + endpath);
	}

	/**
	 * stream the possible file names that can be used to find a file.
	 *
	 * @param home_key the key of the home property, must not be null
	 * @param home_default the default value of the home property, must not be null
	 * @param dir_key
	 * @param dir_default
	 * @param appName
	 * @param subPaths
	 * @return
	 */
	protected Stream<String> streamPossibleFile(String home_key, String home_default, String dir_key, String dir_default,
			String... subPaths) {
		if (home_key == null || home_default == null || dir_key == null || dir_default == null) {
			throw new NullPointerException(Arrays.asList(home_key, home_default, dir_key, dir_default) + "");
		}
		String endpath = null;
		if (subPaths != null) {
			endpath = Stream.concat(Stream.of(appName), Stream.of(subPaths)).filter(str -> str != null && str.length() > 0)
					.collect(Collectors.joining(File.separator));
		} else {
			endpath = appName == null ? "" : appName;
		}
		String endpathFinal = endpath;
		String dirs = properties.getOrDefault(dir_key, dir_default);
		String homes = properties.getOrDefault(home_key, home_default);
		return Stream.concat(
				Stream.of(homes.split(":")),
				Stream.of(dirs.split(":")))
				.map(str -> str + File.separator + endpathFinal)
		;
	}

}
