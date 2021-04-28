package fr.lelouet.tools.webfetchers.icndb;

/**
 * an entry from the http://www.icndb.com/api/ API.
 *
 * @author glelouet
 *
 */
public class EIcndb {

	public String type;

	public Value value;

	public static class Value {
		public int id;
		public String joke;
		public String[] categories;
	}

}
