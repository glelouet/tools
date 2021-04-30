package fr.lelouet.tools.webfetchers.com.breakingbadapi;

import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.springframework.web.reactive.function.client.WebClient;

import fr.lelouet.tools.holders.cache.URIBasedCache;
import fr.lelouet.tools.holders.impl.ObjHolderSimple;
import fr.lelouet.tools.holders.interfaces.ObjHolder;
import fr.lelouet.tools.webfetchers.WebFetcher;

/**
 * service to access https://breakingbadapi.com/documentation
 *
 * @author glelouet
 *
 */
public class BreakingbadapiService {

	private BreakingbadapiService() {
	}

	public static final BreakingbadapiService INSTANCE = new BreakingbadapiService();

	private final WebClient wc = WebClient.create();
	private final ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(10);

	// characters

	public static class Character {
		public int char_id;
		public String name;
		public String birthday;
		public String[] occupation;
		public String img;
		public String status;
		public String nickname;
		public int[] appearance;
		public String portrayed;
		public String category;
		public Object[] better_call_saul_appearance;
	}

	/**
	 * default to 2min delay for refresh
	 */
	private URIBasedCache<Character[], ObjHolder<Character[]>> cacheCharacters = WebFetcher.cacheSync(exec, wc,
			Character[].class, ObjHolderSimple::new, h -> h, 120000l);

	/**
	 * get a character by its id
	 *
	 * @param id
	 * @return
	 */
	public ObjHolder<Character[]> character(int id) {
		return cacheCharacters.get("https://breakingbadapi.com/api/characters/" + id);
	}

}
