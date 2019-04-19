package fr.lelouet.tools.sorters;

import java.util.*;
import java.util.Map.Entry;

/** sort data that is linked to a double value, in asc order */
public class MapSorter<T> {

	public static <T> List<T> sort(Map<T, Double> map) {
		List<Entry<T, Double>> list = new ArrayList<Entry<T, Double>>(map
				.entrySet());
		Collections.sort(list, new Comparator<Entry<T, Double>>() {
			@Override
			public int compare(Entry<T, Double> o1, Entry<T, Double> o2) {
				double diff = o1.getValue() - o2.getValue();
				return diff < 0 ? -1 : diff == 0 ? 0 : 1;
			}
		});
		List<T> ret = new ArrayList<T>();
		for (Entry<T, Double> e : list) {
			ret.add(e.getKey());
		}
		return ret;
	}

}
