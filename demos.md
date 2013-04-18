Pre-requisites
==============

1. Increase font size of text and console!
2. Set up ad-hoc network!
3. Copy all java files from `src/main/start` to `src/main/java/jcache`
4. Copy all xml files from `src/main/start` to `src/main/resources`

Demo 1
======

1. Explain: BasicCacheApiTest is a test that retrieves a football squad from
a fake database. We're looking for the Brazilian football squad of 1982.

2. Run it (no system properties) and check that performance is not very
good :(, i.e. round: 0.95

3. What is wrong with it? Run with profiler (sampling and VM keep alive)
to see where time is going...

4. With profiler `87.5% - 15,541 ms jcache.Database.squadPlayers` should be
the hotspot

5. Add JSR-107 based caching, show Maven dependencies.

6. Type: two references for `javax.cache.CacheManager` and `javax.cache.Cache`
called `cacheManager` and `footballCache`

7. In `beforeClass()`, assign `Caching.getCacheManager()` to `cacheManager`.
This static method is used to retrieve the system JSR-107 cache manager, which
underneath uses ServiceLoader to detect the JSR-107 implementation to use.
Since we've imported Infinispan's JSR-107 implementation, it will use that.

8. Configure `footballCache` calling
`cacheManager.configureCache("football", new SimpleConfiguration<String, FootballSquad>())`

9. Retrieve `football` cache from cache manager via
`footballCache = cacheManager.getCache("football")`

10. Add an if/else statement to check if the football cache contains a
football squad for the id: `footballCache.containsKey(id)`

11. If found, assign it to squad: `squad = footballCache.get(id);`

12. Only go to database if not present: `squad = Database.squadPlayers(id)`,
and put the result in the cache `footballCache.put(id, squad);`

13. Try running `BasicCacheApiTest` now

14. Throws an error, which is a bit cryptic right now:
> org.infinispan.marshall.NotSerializableException: Object of type class jcache.FootballSquad expected to be marshallable

15. By default, JSR-107 mandates caches to be configured with storeByValue,
that means that object state mutations outside of operations to the cache,
won't have an impact in the objects stored in the cache. Infinispan has so
far implemented this using serialization/marshalling to make copies to store
in the cache, and that way adhere to the spec. However, we can disable it:
`new SimpleConfiguration<String, FootballSquad>()).setStoreByValue(false)`

16. Run `BasicCacheApiTest` again, it show run very fast, i.e. round: 0.01

Demo 2
======

1. Open `ConcurrentMap` and `Cache` classes on IDE

2. Compare `putIfAbsent`, conditional remove and conditional replace. In
putIfAbsent CM returns previous value, whereas in Cache it returns a boolean.

3. Notice how Cache has two basic put methods: `void put` and `V getAndPut`,
whereas ConcurrentMap has a single put: `V put`. Let's see them in action

4. In PutVsGetAndPutTest, we have a test that exercises ConcurrentMap.put()
call, verifying how the first time something it's store, it returns null, and
the second time, it returns a non-null value.

5. Let's try to do the same with a Cache (cache manager and caches are
already set up exactly the same as in the previous demo), add
`footballCache.put(id, Database.squadPlayers(id))`

6. To assert the same thing as in the concurrent map case, we need to use
`getAndPut`, so add:
    assertTrue(null == footballCache.getAndPut(id, Database.squadPlayers(id)));
    assertTrue(null != footballCache.getAndPut(id, Database.squadPlayers(id)));

7. You might wonder, why on earth do we need two methods??
   - The problem with CM's put is that it forces implementors to calculate
   what the previous value was.
   - As mentioned earlier, caches have been designed to more easily
   distribute, or provide secondary storage for them.
   - Hence, in some occasions, calculation of the previous value might
   require retrieving the previous value from another node, or from storage,
   which could be expensive.
   - Having the two put operations allows for flexibility, for situations
   where previous value is not required.

8. Other get* methods exist: `getAndRemove` and `getAndReplace` for exact
same reasons.

9. Cache has no key set, no values, no entry set operations, no size operation.
All these operations could be expensive to compute in a distributed cache,
i.e. what's the size of the cache? the size of the cache available locally?
or the total size of the cache in the entire cluster?...etc

Demo 3
======

1. ReplicatedCacheTest is a test that's gonna show how to run distributed
caches with JCache.

2. To run this part, it requires Infinispan 5.3.0-SNAPSHOT which fixes a bug
that stopped this test from working.

3. To create a distributed jcaches, we need to go beyond the basic
configuration provided by JCache, and so we need to pass an Infinispan
specific configuration file where we define how these caches are going to
communicate with each other. Show contents of `infinispan-cluster.xml`

4. If we want to use the standard JCache API to create the cache managers and
get them to form a cluster, we either: duplicate the configuration file and
create two files with the same config, or we assign a different class loader
to each cache manager.

5. Press Ctrl+J and select snippet `jsr107-cluster-cacheloader`

