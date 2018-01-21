package de.rpr.testcontainers.infinispan;

import de.rpr.testcontainers.infinispan.transport.DisabledTopologyStateTransferTransportFactory;
import org.infinispan.client.hotrod.impl.transport.TransportFactory;
import org.testcontainers.containers.BindMode;

import java.util.Optional;

/**
 * An implementation of the {@link org.testcontainers.containers.GenericContainer} class that can be
 * used to easily instantiate an Infinispan server for integration tests.
 */
@SuppressWarnings("ALL")
public class ClusteredInfinispanContainer extends InfinispanContainer {

  private static final String IMAGE_NAME = "jboss/infinispan-server";

  /**
   * Construct an instance using the latest Infinispan image version.
   */
  public ClusteredInfinispanContainer() {
    this(IMAGE_NAME + ":latest");
  }

  /**
   * Construct an instance using the specifice image name.
   *
   * @param imageName The image name, must contain a version reference
   */
  public ClusteredInfinispanContainer(final String imageName) {
    super(imageName);
  }

  /**
   * Use a custom {@link TransportFactory} to disable the Hotrod Topology-State-Transfer.
   *
   * @return
   */
  @Override
  protected Optional<Class<? extends TransportFactory>> getTransportFactory() {
    return Optional.of(DisabledTopologyStateTransferTransportFactory.class);
  }

  /**
   * Links a configuration file for a clustered Infinispan server into the container. The configuration file format needs to match the server version.
   *
   * @param filenameFromClasspath The filename containing the standalone configuration.
   * @return The container itself
   */
  public ClusteredInfinispanContainer withClusteredConfiguration(final String filenameFromClasspath) {
    return (ClusteredInfinispanContainer) withClasspathResourceMapping(
        filenameFromClasspath,
        "/opt/jboss/infinispan-server/standalone/configuration/clustered.xml",
        BindMode.READ_ONLY);
  }

}
