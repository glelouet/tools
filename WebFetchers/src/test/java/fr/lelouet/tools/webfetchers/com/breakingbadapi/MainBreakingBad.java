package fr.lelouet.tools.webfetchers.com.breakingbadapi;

public class MainBreakingBad {

	public static void main(String[] args) {
		BreakingbadapiService access = BreakingbadapiService.INSTANCE;
		access.character(1).follow(ch -> System.out.println("received character from id 1 : " + ch[0].name));
	}

}
