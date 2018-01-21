package de.rpr.testcontainers.infinispan;

import com.github.dockerjava.api.command.InspectContainerResponse;
import org.infinispan.client.hotrod.ProtocolVersion;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.Configuration;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.exceptions.HotRodClientException;
import org.infinispan.client.hotrod.impl.transport.TransportFactory;
import org.junit.runner.Description;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.Wait;

import java.util.*;
import java.util.stream.IntStream;

@SuppressWarnings("ALL")
public abstract class InfinispanContainer<T extends InfinispanContainer<T>> extends GenericContainer<T> {

  private static final Set<ProtocolVersion> incompatibleProtocolVersions = new HashSet<>();

  static {
    incompatibleProtocolVersions.add(ProtocolVersion.PROTOCOL_VERSION_10);
    incompatibleProtocolVersions.add(ProtocolVersion.PROTOCOL_VERSION_11);
    incompatibleProtocolVersions.add(ProtocolVersion.PROTOCOL_VERSION_12);
    incompatibleProtocolVersions.add(ProtocolVersion.PROTOCOL_VERSION_13);
  }

  /*
   * An enumeration of the endpoints provided by Infinispan, that this container provides access to.
   */
  protected enum InfinispanEndpoints {
    HOTROD(11222);

    private final int protocolPort;

    InfinispanEndpoints(final int port) {
      this.protocolPort = port;
    }

    public int getProtocolPort() {
      return protocolPort;
    }

  }

  protected RemoteCacheManager cacheManager;

  private final String infinispanServerVersion;
  private final Collection<String> cacheNames = new ArrayList<>();
  private ProtocolVersion protocolVersion;

  private boolean isProtocolConflict() {
    return incompatibleProtocolVersions.contains(getProtocolVersion());
  }

  private boolean isMajorVersionConflict() {
    return IntStream.range(1, 8)
        .anyMatch(majorVersion -> getInfinispanServerVersion().startsWith(Integer.toString(majorVersion)));
  }

  private boolean isMinorVersionConflict() {
    boolean minorVersionConflict = false;
    if (getInfinispanServerVersion().startsWith("9.0")) {
      minorVersionConflict = true;
    }
    return minorVersionConflict;
  }

  public InfinispanContainer(final String imageName) {
    super(imageName);

    this.infinispanServerVersion = imageName.split(":")[1];

    withExposedPorts(Arrays.stream(InfinispanEndpoints.values())
        .map(endpoint -> endpoint.getProtocolPort()).toArray(Integer[]::new));

    this.waitStrategy = Wait.forListeningPort();
  }


  public InfinispanContainer withProtocolVersion(final ProtocolVersion protocolVersion) {
    this.protocolVersion = protocolVersion;
    return this;
  }

  @Override
  protected void finished(final Description description) {
    if (cacheManager != null) {
      cacheManager.stop();
    }
    super.finished(description);
  }

  /**
   * Retrieve the Hotrod endpoint address used to connect to the Infinispan instance inside the container.
   *
   * @return A String of the format [ipaddress]:[port]
   */
  public String getHotrodEndpointConnectionString() {
    return getContainerIpAddress() + ":" + getMappedPort(StandaloneInfinispanContainer.InfinispanEndpoints.HOTROD.protocolPort);
  }

  /**
   * Retrieve a preconfigured {@link org.infinispan.client.hotrod.RemoteCacheManager}.
   *
   * @return A cacheManager
   */
  public RemoteCacheManager getCacheManager() {
    return cacheManager;
  }

  protected ProtocolVersion getProtocolVersion() {
    return protocolVersion != null ? protocolVersion : ProtocolVersion.PROTOCOL_VERSION_26;
  }

  protected String getInfinispanServerVersion() {
    return infinispanServerVersion;
  }

  /**
   * Defines caches that should be created after the container has started.
   *
   * @param cacheNames An array of cache names
   * @return The container itself
   */
  public InfinispanContainer withCaches(final String... cacheNames) {
    return withCaches(Arrays.asList(cacheNames));
  }

  /**
   * Defines caches that should be created after the container has started.
   *
   * @param cacheNames A collection of cache names
   * @return The container itself
   */
  public InfinispanContainer withCaches(final Collection<String> cacheNames) {

    if (isProtocolConflict()) {
      throw new IllegalArgumentException("Programmatic cache creation only works with Hotrod protocol version >= 2.0!");
    }

    if (isMajorVersionConflict() || isMinorVersionConflict()) {
      throw new IllegalStateException("Programmatic cache creation only works with InfinispanServer version >= 9.1.0!");
    }

    this.cacheNames.clear();
    this.cacheNames.addAll(cacheNames);
    return this;
  }

  @Override
  protected void containerIsStarted(final InspectContainerResponse containerInfo) {
    cacheManager = new RemoteCacheManager(getCacheManagerConfiguration());
    if (cacheManager == null) {
      throw new IllegalStateException("Couldn't instantiate cacheManager");
    }
    this.cacheNames.forEach(this::createCache);
  }

  private Configuration getCacheManagerConfiguration() {
    ConfigurationBuilder configBuilder = new ConfigurationBuilder()
        .addServers(getHotrodEndpointConnectionString())
        .version(getProtocolVersion());
    getTransportFactory().ifPresent(transportFactory -> {
      configBuilder.transportFactory(transportFactory);
    });
    return configBuilder.build();
  }

  private void createCache(final String cacheName) {
    try {
      getCacheManager().administration().createCache(cacheName, null);
    } catch (HotRodClientException e) {
      logger().error("Couldn't create cache '{}'", cacheName, e);
    }
  }

  /**
   * Overload this method to use a custom {@link TransportFactory}.
   *
   * @return
   */
  protected Optional<Class<? extends TransportFactory>> getTransportFactory() {
    return Optional.empty();
  }

}
