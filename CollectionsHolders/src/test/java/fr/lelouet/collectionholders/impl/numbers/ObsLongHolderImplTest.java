package fr.lelouet.collectionholders.impl.numbers;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.lelouet.collectionholders.interfaces.numbers.ObsBoolHolder;

public class ObsLongHolderImplTest {

	@Test(timeOut = 500)
	public void testCreation() {
		ObsLongHolderImpl twenty = new ObsLongHolderImpl(20);
		Assert.assertEquals((long) twenty.add(4l).get(), 24l);
		Assert.assertEquals((long) twenty.sub(4l).get(), 16l);
		Assert.assertEquals((long) twenty.mult(4l).get(), 80l);
		Assert.assertEquals((long) twenty.div(4l).get(), 5l);

		Assert.assertEquals((long) twenty.get(), 20l);

		ObsLongHolderImpl four = new ObsLongHolderImpl(4l);
		Assert.assertEquals((long) twenty.add(four).get(), 24l);
		Assert.assertEquals((long) twenty.sub(four).get(), 16l);
		Assert.assertEquals((long) twenty.mult(four).get(), 80l);
		Assert.assertEquals((long) twenty.div(four).get(), 5l);

		// test predicate
		ObsBoolHolder even = twenty.test(i -> i % 2 == 0);
		ObsBoolHolder odd = even.not();

		Assert.assertTrue(even.get());
		Assert.assertFalse(odd.get());

	}

	@Test(timeOut = 500)
	public void testCombine() {
		Assert.assertEquals(new ObsLongHolderImpl(5l).add(new ObsLongHolderImpl(4l)).add(1l).sub(new ObsLongHolderImpl(5l))
				.sub(new ObsLongHolderImpl(5l)).get(), (Long) 0l);
	}

}
