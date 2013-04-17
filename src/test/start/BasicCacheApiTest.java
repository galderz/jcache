package jcache;

import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import org.junit.BeforeClass;
import org.junit.Test;

public class BasicCacheApiTest extends AbstractBenchmark {

   static int REPS = 10000;

   @BeforeClass
   public static void beforeClass() {
   }

   @Test
   public void testSquadPlayers() throws InterruptedException {
      for (int i = 0; i < REPS; i++) {
         String id = "Brazil-WorldCup1982";
         FootballSquad squad;
         squad = Database.squadPlayers(id);
         use(squad); // do something with result
      }
   }

   private void use(FootballSquad squad) {
      // Do something with result...
      if (squad.hashCode() == System.nanoTime())
         System.out.print(" ");
   }

}
