# Testcontainers Infinispan

The [Testcontainers](https://www.testcontainers.org/) library provides container implementations 
for a few different docker containers that you might want to use during integration tests. You can always
use a generic container to launch other images. This repository contains an implementation of a container
for the Infinispan cache server. It provides an API that is aimed to help you configure the container.

At the moment this is a rudimentary implementation and by no means complete. It only supports the [Hotrod protocol](http://infinispan.org/docs/stable/user_guide/user_guide.html#hot_rod_protocol)
to connect to the Infinispan server, for example.

Feel free to suggest changes!

# Usage

## Instantiation of the Infinispan container

Here's simple example how you can use the `InfinispanContainer`.

```
@ClassRule
public static InfinispanContainer infinispan = new InfinispanContainer()
          .withProtocolVersion(ProtocolVersion.PROTOCOL_VERSION_26);
```
## Cache creation

You can create simple local caches that need to be available for your tests. If you run an up-to-date Infinispan container (>9.1.0) then caches can be created
using the API of the `RemoteCacheManager` provided by the Infinispan client library. Simple configure some caches that the `InfinispanContainer` should create for you.
They will automatically be created once the container has started.

```
new InfinispanContainer()
          .withProtocolVersion(ProtocolVersion.PROTOCOL_VERSION_26)
          .withCaches("testCache");
``` 

If you run an Infinispan server version prior to `9.1.0`, you can link a configuration file that contains the necessary caches into the container:

```
new InfinispanContainer("jboss/infinispan-server:9.0.3.Final")
      .withProtocolVersion(ProtocolVersion.PROTOCOL_VERSION_26)
      .withStandaloneConfiguration("infinispan-standalone.xml")
```

## CacheManager retrieval

If you want, you can retrieve a `RemoteCacheManager` from the container:
```
infinispan.getCacheManager()
```

For general usage info on Testcontainers please look at the examples of the project.
