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

   }

   public static class TestClassLoader extends ClassLoader {
     public TestClassLoader(ClassLoader parent) {
        super(parent);
     }
   }

   @Test
   public void testSquadPlayers() {
   }

}