package fr.lelouet.tools.application.settings;

import java.util.HashMap;

/**
 * A type of a settings. Can be either a ref to an existing type, or a complete type
 *
 * @author glelouet
 *
 */
public class TypeDescription {

	public String ref= null;

	/**
	 * complex type if we are not a reference : can be a map, a list, or a
	 * structure.
	 *
	 * @author glelouet
	 *
	 */
	public enum COMPLEXETYPE {
		map, list, struct;
	}

	/** if not a reference, must be set. default is struct */
	public COMPLEXETYPE type = COMPLEXETYPE.struct;

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

}
