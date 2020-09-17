package fr.lelouet.tools.lambdaref.withstore;

import fr.lelouet.tools.lambdaref.GCManage;

public class MainExample {

	public static void main(String[] args) {
		IntReceiver ir = new IntReceiver();
		StringHolder sr = new StringHolder();
		sr.listen(s -> ir.set(s.length()), ir);
		affectAndPrint(sr, "123", ir);

		System.out.println("adding weak referenced listeners");
		addLambda(sr);
		addSysoDynamic(sr);
		addSysoStatic(sr);
		affectAndPrint(sr, "1233", ir);

		System.out.println("force GC");
		GCManage.force();
		affectAndPrint(sr, "123456", ir);

	}

	protected static void affectAndPrint(StringHolder sr, String value, IntReceiver ir) {
		sr.set(value);
		System.out.println(" value for " + value + " is " + ir.get());
		System.out.println(" #listeners=" + sr.listeners());
	}

	protected static void addLambda(StringHolder ls) {
		IntReceiver is = new IntReceiver();
		ls.listen(s -> is.set(s.length()), is);
	}

	protected static void addSysoDynamic(StringHolder ls) {
		ls.listen(s -> System.out.println(" received(dynamic) " + s + " from " + ls.getClass().getSimpleName()), o -> {
		});
	}

	// no ref to the argument means the lambda is static and linked to the class
	protected static void addSysoStatic(StringHolder ls) {
		ls.listen(s -> System.out.println(" received(static) " + s), o -> {
		});
	}

}
