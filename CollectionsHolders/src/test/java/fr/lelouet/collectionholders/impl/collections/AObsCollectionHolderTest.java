package fr.lelouet.collectionholders.impl.collections;

import java.util.Arrays;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.lelouet.collectionholders.interfaces.collections.ObsListHolder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class AObsCollectionHolderTest {

	@Test(timeOut = 500)
	public void testFlattenNotReceived() {
		ObservableList<ObsListHolder<Character>> underlying = FXCollections.observableArrayList();
		ObsListHolderImpl<ObsListHolder<Character>> test = new ObsListHolderImpl<>(underlying);
		ObsListHolder<Character> flattened = test.flatten(l -> l);

		ObservableList<Character> charList = FXCollections.observableArrayList('c', 'h', 'a', 'r');
		ObsListHolderImpl<Character> charObs = new ObsListHolderImpl<>(charList);

		underlying.add(charObs);
		test.dataReceived();
		charObs.dataReceived();

		Assert.assertEquals(flattened.get(), Arrays.asList('c', 'h', 'a', 'r'));
	}

	@Test(timeOut = 500)
	public void testFlattenReceived() {
		ObservableList<ObsListHolder<Character>> underlying = FXCollections.observableArrayList();
		ObsListHolderImpl<ObsListHolder<Character>> test = new ObsListHolderImpl<>(underlying);

		ObservableList<Character> charList = FXCollections.observableArrayList('c', 'h', 'a', 'r');
		ObsListHolderImpl<Character> charObs = new ObsListHolderImpl<>(charList);

		underlying.add(charObs);
		test.dataReceived();
		charObs.dataReceived();

		ObsListHolder<Character> flattened = test.flatten(l -> l);
		Assert.assertEquals(flattened.get(), Arrays.asList('c', 'h', 'a', 'r'));

		charList.set(3, 't');
		charObs.dataReceived();
		Assert.assertEquals(flattened.get(), Arrays.asList('c', 'h', 'a', 't'));

	}

	@Test(timeOut = 500)
	public void testConcat() {
		ObservableList<ObsListHolder<Character>> underlying = FXCollections.observableArrayList();
		ObsListHolderImpl<ObsListHolder<Character>> test = new ObsListHolderImpl<>(underlying);

		ObservableList<Character> list1 = FXCollections.observableArrayList('c', 'h', 'a', 'r');
		ObsListHolderImpl<Character> list1Obs = new ObsListHolderImpl<>(list1);
		list1Obs.dataReceived();

		ObservableList<Character> list2 = FXCollections.observableArrayList('i', 's', 'm');
		ObsListHolderImpl<Character> list2Obs = new ObsListHolderImpl<>(list2);
		list2Obs.dataReceived();

		underlying.add(list1Obs);
		underlying.add(list2Obs);
		test.dataReceived();

		ObsListHolder<Character> flattened = test.flatten(l -> l);
		Assert.assertEquals(flattened.get(), Arrays.asList('c', 'h', 'a', 'r', 'i', 's', 'm'));

		list2.setAll('a', 'c', 't', 'e', 'r');
		list2Obs.dataReceived();

		Assert.assertEquals(flattened.get(), Arrays.asList('c', 'h', 'a', 'r', 'a', 'c', 't', 'e', 'r'));
	}

	@Test(timeOut = 500)
	public void testRemove() {
		ObservableList<ObsListHolder<Character>> underlying = FXCollections.observableArrayList();
		ObsListHolderImpl<ObsListHolder<Character>> test = new ObsListHolderImpl<>(underlying);

		ObservableList<Character> list1 = FXCollections.observableArrayList('c', 'h', 'a', 'r');
		ObsListHolderImpl<Character> list1Obs = new ObsListHolderImpl<>(list1);
		list1Obs.dataReceived();

		ObservableList<Character> list2 = FXCollections.observableArrayList('i', 's', 'm');
		ObsListHolderImpl<Character> list2Obs = new ObsListHolderImpl<>(list2);
		list2Obs.dataReceived();

		underlying.add(list1Obs);
		underlying.add(list2Obs);
		test.dataReceived();

		ObsListHolder<Character> flattened = test.flatten(l -> l);
		Assert.assertEquals(flattened.get(), Arrays.asList('c', 'h', 'a', 'r', 'i', 's', 'm'), "got" + flattened.get());

		underlying.remove(0);
		test.dataReceived();

		Assert.assertEquals(flattened.get(), Arrays.asList('i', 's', 'm'), "got " + flattened.get());
	}

}
