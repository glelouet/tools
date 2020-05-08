package fr.lelouet.tools.application.settings.description;

import java.util.LinkedHashMap;
import java.util.Map;

public class AccessDescription {

	/** name of the class to instantiate */
	public String name = null;

	/** if set, requires to set root class as extends this one */
	public String rootClass = null;

	public String path = null;

	public String format = null;

	/** additional parameters */
	public Map<String, String> params = new LinkedHashMap<>();

	public AccessDescription() {
	}

	public AccessDescription(String name) {
		this.name = name;
	}

}
