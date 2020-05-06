package fr.lelouet.tools.application.settings.storeformat;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.yaml.snakeyaml.Yaml;

import fr.lelouet.tools.application.settings.StoreFormat;

public class YAMLFormat implements StoreFormat {

	@Override
	public <T> T load(InputStream is, Class<T> clazz) {
		return new Yaml().loadAs(is, clazz);
	}

	@Override
	public <T> InputStream store(T object) {
		return new ByteArrayInputStream(new Yaml().dump(object).getBytes());
	}

}
