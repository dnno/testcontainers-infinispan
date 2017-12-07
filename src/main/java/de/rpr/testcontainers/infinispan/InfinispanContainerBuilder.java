package de.rpr.testcontainers.infinispan;

public interface InfinispanContainerBuilder {

  InfinispanContainerExposedPortsBuilder expose();

  InfinispanContainer build();
}
