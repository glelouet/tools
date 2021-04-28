package fr.lelouet.tools.webfetchers.icndb;

import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.springframework.web.reactive.function.client.WebClient;

import fr.lelouet.tools.holders.cache.URIBasedCache;
import fr.lelouet.tools.holders.impl.ObjHolderSimple;
import fr.lelouet.tools.holders.interfaces.ObjHolder;
import fr.lelouet.tools.webfetchers.WebFetcher;

public class ICNDBService {

	private ICNDBService() {
	}

	public static final ICNDBService INSTANCE = new ICNDBService();

	private WebClient wc = WebClient.create();

	/**
	 * default to 2min delay for refresh
	 */
	private URIBasedCache<EIcndb, ObjHolder<EIcndb>> cacheJokes = WebFetcher
			.cacheSync(new ScheduledThreadPoolExecutor(10), wc, EIcndb.class, ObjHolderSimple::new, h -> h, 120000l);

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
		return new ObjHolderSimple<>(wc.get().uri("").retrieve().toEntity(EIcndb.class).block().getBody());
	}

}
