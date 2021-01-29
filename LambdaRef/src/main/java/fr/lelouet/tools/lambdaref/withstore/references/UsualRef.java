package fr.lelouet.tools.lambdaref.withstore.references;

/**
 * reference that keeps strong link to the refered item.
 *
 * @author
 *
 * @param <U>
 */
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