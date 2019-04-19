package fr.lelouet.tools.container;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.lelouet.tools.containers.Container;

public class ContainerTest {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(ContainerTest.class);

	@Test
	public void testGetAndSet() {
		String val = "a";
		Container<String> c = new Container<String>();
		c.set(val);
		assertEquals(c.get(), val);
	}

	@Test(dependsOnMethods = "testGetAndSet")
	public void testMultipleGetAndSet() {
		String[] vals = new String[] { "a", "b", "c", "d" };
		Container<String> c = new Container<String>();
		for (String val : vals) {
			c.set(val);
			assertEquals(c.get(), val);
		}
	}

	@Test(dependsOnMethods = "testGetAndSet")
	public void testOnReplace() {
		final List<String> received = new ArrayList<String>();
		String[] vals = new String[] { "a", "b", "c", "d" };
		List<String> toSend = Arrays.asList(vals);
		Container<String> c = new Container<String>() {
			@Override
			public void onReplace(String before, String after) {
				Assert.assertNotSame(after, before,
						"same object after a replace");
				received.add(after);
			}
		};
		for (String val : toSend) {
			c.set(val);
		}
		assertEquals(received, toSend);
	}

	@Test(dependsOnMethods = "testGetAndSet")
	public void testBeforeGet() {
		final List<String> strAccess = new ArrayList<String>();
		String[] vals = new String[] { "a", "b", "c" };
		Container<String> c = new Container<String>() {
			@Override
			public void beforeGet(String accessed) {
				strAccess.add(accessed);
			}
		};
		for (String val : vals) {
			c.set(val);
			c.get();
			c.get();
		}
		assertEquals(strAccess,
				Arrays.asList(new String[] { "a", "a", "b", "b", "c", "c" }));
	}

}
