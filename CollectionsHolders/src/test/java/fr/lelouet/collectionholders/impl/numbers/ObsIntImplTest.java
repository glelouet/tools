package fr.lelouet.collectionholders.impl.numbers;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.lelouet.collectionholders.interfaces.numbers.ObsBoolHolder;

public class ObsIntImplTest {

	@Test
	public void testCreation() {
		ObsIntHolderImpl twenty = new ObsIntHolderImpl(20);
		Assert.assertEquals((int) twenty.add(4).get(), 24);
		Assert.assertEquals((int) twenty.sub(4).get(), 16);
		Assert.assertEquals((int) twenty.mult(4).get(), 80);
		Assert.assertEquals((int) twenty.div(4).get(), 5);

		Assert.assertEquals((int) twenty.get(), 20);

		ObsIntHolderImpl four = new ObsIntHolderImpl(4);
		Assert.assertEquals((int) twenty.add(four).get(), 24);
		Assert.assertEquals((int) twenty.sub(four).get(), 16);
		Assert.assertEquals((int) twenty.mult(four).get(), 80);
		Assert.assertEquals((int) twenty.div(four).get(), 5);

		// test predicate
		ObsBoolHolder even = twenty.test(i -> i % 2 == 0);
		ObsBoolHolder odd = even.not();

		Assert.assertTrue(even.get());
		Assert.assertFalse(odd.get());

	}

	@Test
	public void testCombine() {
		Assert.assertEquals(new ObsIntHolderImpl(5).add(new ObsIntHolderImpl(4)).add(1).sub(new ObsIntHolderImpl(5))
				.sub(new ObsIntHolderImpl(5)).get(), (Integer) 0);
	}

}
