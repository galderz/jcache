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

/**
 * // TODO: Document this
 *
 * @author Galder Zamarreño
 * @since // TODO
 */
public class TransactionalCacheTest {

   static CacheManager cacheManager;
   // A query, mapped to the ids returned
   static Cache<String, List<Integer>> queryCache;
   static String query = "select * from person";

   @BeforeClass
   public static void beforeClass() {
      cacheManager = Caching.getCacheManager();
      SimpleConfiguration configuration = new SimpleConfiguration();
      // Snippet 2: configure cache
      configuration.setTransactions(IsolationLevel.READ_COMMITTED, Mode.LOCAL);
      cacheManager.configureCache("query", configuration);
      queryCache = cacheManager.getCache("query");
   }

   @Test
   public void testTransactions() {
      // Snippet 2: get a transaction manager and call normally
      // Snippet 3: throw an exception from within the transaction, catch it and verify contents
      TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();
      try {
         withTx(tm, new Callable<Object>() {
            @Override
            public Object call() throws Exception {
               queryCache.put(query, new ArrayList<Integer>(Arrays.asList(1, 2, 3)));
               throw new Exception("Something went wrong...");
               // return null;
            }
         });
      } catch (Exception e) {
         // Expected
      }

      List<Integer> ids = queryCache.get(query);
      System.out.println(ids); // expect null
   }

   @Test
   public void testInvokeProcessor() {
      // Snippet 4: add to the squad
      queryCache.put(query, new ArrayList<Integer>(Arrays.asList(1, 2, 3)));

      try {
         queryCache.invokeEntryProcessor(query, new Cache.EntryProcessor<String, List<Integer>, Object>() {
            @Override
            public Object process(Cache.MutableEntry<String, List<Integer>> entry) {
               List<Integer> ids = entry.getValue();
               ids.add(4);
               entry.setValue(ids);
               throw new RuntimeException();
            }
         });
      } catch (Exception e) {
         // Expected
      }

      List<Integer> ids = queryCache.get(query);
      System.out.println(ids);
   }

   // Snippet 1: running operations within a transaction
   /**
    * Call an operation within a transaction. This method guarantees that the
    * right pattern is used to make sure that the transaction is always either
    * committed or rollbacked.
    *
    * @param tm transaction manager
    * @param c callable instance to run within a transaction
    * @param <T> type of callable return
    * @return returns whatever the callable returns
    */
   static <T> T withTx(TransactionManager tm, Callable<T> c) {
      try {
         tm.begin();
         try {
            return c.call();
         } catch (Exception e) {
            tm.setRollbackOnly();
            throw e;
         } finally {
            if (tm.getStatus() == Status.STATUS_ACTIVE) tm.commit();
            else tm.rollback();
         }
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

}
