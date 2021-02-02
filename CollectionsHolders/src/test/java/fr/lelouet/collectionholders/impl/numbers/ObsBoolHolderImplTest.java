package fr.lelouet.collectionholders.impl.numbers;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.lelouet.collectionholders.interfaces.numbers.ObsBoolHolder;

public class ObsBoolHolderImplTest {

	@Test
	public void testCreation() {
		Assert.assertEquals(ObsBoolHolderImpl.of(true).get(), Boolean.TRUE);
		Assert.assertEquals(ObsBoolHolderImpl.of(false).get(), Boolean.FALSE);
		ObsBoolHolderImpl test = new ObsBoolHolderImpl();
		Assert.assertFalse(test.isDataAvailable());
		test.set(false);
		Assert.assertEquals(test.get(), Boolean.FALSE);
		test.set(true);
		Assert.assertEquals(test.get(), Boolean.TRUE);
	}

	@Test
	public void testBIOperations() {
		ObsBoolHolderImpl b1 = new ObsBoolHolderImpl();
		ObsBoolHolderImpl b2 = new ObsBoolHolderImpl();
		ObsBoolHolder bxor = b1.xor(b2);
		ObsBoolHolder band = b1.and(b2);
		ObsBoolHolder bor = b1.or(b2);
		ObsBoolHolder mxor = b1.xor(true);
		ObsBoolHolder mand = b1.and(true);
		ObsBoolHolder mor = b1.or(true);

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
