package fr.lelouet.tools.lambdaref.withstore;

import fr.lelouet.tools.lambdaref.GCManage;

public class MainChain {

	public static void main(String[] args) {
		RWHolder<String> source = new RWHolder<>();
		RWHolder<String> dest = source.map(s -> s.length()).map(i -> "" + i);
		source.set("1234");
		System.out.println(dest.get());
		GCManage.force();
		source.set("123");
		System.out.println(dest.get());
	}

}
