Pre-requisites
==============

* Increase font size of text and console!
* Set up ad-hoc network!

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

6. Assign each of the cache managers to ``