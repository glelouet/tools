package fr.lelouet.collectionholders.impl.numbers;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.lelouet.collectionholders.interfaces.numbers.ObsBoolHolder;

public class ObsLongHolderImplTest {

	@Test(timeOut = 500)
	public void testCreation() {
		ObsLongHolderImpl holder = new ObsLongHolderImpl(20);
		Assert.assertEquals((long) holder.add(4l).get(), 24l);
		Assert.assertEquals((long) holder.sub(4l).get(), 16l);
		Assert.assertEquals((long) holder.mult(4l).get(), 80l);
		Assert.assertEquals((long) holder.div(4l).get(), 5l);

		Assert.assertEquals((long) holder.get(), 20l);

		ObsLongHolderImpl four = new ObsLongHolderImpl(4l);
		Assert.assertEquals((long) holder.add(four).get(), 24l);
		Assert.assertEquals((long) holder.sub(four).get(), 16l);
		Assert.assertEquals((long) holder.mult(four).get(), 80l);
		Assert.assertEquals((long) holder.div(four).get(), 5l);

		// test predicate
		ObsBoolHolder even = holder.test(i -> i % 2 == 0);
		ObsBoolHolder odd = even.not();

		Assert.assertTrue(even.get());
		Assert.assertFalse(odd.get());

		holder.set(21l);
		Assert.assertFalse(even.get());
		Assert.assertTrue(odd.get());

	}

	@Test(timeOut = 500)
	public void testCombine() {
		Assert.assertEquals(new ObsLongHolderImpl(5l).add(new ObsLongHolderImpl(4l)).add(1l).sub(new ObsLongHolderImpl(5l))
				.sub(new ObsLongHolderImpl(5l)).get(), (Long) 0l);
	}

	@Test(timeOut = 500)
	public void testCompare() {
		ObsLongHolderImpl three = ObsLongHolderImpl.of(3);
		ObsLongHolderImpl four = ObsLongHolderImpl.of(4);
		ObsLongHolderImpl five = ObsLongHolderImpl.of(5);
		Assert.assertTrue(five.gt(3l).get());
		Assert.assertFalse(three.ge(4l).get());
		Assert.assertTrue(five.gt(three).get());
		Assert.assertFalse(four.gt(four).get());
		Assert.assertTrue(four.ge(three).get());
		Assert.assertFalse(three.ge(four).get());
		Assert.assertFalse(three.lt(3l).get());
		Assert.assertTrue(four.le(4l).get());
		Assert.assertFalse(three.lt(three).get());
		Assert.assertTrue(three.le(four).get());
	}

}
