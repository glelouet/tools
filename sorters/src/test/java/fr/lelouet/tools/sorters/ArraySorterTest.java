package fr.lelouet.tools.sorters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import fr.lelouet.tools.sorters.ArraySorter.IntEvaluator;

public class ArraySorterTest {

	protected static final int minval = 0;
	protected static final int maxval = 100;

	@Test(dataProvider = "intListProvider")
	public void simpleTest(final List<Integer> toAdd) {
		ArraySorter.IntEvaluator<Integer> evaluator = new IntEvaluator<Integer>() {

			@Override
			public int eval(Integer element) {
				return element;
			}

			@Override
			public int maxval() {
				return maxval;
			}

			@Override
			public int minval() {
				return minval;
			}
		};

		ArraySorter<Integer> intSorter = new ArraySorter<Integer>(evaluator);
		for (Integer i : toAdd) {
			intSorter.add(i);
		}
		List<Integer> sortedList = intSorter.toList();
		Assert.assertEquals(sortedList.size(), toAdd.size());
		int lastInt = 0;
		for (Integer i : sortedList) {
			Assert.assertTrue(toAdd.contains(i));
			Assert.assertTrue(lastInt <= i);
			lastInt = i;
		}
	}

	@DataProvider(name = "intListProvider")
	public Object[][] intListProvider() {
		return new Object[][] { { new ArrayList<Integer>() },
				{ Arrays.asList(10, 20, 30, 45, 45, 50, 30, 10, 20, 10) },
				{ Arrays.asList(10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0) },
				{ generateRandomList(200, minval, maxval) }

		};
	}

	protected static List<Integer> generateRandomList(int size, int min, int max) {
		ArrayList<Integer> ret = new ArrayList<Integer>();
		Random ran = new Random();
		int range = max - min + 1;
		for (int i = 0; i < size; i++) {
			ret.add(ran.nextInt(range) + min);
		}
		return ret;
	}
}
