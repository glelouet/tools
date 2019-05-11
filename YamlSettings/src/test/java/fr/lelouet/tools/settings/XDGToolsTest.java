package fr.lelouet.tools.settings;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.testng.Assert;
import org.testng.annotations.Test;

public class XDGToolsTest {

	@Test
	public void testStream() {
		Properties p = new Properties();
		XDGTools test = new XDGTools("app", p);
		p.setProperty("k1", "/home");
		p.setProperty("k2", "/home2:/home3");

		List<String> filenames = test.streamPossibleFile("k1", "nohome", "k2", "nohome2:nohome3", "files", "file1.txt")
				.collect(Collectors.toList());
		Assert.assertEquals(filenames,
				Arrays.asList("/home/app/files/file1.txt", "/home2/app/files/file1.txt", "/home3/app/files/file1.txt"));

		filenames = test.streamPossibleFile("k1b", "nohome", "k2b", "nohome2:nohome3", "files", "file1.txt")
				.collect(Collectors.toList());
		Assert.assertEquals(filenames,
				Arrays.asList("nohome/app/files/file1.txt", "nohome2/app/files/file1.txt", "nohome3/app/files/file1.txt"));

		filenames = test.streamPossibleFile("k1", "nohome", "k2b", "nohome2:nohome3").collect(Collectors.toList());
		Assert.assertEquals(filenames, Arrays.asList("/home/app", "nohome2/app", "nohome3/app"));
	}

}
