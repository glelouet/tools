package fr.lelouet.collectionholders.impl.numbers;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.lelouet.collectionholders.interfaces.numbers.ObsBoolHolder;

public class ObsDoubleHolderImplTest {

	@Test(timeOut = 5000)
	public void testCreation() {
		ObsDoubleHolderImpl twenty = new ObsDoubleHolderImpl(20.0);
		Assert.assertEquals((double) twenty.add(4.0).get(), 24.0);
		Assert.assertEquals((double) twenty.sub(4.0).get(), 16.0);
		Assert.assertEquals((double) twenty.mult(4.0).get(), 80.0);
		Assert.assertEquals((double) twenty.div(4.0).get(), 5.0);

		Assert.assertEquals((double) twenty.get(), 20.0);

		ObsDoubleHolderImpl four = new ObsDoubleHolderImpl(4.0);
		Assert.assertEquals((double) twenty.add(four).get(), 24.0);
		Assert.assertEquals((double) twenty.sub(four).get(), 16.0);
		Assert.assertEquals((double) twenty.mult(four).get(), 80.0);
		Assert.assertEquals((double) twenty.div(four).get(), 5.0);

		// test predicate
		ObsBoolHolder even = twenty.test(i -> i % 2 == 0);
		ObsBoolHolder odd = even.not();

		Assert.assertTrue(even.get());
		Assert.assertFalse(odd.get());

	}

	@Test(timeOut = 5000)
	public void testCeilFloor() {
		ObsDoubleHolderImpl pi = new ObsDoubleHolderImpl(3.14);
		Assert.assertEquals((int) pi.ceil().get(), 4);
		Assert.assertEquals((int) pi.floor().get(), 3);
		ObsDoubleHolderImpl negpi = new ObsDoubleHolderImpl(-3.14);
		Assert.assertEquals((int) negpi.ceil().get(), -3);
		Assert.assertEquals((int) negpi.floor().get(), -4);
	}

}
