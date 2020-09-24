package fr.lelouet.collectionholders.impl;

import org.testng.Assert;

import fr.lelouet.collectionholders.interfaces.numbers.ObsIntHolder;
import fr.lelouet.tools.lambdaref.GCManage;

public class ObsObjHolderSimpleTest {

	// TODO fix
	// @Test(timeOut = 10000)
	public void testWeakRef() {
		ObsObjHolderSimple<String> test = new ObsObjHolderSimple<>("a");
		internalCall(test);
		GCManage.force();
		test.set("c");
		Assert.assertEquals(test.followers(), 0);
	}


	protected void internalCall(ObsObjHolderSimple<String> test) {
		for (int i = 1; i <= 1; i++) {
			ObsIntHolder length = test.mapInt(String::length);
			Assert.assertEquals(length.get(), (Integer) 1);
			Assert.assertEquals(test.followers(), i);
		}
	}

}
