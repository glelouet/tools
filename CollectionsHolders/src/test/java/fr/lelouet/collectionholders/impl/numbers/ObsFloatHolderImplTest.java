package fr.lelouet.collectionholders.impl.numbers;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.lelouet.collectionholders.interfaces.numbers.ObsBoolHolder;

public class ObsFloatHolderImplTest {

	@Test(timeOut = 500)
	public void testCreation() {
		ObsFloatHolderImpl twenty = new ObsFloatHolderImpl(20.0f);
		Assert.assertEquals((float) twenty.add(4.0f).get(), 24.0f);
		Assert.assertEquals((float) twenty.sub(4.0f).get(), 16.0f);
		Assert.assertEquals((float) twenty.mult(4.0f).get(), 80.0f);
		Assert.assertEquals((float) twenty.div(4.0f).get(), 5.0f);

		Assert.assertEquals((float) twenty.get(), 20.0f);

		ObsFloatHolderImpl four = new ObsFloatHolderImpl(4.0f);
		Assert.assertEquals((float) twenty.add(four).get(), 24.0f);
		Assert.assertEquals((float) twenty.sub(four).get(), 16.0f);
		Assert.assertEquals((float) twenty.mult(four).get(), 80.0f);
		Assert.assertEquals((float) twenty.div(four).get(), 5.0f);

		// test predicate
		ObsBoolHolder even = twenty.test(i -> i % 2 == 0);
		ObsBoolHolder odd = even.not();

		Assert.assertTrue(even.get());
		Assert.assertFalse(odd.get());

	}

	@Test(timeOut = 500)
	public void testCeilFloor() {
		ObsFloatHolderImpl pi = new ObsFloatHolderImpl(3.14f);
		Assert.assertEquals((int) pi.ceil().get(), 4);
		Assert.assertEquals((int) pi.floor().get(), 3);
		ObsFloatHolderImpl negpi = new ObsFloatHolderImpl(-3.14f);
		Assert.assertEquals((int) negpi.ceil().get(), -3);
		Assert.assertEquals((int) negpi.floor().get(), -4);
		Assert.assertTrue(pi.gt(3f).get());
		Assert.assertFalse(pi.ge(4f).get());
	}

}
