package fr.lelouet.tools.application.yaml;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;

public class YAMLTools {

	public static DumperOptions blockDumper() {
		DumperOptions ret = new DumperOptions();
		ret.setDefaultFlowStyle(FlowStyle.BLOCK);
		return ret;
	}

}
