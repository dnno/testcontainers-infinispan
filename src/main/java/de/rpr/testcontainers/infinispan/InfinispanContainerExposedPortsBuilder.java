package de.rpr.testcontainers.infinispan;

public interface InfinispanContainerExposedPortsBuilder {

  InfinispanContainerBuilder hotrod();

  InfinispanContainerBuilder hotrod(int port);

  InfinispanContainerBuilder rest();

  InfinispanContainerBuilder rest(int port);

}
