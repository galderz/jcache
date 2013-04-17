package jcache;

import org.infinispan.marshall.AdvancedExternalizer;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Run with: -Djava.net.preferIPv4Stack=true -Djgroups.bind_addr=127.0.0.1
 *
 * @author Galder Zamarreño
 */
public class ReplicatedCacheTest {

   static CacheManager cacheManager1;
   static CacheManager cacheManager2;

   static Cache<String, FootballSquad> footballCache1;
   static Cache<String, FootballSquad> footballCache2;

   @BeforeClass
   public static void beforeClass() {
      // Snippet 1: We want two cache managers, with a clustered configuration, but must be different!
      ClassLoader tccl = Thread.currentThread().getContextClassLoader();
      cacheManager1 = Caching.getCacheManager(new TestClassLoader(tccl), "infinispan-cluster.xml");
      cacheManager2 = Caching.getCacheManager(new TestClassLoader(tccl), "infinispan-cluster.xml");

      footballCache1 = cacheManager1.getCache("footballCache");
      footballCache2 = cacheManager2.getCache("footballCache");
   }

   @Test
   public void testSquadPlayers() {
      // Snippet 4: test replication
      String id = "Brazil-WorldCup1982";
      footballCache1.put(id, Database.squadPlayers(id));
      FootballSquad replicatedSquad = footballCache2.get(id);
      System.out.println(replicatedSquad);
   }

   // Snippet 2: Custom class loader for clustered test
   public static class TestClassLoader extends ClassLoader {
      public TestClassLoader(ClassLoader parent) {
         super(parent);
      }
   }

   // Snippet 3: Add externalizers...
   @SuppressWarnings("unused")
   public static class FootballPlayerExternalizer implements AdvancedExternalizer<FootballPlayer> {
      @Override
      public Set<Class<? extends FootballPlayer>> getTypeClasses() {
         return Collections.<Class<? extends FootballPlayer>>singleton(FootballPlayer.class);
      }

      @Override
      public Integer getId() {
         return 88;
      }

      @Override
      public void writeObject(ObjectOutput output, FootballPlayer object) throws IOException {
         output.writeUTF(object.getName());
      }

      @Override
      public FootballPlayer readObject(ObjectInput input) throws IOException, ClassNotFoundException {
         String name = input.readUTF();
         return new FootballPlayer(name);
      }
   }

   @SuppressWarnings("unused")
   public static class FootballSquadExternalizer implements AdvancedExternalizer<FootballSquad> {
      @Override
      public Set<Class<? extends FootballSquad>> getTypeClasses() {
         return Collections.<Class<? extends FootballSquad>>singleton(FootballSquad.class);
      }

      @Override
      public Integer getId() {
         return 89;
      }

      @Override
      public void writeObject(ObjectOutput output, FootballSquad object) throws IOException {
         output.writeObject(object.getSquad());
      }

      @Override
      @SuppressWarnings("unchecked")
      public FootballSquad readObject(ObjectInput input) throws IOException, ClassNotFoundException {
         List<FootballPlayer> squad = (List<FootballPlayer>) input.readObject();
         return new FootballSquad(squad);
      }
   }

}