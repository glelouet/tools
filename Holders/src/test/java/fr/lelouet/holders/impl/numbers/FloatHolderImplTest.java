package fr.lelouet.holders.impl.numbers;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.lelouet.holders.interfaces.numbers.BoolHolder;

public class FloatHolderImplTest {

	@Test(timeOut = 500)
	public void testCreation() {
		FloatHolderImpl holder = new FloatHolderImpl(20.0f);
		Assert.assertEquals((float) holder.add(4.0f).get(), 24.0f);
		Assert.assertEquals((float) holder.sub(4.0f).get(), 16.0f);
		Assert.assertEquals((float) holder.mult(4.0f).get(), 80.0f);
		Assert.assertEquals((float) holder.div(4.0f).get(), 5.0f);

		Assert.assertEquals((float) holder.get(), 20.0f);

		FloatHolderImpl four = new FloatHolderImpl(4.0f);
		Assert.assertEquals((float) holder.add(four).get(), 24.0f);
		Assert.assertEquals((float) holder.sub(four).get(), 16.0f);
		Assert.assertEquals((float) holder.mult(four).get(), 80.0f);
		Assert.assertEquals((float) holder.div(four).get(), 5.0f);

		// test predicate
		BoolHolder even = holder.test(i -> i % 2 == 0);
		BoolHolder odd = even.not();

		Assert.assertTrue(even.get());
		Assert.assertFalse(odd.get());

		holder.set(21f);
		Assert.assertFalse(even.get());
		Assert.assertTrue(odd.get());

	}

	@Test(timeOut = 500)
	public void testCeilFloor() {
		FloatHolderImpl pi = new FloatHolderImpl(3.14f);
		Assert.assertEquals((int) pi.ceil().get(), 4);
		Assert.assertEquals((int) pi.floor().get(), 3);
		FloatHolderImpl negpi = new FloatHolderImpl(-3.14f);
		Assert.assertEquals((int) negpi.ceil().get(), -3);
		Assert.assertEquals((int) negpi.floor().get(), -4);
	}

	@Test(timeOut = 500)
	public void testCompare() {
		FloatHolderImpl pi = FloatHolderImpl.of(3.14);
		FloatHolderImpl three = FloatHolderImpl.of(3);
		FloatHolderImpl four = FloatHolderImpl.of(4);
		Assert.assertTrue(pi.gt(3f).get());
		Assert.assertFalse(pi.ge(4f).get());
		Assert.assertTrue(pi.gt(three).get());
		Assert.assertFalse(pi.gt(four).get());
		Assert.assertTrue(pi.ge(three).get());
		Assert.assertFalse(pi.ge(four).get());
		Assert.assertFalse(pi.lt(3f).get());
		Assert.assertTrue(pi.le(4f).get());
		Assert.assertFalse(pi.lt(three).get());
		Assert.assertTrue(pi.le(four).get());
	}

}
