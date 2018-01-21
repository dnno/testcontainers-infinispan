package de.rpr.testcontainers.infinispan;

import com.github.dockerjava.api.command.InspectContainerResponse;
import org.infinispan.client.hotrod.ProtocolVersion;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.exceptions.HotRodClientException;
import org.testcontainers.containers.BindMode;

import java.util.*;
import java.util.stream.IntStream;

/**
 * An implementation of the {@link org.testcontainers.containers.GenericContainer} class that can be
 * used to easily instantiate an Infinispan server for integration tests.
 *
 * @param <SELF>
 */
@SuppressWarnings("ALL")
public class StandaloneInfinispanContainer extends InfinispanContainer {

  private static final String IMAGE_NAME = "jboss/infinispan-server";
  private static final String STANDALONE_MODE_CMD = "standalone";

  /**
   * Construct an instance using the latest Infinispan image version.
   */
  public StandaloneInfinispanContainer() {
    this(IMAGE_NAME + ":latest");
  }

  /**
   * Construct n instance using the specifice image name.
   *
   * @param imageName The image name, must contain a version reference
   */
  public StandaloneInfinispanContainer(final String imageName) {
    super(imageName);

    this.withCommand(STANDALONE_MODE_CMD);
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
  public StandaloneInfinispanContainer withCommand(String cmd) {
    super.setCommand(cmd);
    this.withCommand(ensureStandaloneCommand(getCommandParts()));
    return this;
  }

  /**
   * Overloading, because we want to make sure that the "standalone" command is always present.
   *
   * @param cmd
   * @return
   */
  @Override
  public StandaloneInfinispanContainer withCommand(String... commandParts) {
    this.setCommand(ensureStandaloneCommand(commandParts));
    return this;
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

  /**
   * Links a configuration file for a standalone Infinispan server into the container. The configuration file format needs to match the server version.
   *
   * @param filenameFromClasspath The filename containing the standalone configuration.
   * @return The container itself
   */
  public StandaloneInfinispanContainer withStandaloneConfiguration(final String filenameFromClasspath) {
    return (StandaloneInfinispanContainer) withClasspathResourceMapping(
        filenameFromClasspath,
        "/opt/jboss/infinispan-server/standalone/configuration/standalone.xml",
        BindMode.READ_ONLY);
  }

}
