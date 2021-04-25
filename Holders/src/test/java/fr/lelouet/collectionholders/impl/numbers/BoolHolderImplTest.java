package fr.lelouet.collectionholders.impl.numbers;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.lelouet.collectionholders.interfaces.numbers.BoolHolder;

public class BoolHolderImplTest {

	@Test
	public void testCreation() {
		Assert.assertEquals(BoolHolderImpl.of(true).get(), Boolean.TRUE);
		Assert.assertEquals(BoolHolderImpl.of(false).get(), Boolean.FALSE);
		BoolHolderImpl test = new BoolHolderImpl();
		Assert.assertFalse(test.isDataAvailable());
		test.set(false);
		Assert.assertEquals(test.get(), Boolean.FALSE);
		test.set(true);
		Assert.assertEquals(test.get(), Boolean.TRUE);
	}

	@Test
	public void testBIOperations() {
		BoolHolderImpl b1 = new BoolHolderImpl();
		BoolHolderImpl b2 = new BoolHolderImpl();
		BoolHolder bxor = b1.xor(b2);
		BoolHolder band = b1.and(b2);
		BoolHolder bor = b1.or(b2);
		BoolHolder mxor = b1.xor(true);
		BoolHolder mand = b1.and(true);
		BoolHolder mor = b1.or(true);

		b1.set(false);
		b2.set(true);

		Assert.assertEquals(bxor.get(), Boolean.TRUE);
		Assert.assertEquals(band.get(), Boolean.FALSE);
		Assert.assertEquals(bor.get(), Boolean.TRUE);
		Assert.assertEquals(mxor.get(), Boolean.TRUE);
		Assert.assertEquals(mand.get(), Boolean.FALSE);
		Assert.assertEquals(mor.get(), Boolean.TRUE);

	}

}
