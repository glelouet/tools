package fr.lelouet.tools.container;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.lelouet.tools.containers.ListContainer;

public class ListContainerTest {
	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(ListContainerTest.class);

	@Test
	public void testPopHasVal() {
		String[] vals = new String[] { "a", "hello", "boy" };
		ListContainer<String> c = new ListContainer<String>();
		for (String val : vals) {
			c.set(val);
			Assert.assertTrue(c.hasVal());
			Assert.assertEquals(c.popNextVal(), val);
		}
	}

	@Test(dependsOnMethods = "testPopHasVal")
	public void testCorrectIteration() {
		ListContainer<String> c = new ListContainer<String>();
		String[] vals = new String[] { "a", "zz", "ee" };
		for (String val : vals) {
			c.set(val);
		}
		List<String> history = new ArrayList<String>();
		while (c.hasVal()) {
			history.add(c.popNextVal());
		}
		assertEquals(history, Arrays.asList(vals));
	}
}
