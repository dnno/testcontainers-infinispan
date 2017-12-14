package de.rpr.testcontainers.infinispan;

import com.github.dockerjava.api.command.InspectContainerResponse;
import org.infinispan.client.hotrod.ProtocolVersion;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.jetbrains.annotations.NotNull;
import org.junit.runner.Description;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.LogMessageWaitStrategy;

import java.io.File;
import java.net.URL;
import java.time.Duration;
import java.util.*;

@SuppressWarnings("ALL")
public class InfinispanContainer extends GenericContainer<InfinispanContainer> {

  private static final String IMAGE_NAME = "jboss/infinispan-server";

  private static Set<ProtocolVersion> incompatibleProtocolVersions = new HashSet<>();

  static {
    incompatibleProtocolVersions.add(ProtocolVersion.PROTOCOL_VERSION_10);
    incompatibleProtocolVersions.add(ProtocolVersion.PROTOCOL_VERSION_11);
    incompatibleProtocolVersions.add(ProtocolVersion.PROTOCOL_VERSION_12);
    incompatibleProtocolVersions.add(ProtocolVersion.PROTOCOL_VERSION_13);
  }

  private ProtocolVersion protocolVersion;
  private Collection<String> cacheNames;

  private RemoteCacheManager cacheManager;

  public InfinispanContainer() {
    this("latest");
  }

  public InfinispanContainer(final String version) {
    super(IMAGE_NAME + ":" + version);
    withStartupTimeout(Duration.ofMillis(20000));
    withCommand("standalone");
    waitingFor(new LogMessageWaitStrategy().withRegEx(".*Infinispan Server.*started in.*\\s"));
  }

  public InfinispanContainer withProtocolVersion(final ProtocolVersion protocolVersion) {
    if (incompatibleProtocolVersions.contains(protocolVersion)) {
      throw new IllegalArgumentException("You have to use a Hotrod protocol version of 2.0 at least. 1.x can't create caches through the API.");
    }
    this.protocolVersion = protocolVersion;
    return this;
  }

  public InfinispanContainer withCaches(final Collection<String> cacheNames) {
    this.cacheNames = cacheNames;
    return this;
  }

  public InfinispanContainer withCaches(final String... cacheNames) {
    return withCaches(Arrays.asList(cacheNames));
  }

  @Override
  protected void containerIsStarted(final InspectContainerResponse containerInfo) {
    cacheManager = new RemoteCacheManager(new ConfigurationBuilder()
        .addServers(getServerAddress())
        .version(getProtocolVersion())
        .build());

    this.cacheNames.forEach(cacheName -> {
      cacheManager.administration().createCache(cacheName, null);
    });
  }

  @Override
  protected void finished(final Description description) {
    cacheManager.stop();
    super.finished(description);
  }

  private ProtocolVersion getProtocolVersion() {
    return protocolVersion != null ? protocolVersion : ProtocolVersion.PROTOCOL_VERSION_26;
  }

  public String getServerAddress() {
    return getContainerIpAddress() + ":" + getMappedPort(11222);
  }

  public RemoteCacheManager getCacheManager() {
    return cacheManager;
  }
}
