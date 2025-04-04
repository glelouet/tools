package fr.lelouet.tools.application.yaml;

import java.util.Collection;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

public class CleanRepresenterTest {

	public static class TestClass {
		public int b = 1;
		public int d = 2;
		public int a = 3;
		public int c = 4;
		public Integer e = null;
		public Collection<Integer> f = null;
		public Collection<Integer> g = List.of();
	}

	@Test
	public void testSort() {
		TestClass t = new TestClass();
		String dump = YAMLTools.cleanBlock().dump(t);
		Assert.assertEquals(dump, """
a: 3
b: 1
c: 4
d: 2
""");
//		String[] lines = dump.split("\\n");
//		for (String l : lines) {
//			System.err.println(l);
//		}
	}

}
