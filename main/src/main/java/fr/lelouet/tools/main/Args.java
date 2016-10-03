package fr.lelouet.tools.main;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class Args {

	/**
	 * contains the list of targets and the properties deduced from a list of
	 * arguments
	 */
	public static class KeyValArgs {
		/** the argument with no leading dash, no equals */
		public final List<String> targets = new ArrayList<String>();

		/** the (key, val) elements */
		public final Properties props = new Properties();

		public String getRequiredProperty(String key) {
			return Args.getRequiredProperty(props, key);
		}
	}

	protected static void addProp(Properties props, String key, String val) {
		props.setProperty(key, val);
	}

	protected static enum ParamTypes {
		key, keyval, val;
	}

	protected static ParamTypes findType(String param) {
		if (param.contains("=")) {
			return ParamTypes.keyval;
		}
		if (param.startsWith("-")) {
			return ParamTypes.key;
		}
		return ParamTypes.val;
	}

	protected static String removePrefix(String param) {
		if (param.startsWith("-") && param.length() > 0) {
			param = param.substring(1);
			if (param.startsWith("-") && param.length() > 0) {
				param = param.substring(1);
			}
		}
		return param;
	}

	/**
	 * parse an array of Strings, and sort them in (key->val) couples and (val)
	 * elements
	 * <ul>
	 * <li>args with an equal ( eg, daemon=true or port=42 ) are considered
	 * key=val. leading up to two dashes are ommitted (ie, port=42 is the same
	 * as -port=42 or --port=42, but not ---port=42 )</li>
	 * <li>args with a starting dash ( eg, -start ) cannot be a val of a key.
	 * ie, -start -debug means two keys : -start and -debug</li>
	 * <li>args with a starting dash are considered as key if the next argument
	 * can be a val, thus creating the (key, val) couple</li>
	 * <li>args starting with a dash but with next arg being a key are assigned
	 * the "TRUE" val</li>
	 * <li>args not being key nor value are targets</li>
	 * </ul>
	 */
	public static KeyValArgs getArgs(String[] args) {
		KeyValArgs ret = new KeyValArgs();
		List<String> targets = ret.targets;
		Properties props = ret.props;
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			String nextArg = null;
			if (i + 1 < args.length) {
				nextArg = args[i + 1];
			}
			ParamTypes type = findType(arg);
			switch (type) {
			case keyval:
				int pos = arg.indexOf("=");
				addProp(props, removePrefix(arg.substring(0, pos)),
						arg.substring(pos + 1));
				break;
			case key:
				if (nextArg != null && findType(nextArg) == ParamTypes.val) {
					addProp(props, removePrefix(arg), nextArg);
				} else {
					addProp(props, removePrefix(arg), Boolean.TRUE.toString());
				}
				break;
			case val:
				targets.add(arg);
				break;
			default:
				throw new UnsupportedOperationException(
						"case not implemented here : " + type);
			}
		}
		return ret;
	}

	/**
	 * get the property from a {@link Properties} or throws an exception
	 * 
	 * @see {@link #getRequiredProperty(Map, String)}
	 */
	public static String getRequiredProperty(Properties prop, String key)
			throws IllegalArgumentException {
		if (!prop.containsKey(key)) {
			throw new IllegalArgumentException("the properties " + prop
					+ " does not contain the required key " + key);
		}
		return prop.getProperty(key);

	}

	/**
	 * get the property from a {@link Map} or throws an exception
	 * 
	 * @param prop
	 *            the {@link MAp} we want to find the property into
	 * @param key
	 *            the name of the property we require
	 * @return the value associated with that name in the {@link Map}
	 * @throws IllegalArgumentException
	 *             if the Properties does not contain that key
	 */
	public static String getRequiredProperty(Map<String, String> prop,
			String key) throws IllegalArgumentException {
		if (!prop.containsKey(key)) {
			throw new IllegalArgumentException("the properties " + prop
					+ " does not contain the required key " + key);
		}
		return prop.get(key);

	}

}
