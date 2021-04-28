package fr.lelouet.tools.webfetchers.icndb;

public class MainIcndb {

	public static void main(String[] args) {
		ICNDBService.INSTANCE.joke(1).map(e -> e.value.joke).follow(e -> {
			System.out.println("received new joke for id 1 : " + e);
		});
	}

}
