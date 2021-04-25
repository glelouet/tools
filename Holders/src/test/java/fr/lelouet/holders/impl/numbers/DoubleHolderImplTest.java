package fr.lelouet.holders.impl.numbers;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.lelouet.holders.interfaces.numbers.BoolHolder;

public class DoubleHolderImplTest {

	@Test(timeOut = 500)
	public void testCreation() {
		DoubleHolderImpl holder = new DoubleHolderImpl(20.0);
		Assert.assertEquals((double) holder.add(4.0).get(), 24.0);
		Assert.assertEquals((double) holder.sub(4.0).get(), 16.0);
		Assert.assertEquals((double) holder.mult(4.0).get(), 80.0);
		Assert.assertEquals((double) holder.div(4.0).get(), 5.0);

		Assert.assertEquals((double) holder.get(), 20.0);

		DoubleHolderImpl four = new DoubleHolderImpl(4.0);
		Assert.assertEquals((double) holder.add(four).get(), 24.0);
		Assert.assertEquals((double) holder.sub(four).get(), 16.0);
		Assert.assertEquals((double) holder.mult(four).get(), 80.0);
		Assert.assertEquals((double) holder.div(four).get(), 5.0);

		// test predicate
		BoolHolder even = holder.test(i -> i % 2 == 0);
		BoolHolder odd = even.not();

		Assert.assertTrue(even.get());
		Assert.assertFalse(odd.get());

		holder.set(21.0);
		Assert.assertFalse(even.get());
		Assert.assertTrue(odd.get());

	}

	@Test(timeOut = 500)
	public void testCeilFloor() {
		DoubleHolderImpl pi = new DoubleHolderImpl(3.14);
		Assert.assertEquals((int) pi.ceil().get(), 4);
		Assert.assertEquals((int) pi.floor().get(), 3);
		DoubleHolderImpl negpi = new DoubleHolderImpl(-3.14);
		Assert.assertEquals((int) negpi.ceil().get(), -3);
		Assert.assertEquals((int) negpi.floor().get(), -4);
	}

	@Test(timeOut = 500)
	public void testCompare() {
		DoubleHolderImpl pi = DoubleHolderImpl.of(3.14);
		DoubleHolderImpl three = DoubleHolderImpl.of(3);
		DoubleHolderImpl four = DoubleHolderImpl.of(4);
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
