package fr.lelouet.tools.application.settings.examples;

import java.io.IOException;

import fr.lelouet.tools.application.settings.SettingsCompiler;

public class JavaBeanExample {

	public static void main(String[] args) throws IOException {
		SettingsCompiler.main("ExampleWithBeans.yaml", "src/generated/java");
	}

}
