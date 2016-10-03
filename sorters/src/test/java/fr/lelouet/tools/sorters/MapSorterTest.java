package fr.lelouet.tools.sorters;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

public class MapSorterTest {

	@Test
	public void simpleTest() {
		Map<String, Double> toTest = new HashMap<String, Double>();
		toTest.put("a", 10.0);
		toTest.put("b", 15.0);
		toTest.put("c", 5.0);
		toTest.put("d", 20.0);
		toTest.put("e", 0.0);
		List<String> sorted = MapSorter.sort(toTest);
		Assert.assertEquals(Arrays.asList(new String[] { "e", "c", "a", "b",
		"d" }), sorted);
	}

}
