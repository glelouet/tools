package fr.lelouet.collectionholders.impl.numbers;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.lelouet.collectionholders.interfaces.numbers.ObsBoolHolder;

public class ObsFloatHolderImplTest {

	@Test(timeOut = 500)
	public void testCreation() {
		ObsFloatHolderImpl holder = new ObsFloatHolderImpl(20.0f);
		Assert.assertEquals((float) holder.add(4.0f).get(), 24.0f);
		Assert.assertEquals((float) holder.sub(4.0f).get(), 16.0f);
		Assert.assertEquals((float) holder.mult(4.0f).get(), 80.0f);
		Assert.assertEquals((float) holder.div(4.0f).get(), 5.0f);

		Assert.assertEquals((float) holder.get(), 20.0f);

		ObsFloatHolderImpl four = new ObsFloatHolderImpl(4.0f);
		Assert.assertEquals((float) holder.add(four).get(), 24.0f);
		Assert.assertEquals((float) holder.sub(four).get(), 16.0f);
		Assert.assertEquals((float) holder.mult(four).get(), 80.0f);
		Assert.assertEquals((float) holder.div(four).get(), 5.0f);

		// test predicate
		ObsBoolHolder even = holder.test(i -> i % 2 == 0);
		ObsBoolHolder odd = even.not();

		Assert.assertTrue(even.get());
		Assert.assertFalse(odd.get());

		holder.set(21f);
		Assert.assertFalse(even.get());
		Assert.assertTrue(odd.get());

	}

	@Test(timeOut = 500)
	public void testCeilFloor() {
		ObsFloatHolderImpl pi = new ObsFloatHolderImpl(3.14f);
		Assert.assertEquals((int) pi.ceil().get(), 4);
		Assert.assertEquals((int) pi.floor().get(), 3);
		ObsFloatHolderImpl negpi = new ObsFloatHolderImpl(-3.14f);
		Assert.assertEquals((int) negpi.ceil().get(), -3);
		Assert.assertEquals((int) negpi.floor().get(), -4);
	}

	@Test(timeOut = 500)
	public void testCompare() {
		ObsFloatHolderImpl pi = ObsFloatHolderImpl.of(3.14);
		ObsFloatHolderImpl three = ObsFloatHolderImpl.of(3);
		ObsFloatHolderImpl four = ObsFloatHolderImpl.of(4);
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
