package fr.lelouet.collectionholders.impl.numbers;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.lelouet.collectionholders.interfaces.numbers.ObsBoolHolder;
import javafx.beans.property.SimpleObjectProperty;

public class ObsIntImplTest {

	@Test
	public void testCreation() {
		ObsIntHolderImpl twenty = new ObsIntHolderImpl(new SimpleObjectProperty<>(20));
		Assert.assertEquals((int) twenty.add(4).get(), 24);
		Assert.assertEquals((int) twenty.sub(4).get(), 16);
		Assert.assertEquals((int) twenty.mult(4).get(), 80);
		Assert.assertEquals((int) twenty.div(4).get(), 5);

		Assert.assertEquals((int) twenty.get(), 20);

		ObsIntHolderImpl four = new ObsIntHolderImpl(new SimpleObjectProperty<>(4));
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

}
