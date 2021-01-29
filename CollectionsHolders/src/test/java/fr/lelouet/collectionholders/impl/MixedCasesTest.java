package fr.lelouet.collectionholders.impl;

import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.lelouet.collectionholders.impl.collections.ObsMapHolderImpl;
import fr.lelouet.collectionholders.impl.numbers.ObsIntHolderImpl;
import fr.lelouet.collectionholders.interfaces.numbers.ObsIntHolder;

public class MixedCasesTest {

	@Test(timeOut = 500)
	public void testMixedDate() {

		Map<Integer, Integer> underlying = new HashMap<>();
		ObsMapHolderImpl<Integer, Integer> skills = new ObsMapHolderImpl<>();
		ObsIntHolder skill1 = skills.at(3406, 0).mapInt(i -> i);
		ObsIntHolder skill2 = skills.at(24624, 0).mapInt(i -> i);
		ObsIntHolder totalSlots = skill1.add(skill2).add(1);

		ObsIntHolder jobs = new ObsIntHolderImpl(3);
		ObsIntHolder corpJobs = new ObsIntHolderImpl(3);
		ObsIntHolder totalJobs = jobs.add(corpJobs);

		ObsIntHolder researchSlots = totalSlots.sub(totalJobs);

		underlying.put(3406, 5);
		underlying.put(24624, 4);
		skills.set(underlying);

		Assert.assertEquals(researchSlots.get(), (Integer) 4);
	}

}
