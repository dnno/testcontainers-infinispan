package de.rpr.testcontainers.infinispan;

import com.github.dockerjava.api.command.InspectContainerResponse;
import org.infinispan.client.hotrod.ProtocolVersion;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.junit.runner.Description;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.LogMessageWaitStrategy;

import java.time.Duration;
import java.util.*;

import static java.time.temporal.ChronoUnit.SECONDS;

/**
 * An implementation of the {@link org.testcontainers.containers.GenericContainer} class that can be
 * used to easily instantiate an Infinispan server for integration tests.
 *
 * @param <SELF>
 */
@SuppressWarnings("ALL")
public class InfinispanContainer extends GenericContainer<InfinispanContainer> {

  private static final String IMAGE_NAME = "jboss/infinispan-server";
  public static final String STANDALONE_MODE_CMD = "standalone";

  /*
   * An enumeration of the endpoints provided by Infinispan, that this container provides access to.
   */
  private enum InfinispanEndpoints {
    HOTROD(11222);

    private final int protocolPort;

    private InfinispanEndpoints(final int port) {
      this.protocolPort = port;
    }
  }

  private static final Set<ProtocolVersion> incompatibleProtocolVersions = new HashSet<>();

  static {
    incompatibleProtocolVersions.add(ProtocolVersion.PROTOCOL_VERSION_10);
    incompatibleProtocolVersions.add(ProtocolVersion.PROTOCOL_VERSION_11);
    incompatibleProtocolVersions.add(ProtocolVersion.PROTOCOL_VERSION_12);
    incompatibleProtocolVersions.add(ProtocolVersion.PROTOCOL_VERSION_13);
  }

  private ProtocolVersion protocolVersion;
  private Collection<String> cacheNames = new ArrayList<>();

  private RemoteCacheManager cacheManager;

  /**
   * Construct an instance using the latest Infinispan image version.
   */
  public InfinispanContainer() {
    this(IMAGE_NAME + ":latest");
  }

  /**
   * Construct n instance using the specifice image name.
   *
   * @param imageName The image name, must contain a version reference
   */
  public InfinispanContainer(final String imageName) {
    super(imageName);

    this.withCommand(STANDALONE_MODE_CMD);
    withExposedPorts(Arrays.stream(InfinispanEndpoints.values()).map(endpoint -> endpoint.protocolPort).toArray(Integer[]::new));

    this.waitStrategy = new LogMessageWaitStrategy()
        .withRegEx(".*Infinispan Server.*started in.*\\s")
        .withStartupTimeout(Duration.of(60, SECONDS));
  }

  /**
   * Overloading, because we want to make sure that the "standalone" command is always present.
   * <p>
   * The {@link org.testcontainers.containers.GenericContainer#setCommand} method splits on empty string.
   * In order to avoid dependency of that behaviour, we set the cmd first, then getting the commandParts
   * and ensuring that it contains the "standalone" command.
   * </p>
   *
   * @param cmd The command(s) to set. {@link org.testcontainers.containers.GenericContainer#setCommand}
   * @return The container instance
   */
  @Override
  public InfinispanContainer withCommand(String cmd) {
    super.setCommand(cmd);
    this.withCommand(ensureStandaloneCommand(getCommandParts()));
    return self();
  }

  /**
   * Overloading, because we want to make sure that the "standalone" command is always present.
   *
   * @param cmd
   * @return
   */
  @Override
  public InfinispanContainer withCommand(String... commandParts) {
    this.setCommand(ensureStandaloneCommand(commandParts));
    return self();
  }

  private String[] ensureStandaloneCommand(final String[] commandParts) {
    List<String> commands = Arrays.asList(commandParts);
    if (commands.contains(STANDALONE_MODE_CMD)) {
      return commands.toArray(new String[0]);
    } else {
      commands.add(STANDALONE_MODE_CMD);
      return commands.toArray(new String[0]);
    }
  }

  public InfinispanContainer withProtocolVersion(final ProtocolVersion protocolVersion) {
    this.protocolVersion = protocolVersion;
    return this;
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
    if (incompatibleProtocolVersions.contains(protocolVersion)) {
      throw new IllegalArgumentException(
          "You have to use a Hotrod protocol version of 2.0 at least. 1.x can't create caches through the API. " +
              "You can still map a configuration file into the container using '.withClasspathResourceMapping()'");
    }
    this.cacheNames = cacheNames;
    return this;
  }

  @Override
  protected void containerIsStarted(final InspectContainerResponse containerInfo) {
    cacheManager = new RemoteCacheManager(new ConfigurationBuilder()
        .addServers(getHotrodEndpointConnectionString())
        .version(getProtocolVersion())
        .build());

    this.cacheNames.forEach(cacheName -> cacheManager.administration().createCache(cacheName, null));
  }

  @Override
  protected void finished(final Description description) {
    if (cacheManager != null) {
      cacheManager.stop();
    }
    super.finished(description);
  }

  private ProtocolVersion getProtocolVersion() {
    return protocolVersion != null ? protocolVersion : ProtocolVersion.PROTOCOL_VERSION_26;
  }

  /**
   * Retrieve the Hotrod endpoint address used to connect to the Infinispan instance inside the container.
   *
   * @return A String of the format [ipaddress]:[port]
   */
  public String getHotrodEndpointConnectionString() {
    return getContainerIpAddress() + ":" + getMappedPort(InfinispanEndpoints.HOTROD.protocolPort);
  }

  /**
   * Retrieve a preconfigured {@link org.infinispan.client.hotrod.RemoteCacheManager}.
   *
   * @return A cacheManager
   */
  public RemoteCacheManager getCacheManager() {
    return cacheManager;
  }
}
