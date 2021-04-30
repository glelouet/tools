package fr.lelouet.tools.webfetchers.com.icndb;

public class MainIcndb {

	public static void main(String[] args) {
		ICNDBService access = ICNDBService.INSTANCE;
		// access.joke(1).map(e -> e.value.joke).follow(e -> {
		// System.out.println("received new joke for id 1 : " + e);
		// });

		// access.jokeNOTYPE(1).map(e -> e.value.joke).follow(e -> {
		// System.out.println("received new jokenotype for id 1 : " + e);
		// });
		access.jokeWRONGVALUE(1).map(e -> e == null ? null : e.value[0].joke).follow(e -> {
			System.out.println("received new jokebadvalue for id 1 : " + e);
		});
	}

}
