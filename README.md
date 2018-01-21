# Testcontainers Infinispan

The [Testcontainers](https://www.testcontainers.org/) library provides container implementations 
for a few different docker containers that you might want to use during integration tests. You can always
use a generic container to launch other images. This repository contains an implementation of a container
for the Infinispan cache server. It provides an API that is aimed to help you configure the container.

At the moment this is a rudimentary implementation and by no means complete. It only supports the [Hotrod protocol](http://infinispan.org/docs/stable/user_guide/user_guide.html#hot_rod_protocol)
to connect to the Infinispan server, for example.

Further information can be found in these blog posts:

- [12-19-2017: Running an Infinispan Server using Testcontainers](https://blog.codecentric.de/en/2017/12/running-infinispan-server-using-testcontainers)
- [01-21-2018: Running a clustered Infinispan Server using Testcontainers](https://reinhard.codes/2018/01/21/running-an-infinispan-node-in-clustered-mode-using-testcontainers/)

Feel free to suggest changes!

# Usage

It's possible to run Infinispan in standalone or in clustered mode. See the integration tests contained in this repository
for detailed examples.

## Instantiation of the Infinispan containers

Here's a simple example how you can use the `StandaloneInfinispanContainer`.

```
@ClassRule
public static InfinispanContainer infinispan = new StandaloneInfinispanContainer();
```

If you want to run Infinispan as a one-node clustered instance, you can do it like this:

```
@ClassRule
public static InfinispanContainer infinispan = new ClusteredInfinispanContainer();
```

## Cache creation

You can create simple local caches that need to be available for your tests. If you run an up-to-date Infinispan container (>9.1.0) then caches can be created
using the API of the `RemoteCacheManager` provided by the Infinispan client library. Simple configure some caches that the `InfinispanContainer` should create for you.
They will automatically be created once the container has started.

```
new StandaloneInfinispanContainer(‚)
    .withCaches("testCache")
    .withProtocolVersion(ProtocolVersion.PROTOCOL_VERSION_26);
``` 

The `ClusteredInfinispanContainer` supports the same method.

If you run an Infinispan server version prior to `9.1.0`, you can link a configuration file that contains the necessary caches into the container:

```
new StandaloneInfinispanContainer("jboss/infinispan-server:9.0.3.Final")
    .withStandaloneConfiguration("infinispan-standalone.xml")
    .withProtocolVersion(ProtocolVersion.PROTOCOL_VERSION_26)
```

There's an equivalent for the `ClusteredInfinispanContainer`:

```
new ClusteredInfinispanContainer("jboss/infinispan-server:9.0.3.Final")
    .withClusteredConfiguration("infinispan-standalone.xml")
    .withProtocolVersion(ProtocolVersion.PROTOCOL_VERSION_26)
```

## CacheManager retrieval

If you want, you can retrieve a `RemoteCacheManager` from the container:

```
infinispan.getCacheManager()
```

For general usage info on Testcontainers please look at the examples of the project.
