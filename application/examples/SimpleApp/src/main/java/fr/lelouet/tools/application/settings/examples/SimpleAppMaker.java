package fr.lelouet.tools.application.settings.examples;

import java.io.IOException;

import fr.lelouet.tools.application.settings.SettingsCompiler;

public class SimpleAppMaker {

	public static void main(String[] args) throws IOException {
		SettingsCompiler.main("simpleDescription.yaml", "src/generated/java");
	}

}
