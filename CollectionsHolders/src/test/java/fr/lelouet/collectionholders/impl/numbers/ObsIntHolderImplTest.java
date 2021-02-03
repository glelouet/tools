package fr.lelouet.collectionholders.impl.numbers;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.lelouet.collectionholders.interfaces.numbers.ObsBoolHolder;

public class ObsIntHolderImplTest {

	@Test(timeOut = 500)
	public void testCreation() {
		ObsIntHolderImpl holder = new ObsIntHolderImpl(20);
		Assert.assertEquals((int) holder.add(4).get(), 24);
		Assert.assertEquals((int) holder.sub(4).get(), 16);
		Assert.assertEquals((int) holder.mult(4).get(), 80);
		Assert.assertEquals((int) holder.div(4).get(), 5);

		Assert.assertEquals((int) holder.get(), 20);

		ObsIntHolderImpl four = new ObsIntHolderImpl(4);
		Assert.assertEquals((int) holder.add(four).get(), 24);
		Assert.assertEquals((int) holder.sub(four).get(), 16);
		Assert.assertEquals((int) holder.mult(four).get(), 80);
		Assert.assertEquals((int) holder.div(four).get(), 5);

		// test predicate
		ObsBoolHolder even = holder.test(i -> i % 2 == 0);
		ObsBoolHolder odd = even.not();

		Assert.assertTrue(even.get());
		Assert.assertFalse(odd.get());

		holder.set(21);
		Assert.assertFalse(even.get());
		Assert.assertTrue(odd.get());

	}

	@Test(timeOut = 500)
	public void testCombine() {
		Assert.assertEquals(new ObsIntHolderImpl(5).add(new ObsIntHolderImpl(4)).add(1).sub(new ObsIntHolderImpl(5))
				.sub(new ObsIntHolderImpl(5)).get(), (Integer) 0);
	}

}
