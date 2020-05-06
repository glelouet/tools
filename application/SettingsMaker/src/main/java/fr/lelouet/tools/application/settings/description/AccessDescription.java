package fr.lelouet.tools.application.settings.description;

import java.util.LinkedHashMap;
import java.util.Map;

public class AccessDescription {

	public String name = null;

	public Map<String, String> params = new LinkedHashMap<>();

	public AccessDescription() {
	}

	public AccessDescription(String name) {
		this.name = name;
	}

}
