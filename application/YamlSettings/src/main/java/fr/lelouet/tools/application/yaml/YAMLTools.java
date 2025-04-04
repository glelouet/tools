package fr.lelouet.tools.application.yaml;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;

public class YAMLTools {

	public static DumperOptions blockDumper() {
		DumperOptions ret = new DumperOptions();
		ret.setDefaultFlowStyle(FlowStyle.BLOCK);
		return ret;
	}

	public static Yaml cleanBlock() {
		return new Yaml(new CleanRepresenter(), YAMLTools.blockDumper());
	}

}
