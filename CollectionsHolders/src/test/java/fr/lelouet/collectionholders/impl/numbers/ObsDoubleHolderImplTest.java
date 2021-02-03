package fr.lelouet.collectionholders.impl.numbers;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.lelouet.collectionholders.interfaces.numbers.ObsBoolHolder;

public class ObsDoubleHolderImplTest {

	@Test(timeOut = 500)
	public void testCreation() {
		ObsDoubleHolderImpl holder = new ObsDoubleHolderImpl(20.0);
		Assert.assertEquals((double) holder.add(4.0).get(), 24.0);
		Assert.assertEquals((double) holder.sub(4.0).get(), 16.0);
		Assert.assertEquals((double) holder.mult(4.0).get(), 80.0);
		Assert.assertEquals((double) holder.div(4.0).get(), 5.0);

		Assert.assertEquals((double) holder.get(), 20.0);

		ObsDoubleHolderImpl four = new ObsDoubleHolderImpl(4.0);
		Assert.assertEquals((double) holder.add(four).get(), 24.0);
		Assert.assertEquals((double) holder.sub(four).get(), 16.0);
		Assert.assertEquals((double) holder.mult(four).get(), 80.0);
		Assert.assertEquals((double) holder.div(four).get(), 5.0);

		// test predicate
		ObsBoolHolder even = holder.test(i -> i % 2 == 0);
		ObsBoolHolder odd = even.not();

		Assert.assertTrue(even.get());
		Assert.assertFalse(odd.get());

		holder.set(21.0);
		Assert.assertFalse(even.get());
		Assert.assertTrue(odd.get());

	}

	@Test(timeOut = 500)
	public void testCeilFloor() {
		ObsDoubleHolderImpl pi = new ObsDoubleHolderImpl(3.14);
		Assert.assertEquals((int) pi.ceil().get(), 4);
		Assert.assertEquals((int) pi.floor().get(), 3);
		ObsDoubleHolderImpl negpi = new ObsDoubleHolderImpl(-3.14);
		Assert.assertEquals((int) negpi.ceil().get(), -3);
		Assert.assertEquals((int) negpi.floor().get(), -4);
	}

	@Test(timeOut = 500)
	public void testCompare() {
		ObsDoubleHolderImpl pi = ObsDoubleHolderImpl.of(3.14);
		ObsDoubleHolderImpl three = ObsDoubleHolderImpl.of(3);
		ObsDoubleHolderImpl four = ObsDoubleHolderImpl.of(4);
		Assert.assertTrue(pi.gt(3.0).get());
		Assert.assertFalse(pi.ge(4.0).get());
		Assert.assertTrue(pi.gt(three).get());
		Assert.assertFalse(pi.gt(four).get());
		Assert.assertTrue(pi.ge(three).get());
		Assert.assertFalse(pi.ge(four).get());
		Assert.assertFalse(pi.lt(3.0).get());
		Assert.assertTrue(pi.le(4.0).get());
		Assert.assertFalse(pi.lt(three).get());
		Assert.assertTrue(pi.le(four).get());
	}

}
