package fr.lelouet.tools.cache;

import org.testng.Assert;
import org.testng.annotations.Test;

public class UseCacheTest extends UseCache {

	@Cached
	private String foundString = null;

	protected int callCount = 0;

	protected String findString() {
		foundString = "calls:" + callCount++;
		return foundString;
	}

	public String getString() {
		return foundString != null ? foundString : findString();
	}

	@Test
	public void simpleCache() {
		String debugone = getString();
		String debugtwo = getString();

		cleanCache();

		String debugthree = getString();
		String debugfour = getString();

		Assert.assertEquals(debugtwo, debugone);
		Assert.assertNotSame(debugthree, debugone);
		Assert.assertEquals(debugfour, debugthree);
	}

	protected class WithBaseTypes extends UseCache {
		@Cached
		int cachedInt;
		@Cached
		double cachedDouble;
		@Cached
		boolean cachedBoolean;
		@Cached
		long cachedLong;
	}

	@Test
	public void withBaseTypes() {
		WithBaseTypes toTest = new WithBaseTypes();
		toTest.cachedBoolean = true;
		toTest.cachedDouble = -5;
		toTest.cachedInt = 5;
		toTest.cachedLong = 5L;
		toTest.cleanCache();
		Assert.assertEquals(toTest.cachedBoolean, false);
		Assert.assertEquals(toTest.cachedDouble, 0.0);
		Assert.assertEquals(toTest.cachedInt, 0);
		Assert.assertEquals(toTest.cachedLong, 0);
	}

	protected class WithDefaultSpecification extends UseCache {
		@Cached("getDefaultInt")
		int cachedInt;

		public static final int DEFAULTINT = -2;

		public Object getDefaultInt() {
			return DEFAULTINT;
		}

		@Cached("getDefaultObject")
		Object cachedObject;

		public static final String DEFAULTOBJECT = "default string";

		public Object getDefaultObject() {
			return DEFAULTOBJECT;
		}
	}

	@Test
	public void withDefaultSpecification() {
		WithDefaultSpecification toTest = new WithDefaultSpecification();
		toTest.cachedInt = 72;
		toTest.cachedObject = "lol";
		toTest.cleanCache();
		Assert.assertEquals(toTest.cachedInt,
				WithDefaultSpecification.DEFAULTINT);
		Assert.assertEquals(toTest.cachedObject,
				WithDefaultSpecification.DEFAULTOBJECT);

	}

}
