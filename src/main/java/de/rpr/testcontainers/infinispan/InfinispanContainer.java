package de.rpr.testcontainers.infinispan;

import com.github.dockerjava.api.command.InspectContainerResponse;
import org.apache.commons.lang.StringUtils;
import org.infinispan.client.hotrod.ProtocolVersion;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.junit.runner.Description;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.LogMessageWaitStrategy;
import org.testcontainers.shaded.io.netty.util.internal.StringUtil;

import java.time.Duration;
import java.util.*;

import static java.time.temporal.ChronoUnit.SECONDS;

@SuppressWarnings("ALL")
public class InfinispanContainer<SELF extends InfinispanContainer<SELF>> extends GenericContainer<SELF> {

  private static final String IMAGE_NAME = "jboss/infinispan-server";
  public static final String STANDALONE_MODE_CMD = "standalone";

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
    this(IMAGE_NAME + ":latest");
  }

  public InfinispanContainer(final String imageName) {
    super(imageName);

    this.withCommand(STANDALONE_MODE_CMD);

    this.waitStrategy = new LogMessageWaitStrategy()
        .withRegEx(".*Infinispan Server.*started in.*\\s")
        .withTimes(2)
        .withStartupTimeout(Duration.of(20, SECONDS));
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
  public SELF withCommand(String cmd) {
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
  public SELF withCommand(String... commandParts) {
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
