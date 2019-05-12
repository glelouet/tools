package fr.lelouet.tools.settings.yaml;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.yaml.snakeyaml.Yaml;

public class YamlSettingsTest {

	public static class FalseSettings extends YamlSettings {
		public String a = "bla";

		public FalseSettings() throws IOException {
			tmpFile = File.createTempFile("test", ".test");
		}

		File tmpFile;

		@Override
		public File findStoreFile() {
			return tmpFile;
		}

		@Override
		public File getStoreFile() {
			return tmpFile;
		}

		@Override
		public void storeLater() {
			store();
		}
	}

	@Test
	public void testSave() throws IOException {
		FalseSettings fs1 = new FalseSettings();
		FalseSettings fs2 = null;
		Assert.assertEquals(fs1.a, "bla");
		fs1.a = "bbb";
		fs1.store();
		fs2 = new Yaml().loadAs(new FileReader(fs1.getStoreFile()), FalseSettings.class);
		Assert.assertEquals(fs2.a, "bbb");
		fs2 = YamlSettings.load(FalseSettings.class);
		Assert.assertEquals(fs2.a, "bbb");
		fs2.erase();
		fs2 = YamlSettings.load(FalseSettings.class);
		Assert.assertEquals(fs2.a, "bla");
	}

}
