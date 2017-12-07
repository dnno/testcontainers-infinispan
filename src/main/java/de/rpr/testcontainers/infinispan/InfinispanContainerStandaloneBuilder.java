package de.rpr.testcontainers.infinispan;

public interface InfinispanContainerStandaloneBuilder {

  InfinispanContainerBuilder configurationFile(String classPathResourceName);

  InfinispanContainerBuilder and();

}
