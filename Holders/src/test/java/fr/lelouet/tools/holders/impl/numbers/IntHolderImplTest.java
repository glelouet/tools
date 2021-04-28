package fr.lelouet.tools.holders.impl.numbers;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.lelouet.tools.holders.interfaces.numbers.BoolHolder;

public class IntHolderImplTest {

	@Test(timeOut = 500)
	public void testCreation() {
		IntHolderImpl holder = new IntHolderImpl(20);
		Assert.assertEquals((int) holder.add(4).get(), 24);
		Assert.assertEquals((int) holder.sub(4).get(), 16);
		Assert.assertEquals((int) holder.mult(4).get(), 80);
		Assert.assertEquals((int) holder.div(4).get(), 5);

		Assert.assertEquals((int) holder.get(), 20);

		IntHolderImpl four = new IntHolderImpl(4);
		Assert.assertEquals((int) holder.add(four).get(), 24);
		Assert.assertEquals((int) holder.sub(four).get(), 16);
		Assert.assertEquals((int) holder.mult(four).get(), 80);
		Assert.assertEquals((int) holder.div(four).get(), 5);

		// test predicate
		BoolHolder even = holder.test(i -> i % 2 == 0);
		BoolHolder odd = even.not();

		Assert.assertTrue(even.get());
		Assert.assertFalse(odd.get());

		holder.set(21);
		Assert.assertFalse(even.get());
		Assert.assertTrue(odd.get());

	}

	@Test(timeOut = 500)
	public void testCombine() {
		Assert.assertEquals(new IntHolderImpl(5).add(new IntHolderImpl(4)).add(1).sub(new IntHolderImpl(5))
				.sub(new IntHolderImpl(5)).get(), (Integer) 0);
	}

}
