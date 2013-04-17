package jcache;

import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.SimpleConfiguration;

public class BasicCacheApiTest extends AbstractBenchmark {

   static int REPS = 10000;

   // Snippet 1: Cache and CacheManager definitions
   static CacheManager cacheManager;
   static Cache<String, FootballSquad> footballCache;

   @BeforeClass
   public static void beforeClass() {
      // Snippet 2: assign CacheManager and caches
      cacheManager = Caching.getCacheManager();
      SimpleConfiguration configuration = new SimpleConfiguration();

      // Snippet 4: use store by reference
      configuration.setStoreByValue(false);

      // Snippet 2: assign CacheManager and caches
      cacheManager.configureCache("football", configuration);
      footballCache = cacheManager.getCache("football");
   }

   @Test
   public void testSquadPlayers() {
      for (int i = 0; i < REPS; i++) {
         String id = "Brazil-WorldCup1982";
         FootballSquad squad;

         // Snippet 3: use cache to check and store football squad
         if (footballCache.containsKey(id)) {
            squad = footballCache.get(id);
         } else {
            squad = Database.squadPlayers(id);
            footballCache.put(id, squad);
         }

         use(squad); // do something with result
      }
   }

   private void use(FootballSquad squad) {
      // Do something with result...
      if (squad.hashCode() == System.nanoTime())
         System.out.print(" ");
   }

}
