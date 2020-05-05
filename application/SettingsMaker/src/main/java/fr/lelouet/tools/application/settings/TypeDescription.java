package fr.lelouet.tools.application.settings;

import java.util.HashMap;

/**
 * A type of a settings. Can be either a ref to an existing type, or a complete type
 *
 * @author glelouet
 *
 */
public class TypeDescription {

	/** reference to a type */
	public String ref= null;

	/**
	 * description of the field, will be used as a comment.
	 */
	public String desc = null;

	/**
	 * if a list, must describe the internal type.
	 */
	public TypeDescription of;

	/**
	 * if a map, must describe the internal key type.
	 */
	public TypeDescription key;

	/**
	 * if a map, must describe the internal value type
	 */
	public TypeDescription val;

	/**
	 * if a structure, must describe the actual structure.
	 */
	public HashMap<String, TypeDescription> struct;

	public TypeDescription() {
	}

	public TypeDescription(String ref) {
		this.ref = ref;
	}

	@Override
	public String toString() {
		if (ref != null) {
			return "ref:" + ref;
		}
		if (of != null) {
			return "of:" + of;
		}
		if (key != null && val != null) {
			return "map:" + key + ":" + val;
		}
		if (struct != null) {
			return "struct:" + struct;
		}
		return "empty";
	}

}
