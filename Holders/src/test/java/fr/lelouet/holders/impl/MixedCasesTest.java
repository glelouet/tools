package fr.lelouet.holders.impl;

import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.lelouet.holders.impl.collections.MapHolderImpl;
import fr.lelouet.holders.impl.numbers.IntHolderImpl;
import fr.lelouet.holders.interfaces.numbers.IntHolder;

public class MixedCasesTest {

	@Test(timeOut = 500)
	public void testMixedDate() {

		Map<Integer, Integer> underlying = new HashMap<>();
		MapHolderImpl<Integer, Integer> skills = new MapHolderImpl<>();
		IntHolder skill1 = skills.at(3406, 0).mapInt(i -> i);
		IntHolder skill2 = skills.at(24624, 0).mapInt(i -> i);
		IntHolder totalSlots = skill1.add(skill2).add(1);

		IntHolder jobs = new IntHolderImpl(3);
		IntHolder corpJobs = new IntHolderImpl(3);
		IntHolder totalJobs = jobs.add(corpJobs);

		IntHolder researchSlots = totalSlots.sub(totalJobs);

		underlying.put(3406, 5);
		underlying.put(24624, 4);
		skills.set(underlying);

		Assert.assertEquals(researchSlots.get(), (Integer) 4);
	}

}
