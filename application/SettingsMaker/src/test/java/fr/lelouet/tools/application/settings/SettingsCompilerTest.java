package fr.lelouet.tools.application.settings;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SettingsCompilerTest {

	@Test
	public void testName() {
		Assert.assertEquals(SettingsCompiler.makeJavaClassName("-Base-Name-"), "Base_Name_");
		Assert.assertEquals(SettingsCompiler.makeJavaClassName("-base--Name-"), "Base_Name_");
		Assert.assertEquals(SettingsCompiler.makeJavaClassName("007"), "C_007");
	}

}
