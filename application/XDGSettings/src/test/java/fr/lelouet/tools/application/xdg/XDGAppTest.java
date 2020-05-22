package fr.lelouet.tools.application.xdg;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.testng.Assert;
import org.testng.annotations.Test;

public class XDGAppTest {

	@Test
	public void testStream() {
		HashMap<String, String> p = new HashMap<>();
		XDGApp test = new XDGApp("app", p);
		p.put("homekey", "/home");
		p.put("dirkey", "/home2:/home3");

		List<String> filenames = test
				.streamPossibleFile("homekey", "nohome", "dirkey", "nohome2:nohome3", "files", "file1.txt")
				.collect(Collectors.toList());
		Assert.assertEquals(filenames,
				Arrays.asList("/home/app/files/file1.txt", "/home2/app/files/file1.txt", "/home3/app/files/file1.txt"));

		filenames = test.streamPossibleFile("nohomekey", "nohome", "nodirkey", "nohome2:nohome3", "files", "file1.txt")
				.collect(Collectors.toList());
		Assert.assertEquals(filenames,
				Arrays.asList("nohome/app/files/file1.txt", "nohome2/app/files/file1.txt", "nohome3/app/files/file1.txt"));

		filenames = test.streamPossibleFile("homekey", "nohome", "nodirkey", "nohome2:nohome3")
				.collect(Collectors.toList());
		Assert.assertEquals(filenames, Arrays.asList("/home/app", "nohome2/app", "nohome3/app"));
	}

}
