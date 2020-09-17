package fr.lelouet.tools.lambdaref.withstore.references;

public class UsualRef<U> implements IRef<U> {

	private final U referent;

	public UsualRef(U referent) {
		this.referent = referent;
	}

	@Override
	public U get() {
		return referent;
	}

}