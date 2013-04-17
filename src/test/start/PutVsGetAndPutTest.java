package jcache;

import org.junit.BeforeClass;
import org.junit.Test;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.SimpleConfiguration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.jgroups.util.Util.assertTrue;

/**
 * // TODO: Document this
 *
 * @author Galder Zamarreño
 * @since // TODO
 */
public class PutVsGetAndPutTest {

   static String id = "Brazil-WorldCup1982";

   static CacheManager cacheManager;
   static Cache<String, FootballSquad> footballCache;

   static ConcurrentMap<String, FootballSquad> footballMap =
         new ConcurrentHashMap<String, FootballSquad>();

   @BeforeClass
   public static void beforeClass() {
      cacheManager = Caching.getCacheManager();
      cacheManager.configureCache("football",
            new SimpleConfiguration<String, FootballSquad>().setStoreByValue(false));
      footballCache = cacheManager.getCache("football");
   }

   @Test
   public void testConcurrentMap() {
      FootballSquad previousValue =
            footballMap.put(id, Database.squadPlayers(id));
      assertTrue(null == previousValue);

      previousValue =
            footballMap.put(id, Database.squadPlayers(id));
      assertTrue(null != previousValue);
   }

   @Test
   public void testCache() {

   }

}
