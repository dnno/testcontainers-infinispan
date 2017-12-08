package de.rpr.testcontainers.infinispan;

import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.LogMessageWaitStrategy;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class InfinispanContainerFactory
    extends GenericContainer<InfinispanContainerFactory>
    implements InfinispanContainer<InfinispanContainerFactory>, InfinispanContainerExposedPortsBuilder, InfinispanContainerBuilder,
    InfinispanContainerStandaloneBuilder {

  private static final String IMAGE_NAME = "jboss/infinispan-server:";

  @Override
  public InfinispanContainerBuilder configurationFile(final String classPathResourceName) {
    URL resourceUrl = this.getClass().getClassLoader().getResource(classPathResourceName);
    if (resourceUrl == null) {
      throw new IllegalArgumentException("Cannot read configuration resource");
    }
    return withFileSystemBind(new File(resourceUrl.getFile()).getAbsolutePath(),
        "/opt/jboss/infinispan-server/standalone/configuration/standalone.xml",
        BindMode.READ_ONLY);
  }

  @Override
  public InfinispanContainerBuilder and() {
    return this;
  }

  private enum Protocol {
    HOTROD(11222),
    REST(8080);

    private final int defaultPort;

    Protocol(final int defaultPort) {
      this.defaultPort = defaultPort;
    }
  }

  private final Map<Protocol, Integer> exposedPorts = new HashMap<>();

  private InfinispanContainerFactory(final String dockerImageName) {
    super(dockerImageName);
  }

  public static InfinispanContainerStandaloneBuilder standalone() {
    return standalone("9.1.3.Final");
  }

  public static InfinispanContainerStandaloneBuilder standalone(final String version) {
    InfinispanContainer container = new InfinispanContainerFactory(IMAGE_NAME + version);
    container.withCommand("standalone");
    return (InfinispanContainerStandaloneBuilder) container;
  }

  @Override
  public InfinispanContainerExposedPortsBuilder expose() {
    return this;
  }

  @Override
  public InfinispanContainerBuilder hotrod() {
    return hotrod(Protocol.HOTROD.defaultPort);
  }

  @Override
  public InfinispanContainerBuilder hotrod(final int port) {
    return exposePort(Protocol.HOTROD, port);
  }

  @Override
  public InfinispanContainerBuilder rest() {
    return rest(Protocol.REST.defaultPort);
  }

  @Override
  public InfinispanContainerBuilder rest(final int port) {
    return exposePort(Protocol.REST, port);
  }

  private InfinispanContainerFactory exposePort(final Protocol service, final int port) {
    exposedPorts.put(service, port);
    return this;
  }

  @Override
  public InfinispanContainer build() {
    return waitingFor(new LogMessageWaitStrategy().withRegEx(".*Infinispan Server.*started in.*\\s"));
  }

}
