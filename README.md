# Testcontainers Infinispan

The [Testcontainers](https://www.testcontainers.org/) library provides container implementations 
for a few different docker containers that you might want to use during integration tests. You can always
use a generic container to launch other images. This repository contains an implementation of a container
for the Infinispan cache server. It provides an API that is aimed to help you configure the container.

At the moment this is a rudimentary implementation and by no means complete. Feel free to suggest changes!

# Usage

For general usage info on Testcontainers please look at the examples of the project.

```
@ClassRule
public static InfinispanContainer infinispan = InfinispanContainerFactory
      .standalone("9.1.3.Final")
      .configurationFile("infinispan-standalone.xml")
      .expose()
      .hotrod()
      .build();
```