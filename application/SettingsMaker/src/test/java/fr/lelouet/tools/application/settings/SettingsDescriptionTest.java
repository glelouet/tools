package fr.lelouet.tools.application.settings;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SettingsDescriptionTest {

	@Test
	public void testLoad() {
		SettingsDescription settings = SettingsDescription
				.load(getClass().getClassLoader().getResourceAsStream("simpleDescription.yaml"));
		Assert.assertEquals(settings.name, "simpleapp");
		Assert.assertEquals(settings.path, "my.simple.app");
		Assert.assertEquals(settings.contains.get("yes").ref, "bool");
		Assert.assertEquals(settings.contains.get("name").ref, "string");
		TypeDescription locations = settings.contains.get("locations");
		Assert.assertNotNull(locations, "" + settings.contains);
		Assert.assertEquals(locations.struct.get("a").ref, "string", "" + locations.struct);
		Assert.assertEquals(locations.struct.get("b").ref, "double");
	}

}
