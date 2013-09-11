package jcache;

import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;

import static org.junit.Assert.*;

public class BasicCacheApiTest extends AbstractBenchmark {

   static int REPS = 10000;
   static CacheManager cacheManager;
   static Cache<String, FootballSquad> footballCache;

   @BeforeClass
   public static void beforeClass() {
      cacheManager = Caching.getCachingProvider().getCacheManager();
      footballCache = cacheManager.configureCache("fooball",
            new MutableConfiguration<String, FootballSquad>()
                  .setStoreByValue(false));
   }

   @Test
   public void testSquadPlayers() throws InterruptedException {
      for (int i = 0; i < REPS; i++) {
         String id = "Brazil-WorldCup1982";
         FootballSquad squad;

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
