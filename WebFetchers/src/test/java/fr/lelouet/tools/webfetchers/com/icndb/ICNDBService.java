package fr.lelouet.tools.webfetchers.com.icndb;

import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.springframework.web.reactive.function.client.WebClient;

import fr.lelouet.tools.holders.cache.URIBasedCache;
import fr.lelouet.tools.holders.impl.ObjHolderSimple;
import fr.lelouet.tools.holders.interfaces.ObjHolder;
import fr.lelouet.tools.webfetchers.WebFetcher;
import fr.lelouet.tools.webfetchers.com.icndb.ICNDBService.EIcndb.Value;

/**
 * service to get data from http://www.icndb.com/api/ API
 *
 * @author glelouet
 *
 */
public class ICNDBService {

	private ICNDBService() {
	}

	public static final ICNDBService INSTANCE = new ICNDBService();

	private final WebClient wc = WebClient.create();
	private final ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(10);

	// correct jokes path

	/**
	 * an entry from the http://www.icndb.com/api/ API.
	 *
	 * @author glelouet
	 *
	 */
	public static class EIcndb {

		public String type;

		public Value value;

		public static class Value {
			public int id;
			public String joke;
			public String[] categories;
		}

	}

	/**
	 * default to 2min delay for refresh
	 */
	private URIBasedCache<EIcndb, ObjHolder<EIcndb>> cacheJokes = WebFetcher
			.cacheSync(exec, wc, EIcndb.class, ObjHolderSimple::new, h -> h, 120000l);

	/**
	 * get a joke by its id
	 *
	 * @param id
	 * @return
	 */
	public ObjHolder<EIcndb> joke(int id) {
		return cacheJokes.get("http://api.icndb.com/jokes/" + id);
	}

	/**
	 * get a random joke
	 *
	 * @return
	 */
	public ObjHolder<EIcndb> joke() {
		return new ObjHolderSimple<>(
				wc.get().uri("http://api.icndb.com/jokes/random").retrieve().toEntity(EIcndb.class).block().getBody());
	}


	// jokes with missing "type" field

	/**
	 * an entry that is missing the type. Used for testing purpose
	 *
	 * @author glelouet
	 *
	 */
	public static class EIcndbNOTYPE {

		public Value value;

	}

	private URIBasedCache<EIcndbNOTYPE, ObjHolder<EIcndbNOTYPE>> cacheJokesNOTYPE = WebFetcher.cacheSync(exec, wc,
			EIcndbNOTYPE.class, ObjHolderSimple::new, h -> h, 120000l);

	/**
	 * get a joke by its id. invalid method
	 *
	 * @param id
	 * @return
	 */
	public ObjHolder<EIcndbNOTYPE> jokeNOTYPE(int id) {
		return cacheJokesNOTYPE.get("http://api.icndb.com/jokes/" + id);
	}

	// jokes with invalid value type (array instead of single item)

	/**
	 * an entry that has wrong type for value. Used for testing purpose
	 *
	 * @author glelouet
	 *
	 */
	public static class EIcndbWRONGVALUE {

		public Value[] value;

	}

	private URIBasedCache<EIcndbWRONGVALUE, ObjHolder<EIcndbWRONGVALUE>> cacheJokesWRONGVALUE = WebFetcher.cacheSync(exec,
			wc, EIcndbWRONGVALUE.class, ObjHolderSimple::new, h -> h, 120000l);

	/**
	 * get a joke by its id. Invalid method
	 *
	 * @param id
	 * @return
	 */
	public ObjHolder<EIcndbWRONGVALUE> jokeWRONGVALUE(int id) {
		return cacheJokesWRONGVALUE.get("http://api.icndb.com/jokes/" + id);
	}

}