6. Assign each of the cache managers to
`Caching.getCacheManager(new TestClassLoader(tccl), "infinispan-cluster.xml")`

7. Assign each of the caches: `cacheManager1.getCache("football")` and
`cacheManager2.getCache("football")`

8. Write code to store a football squad in the cache in node 1:
`footballCache1.put(id, Database.squadPlayers(id))`

9. Retrieve football squad from node 2 and print it out:
`FootballSquad squad = footballCache2.get(id)` and `System.out.println(replicatedSquad)`

10. Run ReplicatedCacheTest with system properties:
`-Djava.net.preferIPv4Stack=true -Djgroups.bind_addr=127.0.0.1`. It should fail
with `NotSerializableException`

11. In order to distribute data in a cluster, it needs to be marshallable.
There are different ways to do it in Infinispan, let's show you the
Externalizer method. Infinispan allows you to define a Externalizer for a
type without modifying the type's source code. This is very handy for
situations where source code is not available, or you cannot modify the
source code.

12. Go to end of ReplicatedCacheTest, press Ctrl+J and select snippet
`jsr107-cluster-externalizers`

13. Once the externalizers have been defined, Infinispan needs to be
configured with them, and we're gonna do it via XML. Go to
`infinispan-cluster.xml` and go to the end of global section.
Press Ctrl+J and select snippet `jsr107-cluster-xml-externalizers`

14. Try running ReplicatedCacheTest again, it should print out the squad.

Demo 4
======

1. Open TransactionalCacheTest, which is going to be used to test
JCache transactions integration.

2. A query cache is going to be defined, which given a String-based query,
it returns a list of integers, representing ids that the query matched.

3. JCache API enables you to verify whether a particular optional feature
is supported by the implementation. So, to be sure, let's verify that
Infinispan's JCache implementation supports transactions:
`assertTrue(cacheManager.isSupported(OptionalFeature.TRANSACTIONS))`

3. Configure the cache to use `READ_COMMITTED` isolation level, and local
transactions:
`cacheManager.configureCache("query", new SimpleConfiguration<String, List<Integer>>().setTransactions(IsolationLevel.READ_COMMITTED, Mode.LOCAL))`

4. You can also configure XA transactions when the cache will participate
in transactions with other XA resources.

5. Assign the query cache: `queryCache = cacheManager.getCache("query")`

6. To use transactions, we need a transaction manager and we're gonna use
JBoss TS's transaction manager. Press Ctrl+J and select
snippet `jsr107-transactions-transactionmanager`

7. To see transactions in action, we need to call cache operations within
a transaction, so that means that a transaction needs to be retrieved, call
begin, then commit/rollback...etc. For that, I'm using a helper method that
allows any operation to be called within a transaction. Go to the end of
TransactionalCacheTest, press `Ctrl+J` and select snippet
`jsr107-transactions-withTx`

9. Call `queryCache.put(query, new ArrayList<Integer>(Arrays.asList(1, 2, 3)))`
within a transaction.

10. Get the query results from the cache outside of a transaction
`queryCache.get(query)` and print results `System.out.println(ids)`.

11. Run TransactionalCacheTest, it should print `[1, 2, 3]`

12. Change implementation of testTransactions and throw an exception instead
of returning null at the end of the callable implementation:
`throw new Exception("Something went wrong...")`

13. Catch the exception, and ignore it.

14. Run TransactionalCacheTest, it should print `null`

15. To finish the presentation, I wanna show you that JCache comes an API
that allows you to do the same thing we just did but without using
transactions.

16. It's called `invokeProcessor` and it allows you to do compound operations
against a particular entry in the cache, giving you the possibility to mutate
it with exclusive access and in an atomic way. If an exception is thrown,
the changes are not applied.

17. Create a test method called `testInvokeProcessor()`

18. Annotate the method with `@Test`

19. Store a query and some results in the query cache:
`queryCache.put(query, new ArrayList<Integer>(Arrays.asList(1, 2, 3)))`

20. Call `queryCache.invokeEntryProcessor(query, new...`

21. In the implementation, take the list of ids and add a new id to the list,
and make sure you call setValue!
`List<Integer> ids = entry.getValue();
ids.add(4);
entry.setValue(ids);`

22. Get the query results from the cache outside of a transaction
`queryCache.get(query)` and print results `System.out.println(ids)`.

23. Execute test **method** `TransactionalCacheTest.testInvokeProcessor`
right-clicking on the test method click on `run...`

24. It should print: `[1, 2, 3, 4]`

25. Change implementation of invoke processor and throw an exception instead
of returning null at the end of the callable implementation:
`throw new RuntimeException("Something went wrong...")`

26. Catch the exception, and ignore it.

27. Run `TransactionalCacheTest.testInvokeProcessor` once again

28. The output should show: `[1, 2, 3]` because the changes in
invokeProcessor implementation were not applied as a result of the exception.
