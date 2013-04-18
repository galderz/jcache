package jcache;

import org.junit.BeforeClass;
import org.junit.Test;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.SimpleConfiguration;
import javax.cache.transaction.IsolationLevel;
import javax.cache.transaction.Mode;
import javax.transaction.Status;
import javax.transaction.TransactionManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import static org.junit.Assert.*;

/**
 * // TODO: Document this
 *
 * @author Galder Zamarreño
 * @since // TODO
 */
public class TransactionalCacheTest {

   static CacheManager cacheManager;
   static Cache<String, List<Integer>> queryCache;
   static String query = "select * from person";

   @BeforeClass
   public static void beforeClass() {
      cacheManager = Caching.getCacheManager();
   }

   @Test
   public void testTransactions() {

   }

}
